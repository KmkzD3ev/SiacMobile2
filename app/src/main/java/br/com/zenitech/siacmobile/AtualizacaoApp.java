package br.com.zenitech.siacmobile;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

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

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.siacmobile.domains.EnviarDados;
import br.com.zenitech.siacmobile.domains.Sincronizador;
import br.com.zenitech.siacmobile.interfaces.IEnviarDados;
import br.com.zenitech.siacmobile.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AtualizacaoApp extends AppCompatActivity {

    private String entregaFuturaString;


    //
    private SharedPreferences prefs;
    ClassAuxiliar classAuxiliar;
    private DatabaseHelper bd;
    private static final String TAG = "GerenciarCF";
    private Context context = null;
    private ProgressDialog pd;
    String[] dados, dadosFin, dadosContasReceber, dadosVales;
    private int quant = 0;
    AppCompatButton btnAtualizarBanco, btnEnviarDados;

    private AlertDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atualizacao_app);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);*/





        bd = new DatabaseHelper(this);
        bd.listarEntregasFuturas();

        Log.d( "SERVIDOR CLASS ", "onCreate: ENVIO DA STRING PHP" );
        ArrayList<Integer> registrosEntregaFutura = bd.listarEntregasFuturas();
        // Converter a lista de inteiros em uma string separada por vírgulas e adicionar aspas duplas ao redor
        entregaFuturaString = "," + registrosEntregaFutura.toString()
                .replace("[", "")  // Remover o colchete de abertura
                .replace("]", "")  // Remover o colchete de fechamento
                .replace(" ", "");  // Remover espaços em branco


        //
        context = this;
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        classAuxiliar = new ClassAuxiliar();

        bd = new DatabaseHelper(context);

        btnAtualizarBanco = findViewById(R.id.btnAtualizarBanco);
        btnEnviarDados = findViewById(R.id.btnEnviarDados);

        //
        try {
            //
            if (bd.getAllVendas().size() > 0 || bd.getAllRecebidos().size() > 0 || bd.getAllVales().size() > 0) {
                dados = bd.EnviarDados(classAuxiliar.exibirData(prefs.getString("data_movimento_atual", "")));
                dadosFin = bd.EnviarDadosFinanceiroTemp();
                dadosContasReceber = bd.EnviarDadosContasReceber();
                dadosVales = bd.EnviarDadosVales(prefs.getString("data_movimento_atual", ""));
                btnEnviarDados.setVisibility(View.VISIBLE);
                btnAtualizarBanco.setVisibility(View.GONE);
            } else {
                btnEnviarDados.setVisibility(View.GONE);
                btnAtualizarBanco.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("Enviar Dados", Objects.requireNonNull(e.getMessage()));
        }

        btnEnviarDados.setOnClickListener(v -> {
            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(context, "Enviando dados...", "Aguarde...",
                    true, false);

            enviarDados();
            enviarDadosContasReceber();
            enviarDadosVales();
        });

        btnAtualizarBanco.setOnClickListener(v -> mostrarMsgFinalizarPOS());
    }

    private void mostrarMsgFinalizarPOS() {

        //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
        bd.FecharConexao();
        context.deleteDatabase("siacmobileDB");
        Intent i = new Intent(context, SincronizarBancoDados.class);
        i.putExtra("atualizarbd", "sim");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

        /*//Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.logosiac);
        //define o titulo
        builder.setTitle("Atenção");
        //define a mensagem
        builder.setMessage("Para continuar é preciso atualizar seu banco de dados");
        //define um botão como positivo
        builder.setPositiveButton("Atualizar Agora", (arg0, arg1) -> {
            //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
            context.deleteDatabase("siacmobileDB");
            Intent i = new Intent(context, SincronizarBancoDados.class);
            i.putExtra("atualizarbd", "sim");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
        builder.setCancelable(false);
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();*/
    }

    void enviarDados() {
        findViewById(R.id.cv_btn_enviar_dados).setVisibility(View.GONE);
        //
        // Exibir o valor da string convertida no log
        Log.d("EntregaFutura", "String de entrega futura para envio: " + entregaFuturaString);
        final IEnviarDados iEnviarDados = IEnviarDados.retrofit.create(IEnviarDados.class);

        final Call<ArrayList<EnviarDados>> call = iEnviarDados.enviarDados(
                "850",
                prefs.getString("serial", ""),
                "" + dados[0],
                "" + dados[1],
                "" + dados[2],
                "" + dados[3],
                "" + dados[4],
                "" + dados[5],
                "" + dadosFin[0],
                "" + dadosFin[1],
                "" + dadosFin[2],
                "" + dadosFin[3],
                "" + dadosFin[4],
                "" + dadosFin[5],
                "" + dadosFin[6],
                "" + dadosFin[7],
                entregaFuturaString
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


        /*//APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
        bd.FecharConexao();
        context.deleteDatabase("siacmobileDB");
        Intent i = new Intent(context, SincronizarBancoDados.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();*/


        btnEnviarDados.setVisibility(View.GONE);
        btnAtualizarBanco.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(context, Principal2.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}