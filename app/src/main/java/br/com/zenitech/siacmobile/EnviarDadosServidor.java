package br.com.zenitech.siacmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.siacmobile.domains.EnviarDados;
import br.com.zenitech.siacmobile.domains.Sincronizador;
import br.com.zenitech.siacmobile.interfaces.IEnviarDados;
import br.com.zenitech.siacmobile.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnviarDadosServidor extends AppCompatActivity {

    private String entregaFuturaString;

    //
    private SharedPreferences prefs;
    ClassAuxiliar classAuxiliar;
    private DatabaseHelper bd;
    private static final String TAG = "GerenciarCF";
    private Context context = null;
    private ProgressDialog pd;
    String[] dados, dadosFin, dadosContasReceber, dadosVales ,dadosProdutos;
    private int quant = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_dados_servidor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        /************* RECUPERANDO DADOS PRA ENVIO SERVIDOR ENTREGA-FUTURA *********/

        bd = new DatabaseHelper(this);
        bd.listarEntregasFuturas();

        Log.d( "SERVIDOR CLASS ", "onCreate: ENVIO DA STRING PHP" );
        ArrayList<Integer> registrosEntregaFutura = bd.listarEntregasFuturas();
        // Converter a lista de inteiros em uma string separada por vírgulas
        entregaFuturaString = "," + registrosEntregaFutura.toString()
                .replace("[", "")  // Remover o colchete de abertura
                .replace("]", "")  // Remover o colchete de fechamento
                .replace(" ", "");  // Remover espaços em branco

        // Logs para verificar os dados recebidos
        Log.d("RECEBENDO NO ENVIAR !", "Registros de entrega_futura_venda: " + entregaFuturaString);
        /************************************************/

        //
        context = this;
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        classAuxiliar = new ClassAuxiliar();
        bd = new DatabaseHelper(context);

        //
        dados = bd.EnviarDados(classAuxiliar.exibirData(prefs.getString("data_movimento_atual", "")));


        dadosProdutos = bd.EnviarDadosProdutos();

        //
       // dadosFin = bd.EnviarDadosFinanceiro();


        //
        dadosContasReceber = bd.EnviarDadosContasReceber();

        dadosVales = bd.EnviarDadosVales(prefs.getString("data_movimento_atual", ""));

        findViewById(R.id.cv_enviar_dados).setOnClickListener(v -> {
            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(context, "Enviando dados...", "Aguarde...",
                    true, false);

            enviarDados();
            enviarDadosContasReceber();
            enviarDadosVales();
            //enviarDadosProdutos();
        });
    }
    void enviarDados() {
        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);

        // Instancia a interface da API
        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);

        // Gera o JSON unificado como string
        String dataMovimento = prefs.getString("data_movimento_atual", "");
        if (dataMovimento.isEmpty()) {
            dataMovimento = "1970-01-01"; // Valor padrão, ajuste conforme necessário
        }

        // Gera o JSON inicial e aplica as formatações
        String pedidosJsonString = bd.montarJson(classAuxiliar.exibirData(dataMovimento)); // Gera JSON
        JsonObject pedidosJson = new Gson().fromJson(pedidosJsonString, JsonObject.class); // Converte em JSON
        JsonObject jsonFormatado = classAuxiliar.formatarJsonParaEnvio(pedidosJson); // Aplica formatações

        // Loga o JSON para verificar se está formatado corretamente
        Log.d("EnvioDados", "TELA: 850");
        Log.d("EnvioDados", "SERIAL: " + prefs.getString("serial", ""));
        Log.d("EnvioDados", "PEDIDOS FORMATADOS: " + jsonFormatado.toString()); // Verifica se o JSON está correto

        // Chamada para enviar o JSON unificado
        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDadosUnificado(
                "850", // Tela de envio
                prefs.getString("serial", ""), // Serial do dispositivo
                jsonFormatado // Envia o JsonObject formatado
        );

        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
            @Override
            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
                final ArrayList<EnviarDados> sincronizacao = response.body();
                if (sincronizacao != null) {
                    quant++;
                    FinalizarPOS();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {
                // CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }


//    void enviarDados() {
//        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);
//
//        // Instancia a interface da API
//        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
//
//        // Gera o JSON unificado
//        String pedidosJson = bd.montarJson(classAuxiliar.exibirData(prefs.getString("data_movimento_atual", "")));
//
//        // Loga o JSON para verificação antes do envio
//        Log.d("EnvioDados", "TELA: 850");
//        Log.d("EnvioDados", "SERIAL: " + prefs.getString("serial", ""));
//        Log.d("EnvioDados", "PEDIDOS: " + pedidosJson);
//        Log.d("EnvioDados", "PEDIDOS: " + pedidosJson);
//
//        // Chamada para enviar o JSON unificado
//        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDadosUnificado(
//                "850", // Tela de envio
//                prefs.getString("serial", ""), // Serial do dispositivo
//                pedidosJson // JSON completo
//        );
//
//        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
//            @Override
//            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
//                final ArrayList<EnviarDados> sincronizacao = response.body();
//                if (sincronizacao != null) {
//                    quant++;
//                    FinalizarPOS();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {
//                // Cancela a mensagem de sincronização
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }
//                Log.e("EnvioDados", "Falha ao enviar dados: " + t.getMessage(), t);
//            }
//        });
//    }


//    void enviarDados() {
//        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);
//        //
//        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
//
//        // Logando os dados antes de enviar para a API
//        Log.d("EnvioDados", "TELA: 850");
//        Log.d("EnvioDados", "SERIAL: " + prefs.getString("serial", ""));
//        Log.d("EnvioDados", "VENDAS: " + dados[0]);
//        Log.d("EnvioDados", "CLIENTES: " + dados[1]);
//        Log.d("EnvioDados", "PRODUTOS: " + dados[2]);
//        Log.d("EnvioDados", "QUANTIDADES: " + dados[3]);
//        Log.d("EnvioDados", "DATAS: " + dados[4]);
//        Log.d("EnvioDados", "VALORES: " + dados[5]);
//        Log.d("EnvioDados", "FINANCEIROS: " + dadosFin[0]);
//        Log.d("EnvioDados", "FINVEN: " + dadosFin[1]);
//        Log.d("EnvioDados", "VENCIMENTOS: " + dadosFin[2]);
//        Log.d("EnvioDados", "VALORESFIN: " + dadosFin[3]);
//        Log.d("EnvioDados", "FPAGAMENTOS: " + dadosFin[4]);
//        Log.d("EnvioDados", "DOCUMENTOS: " + dadosFin[5]);
//        Log.d("EnvioDados", "NOTASFISCAIS: " + dadosFin[6]);
//        Log.d("EnvioDados", "CODALIQUOTAS: " + dadosFin[7]);
//        Log.d("EnvioDados", "ENTFUTURA: " + entregaFuturaString); // Logando a string de entrega futura
//
//
//        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDados(
//                "850",
//                prefs.getString("serial", ""),
//                "" + dados[0],
//                "" + dados[1],
//                "" + dados[2],
//                "" + dados[3],
//                "" + dados[4],
//                "" + dados[5],
//                "" + dadosFin[0],
//                "" + dadosFin[1],
//                "" + dadosFin[2],
//                "" + dadosFin[3],
//                "" + dadosFin[4],
//                "" + dadosFin[5],
//                "" + dadosFin[6],
//                "" + dadosFin[7],
//                "" + entregaFuturaString
//
//        );
//
//        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
//            @Override
//            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
//                final ArrayList<EnviarDados> sincronizacao = response.body();
//                if (sincronizacao != null) {
//                    quant++;
//                    FinalizarPOS();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {
//
//                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }
//            }
//        });
//    }
//
//    void enviarDadosProdutos() {
//        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);
//
//        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
//
//        // Logando apenas os campos relevantes
//        Log.d("EnvioDadosProdutos", "TELA: 852");
//        Log.d("EnvioDadosProdutos", "SERIAL: " + prefs.getString("serial", ""));
//
//        // Realizando a chamada para cada produto individualmente
//        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDadosProdutos(
//                "852",
//                prefs.getString("serial", ""),
//               "" + dadosProdutos[2],
//                "" + dadosProdutos[0],
//                "" +dadosProdutos[1]
//        );
//
//        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
//                @Override
//                public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
//                    final ArrayList<EnviarDados> sincronizacao = response.body();
//                    if (sincronizacao != null) {
//
//                        FinalizarPOS();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {
//
//                    if (pd != null && pd.isShowing()) {
//                        pd.dismiss();
//                    }
//                }
//            });


        // Log fim do envio para a venda
//        Log.d("EnvioDadosProdutos", "Envio concluído para todos os produtos de ID_DA_VENDA: " );
//    }

    void enviarDadosContasReceber() {
        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDadosContasReceber(
                "851",
                prefs.getString("serial", ""),
                "" + dadosContasReceber[0],
                "" + dadosContasReceber[1],
                "" + dadosContasReceber[2],
                "" + dadosContasReceber[3],
                "" + dadosContasReceber[4],
                "" + dadosContasReceber[5],
                "" + dadosContasReceber[6],
                "" + dadosContasReceber[7],
                "" + dadosContasReceber[8],
                "" + dadosContasReceber[9],
                "" + dadosContasReceber[10],
                "" + dadosContasReceber[11],
                "" + dadosContasReceber[12],
                "" + dadosContasReceber[13]
        );

        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
            @Override
            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
                final ArrayList<EnviarDados> sincronizacao = response.body();
                if (sincronizacao != null) {
                    quant++;
                    FinalizarPOS();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {

                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }

    private void enviarDadosVales() {
        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDadosVales(
                "753",
                prefs.getString("serial", ""),
                "" + dadosVales[0],
                "" + dadosVales[2],
                "" + dadosVales[3],
                "" + dadosVales[4]
        );

        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
            @Override
            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
                final ArrayList<EnviarDados> sincronizacao = response.body();
                if (sincronizacao != null) {
                    quant++;
                    FinalizarPOS();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {

                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }

    void FinalizarPOS() {

        //if(quant < 3) return;

        //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
        pd = ProgressDialog.show(context, "Finalizando POS...", "Aguarde...",
                true, false);

        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);
        final Call<Sincronizador> call = iSincronizar.ativarDesativarPOS(
                "desativar",
                prefs.getString("serial_app", "")
        );

        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {
                final Sincronizador sincronizacao = response.body();
                if (sincronizacao != null) {
                    if (sincronizacao.getErro().equalsIgnoreCase("0")) {

                    }
                } else {
                    Toast.makeText(context, "Não foi possível Finalizar o POS!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                //msgErro = "Não conseguimos ativar o app! Tente novamente em alguns instantes.";
                Toast.makeText(context, "Falha - " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //
        prefs.edit().putBoolean("sincronizado", false).apply();
        prefs.edit().putString("unidade_vendedor", "").apply();
        prefs.edit().putString("unidade_usuario", "").apply();
        prefs.edit().putString("codigo_usuario", "").apply();
        prefs.edit().putString("login_usuario", "").apply();
        prefs.edit().putString("senha_usuario", "").apply();
        prefs.edit().putString("usuario_atual", "").apply();
        prefs.edit().putString("nome_vendedor", "").apply();
        prefs.edit().putString("data_movimento", "").apply();
        prefs.edit().putString("biometria", "").apply();

        //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }

        Toast.makeText(context, "POS Finalizado!", Toast.LENGTH_SHORT).show();


        //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
        bd.FecharConexao();
        context.deleteDatabase("siacmobileDB");
        Intent i = new Intent(context, SincronizarBancoDados.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}


//    void enviarDados() {
//        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);
//
//        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);
//
//        // Logando os dados antes de enviar para a API
//        Log.d("EnvioDados", "TELA: 850");
//        Log.d("EnvioDados", "SERIAL: " + prefs.getString("serial", ""));
//        Log.d("EnvioDados", "VENDAS: " + dados[0]);
//        Log.d("EnvioDados", "CLIENTES: " + dados[1]);
//        Log.d("EnvioDados", "DATAS: " + dados[4]);
//
//        // Divide os dados por ";" para visualizar pedidos com múltiplos produtos
//        String[] vendasArray = dados[0].split(";");
//        String[] clientesArray = dados[1].split(";");
//        String[] produtosArray = dados[2].split(";");
//        String[] quantidadesArray = dados[3].split(";");
//        String[] datasArray = dados[4].split(";");
//        String[] valoresArray = dados[5].split(";");
//
//        for (int i = 1; i < vendasArray.length; i++) {
//            Log.d("Pedido", "---- Detalhes do Pedido " + i + " ----");
//            Log.d("Pedido", "Venda ID: " + vendasArray[i]);
//            Log.d("Pedido", "Cliente ID: " + clientesArray[i]);
//            Log.d("Pedido", "Data: " + datasArray[i]);
//
//            // Logando produtos, quantidades e valores de cada venda
//            Log.d("Pedido", "Produtos: " + produtosArray[i]);
//            Log.d("Pedido", "Quantidades: " + quantidadesArray[i]);
//            Log.d("Pedido", "Valores: " + valoresArray[i]);
//        }
//
//        // Log para os dados financeiros, como no código original
//        Log.d("EnvioDados", "FINANCEIROS: " + dadosFin[0]);
//        Log.d("EnvioDados", "FINVEN: " + dadosFin[1]);
//        Log.d("EnvioDados", "VENCIMENTOS: " + dadosFin[2]);
//        Log.d("EnvioDados", "VALORESFIN: " + dadosFin[3]);
//        Log.d("EnvioDados", "FPAGAMENTOS: " + dadosFin[4]);
//        Log.d("EnvioDados", "DOCUMENTOS: " + dadosFin[5]);
//        Log.d("EnvioDados", "NOTASFISCAIS: " + dadosFin[6]);
//        Log.d("EnvioDados", "CODALIQUOTAS: " + dadosFin[7]);
//        Log.d("EnvioDados", "ENTFUTURA: " + entregaFuturaString);
//
//        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDados(
//                "850",
//                prefs.getString("serial", ""),
//               "" + dados[0],
//                "" +  dados[1],
//               "" + dados[2],
//               "" + dados[3],
//               "" + dados[4],
//               "" + dados[5],
//                "" +  dadosFin[0],
//              "" + dadosFin[1],
//               ""+ dadosFin[2],
//               "" +dadosFin[3],
//             ""  + dadosFin[4],
//              "" + dadosFin[5],
//             "" +  dadosFin[6],
//              "" + dadosFin[7],
//              "" + entregaFuturaString
//        );
//
//        call.enqueue(new Callback<ArrayList<EnviarDados>>() {
//            @Override
//            public void onResponse(Call<ArrayList<EnviarDados>> call, Response<ArrayList<EnviarDados>> response) {
//                final ArrayList<EnviarDados> sincronizacao = response.body();
//                if (sincronizacao != null) {
//                    quant++;
//                    FinalizarPOS();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ArrayList<EnviarDados>> call, Throwable t) {
//                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
//                if (pd != null && pd.isShowing()) {
//                    pd.dismiss();
//                }
//            }
//        });
//    }
