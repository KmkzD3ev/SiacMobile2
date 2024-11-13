
package br.com.zenitech.siacmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import br.com.zenitech.siacmobile.adapters.ProdutosAdapter;
import br.com.zenitech.siacmobile.domains.DadosCompletosDomain;
import br.com.zenitech.siacmobile.domains.FinanceiroReceberClientes;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;
import br.com.zenitech.siacmobile.domains.VendasDomain;

public class Vendas extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private CreditoPrefs creditoPrefs;


    AlertDialog alerta;

    ArrayList<String> listaProdutos;
    //LISTAR VENDAS
    ArrayList<VendasDomain> listaVendas;
    ProdutosAdapter adapter;
    RecyclerView rvVendas;


    Spinner spProduto;
    EditText etQuantidade, etPreco;
    public static TextView textTotalItens, txtTotalVenda;
    Button btnAddProdutoLista;
    private DatabaseHelper bd;

    int totalVenda = 0;
    String id_cliente = "";
    String nome_cliente = "";
    String latitude_cliente = "";
    String longitude_cliente = "";
    int id ;
    int id_venda_app = 1;
    private String total_venda = "0.0";
    String saldo = "";
    String cpfcnpj = "";
    String endereco = "";
    private ClassAuxiliar classAuxiliar;

    //DADOS PARA PASSAR AO EMISSOR WEB
    private String produto_emissor;
    private String quantidade_emissor;
    private String valor_unit_emissor;
    private String editandoVenda = "";
    private boolean isPrecoFixo = false; // PARAMETRO PARA VERIFICAÇO E EDIÇAO DE PREÇO PRE-DEFINIDO
    private int estadoEntregaFuturaOriginal;
    // Dentro da classe Vendas ou onde o método atualizarListaProdutos está implementado
    private ArrayList<ProdutoEmissor> listaProdutosVenda = new ArrayList<>();
    private boolean vendaAlterada = false;  // Inicialmente, não há alterações




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendas);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);




        /************ COMPONENTES PARA PERSISTIR ESTADO CHECBOX ***********/

        // Inicializa corretamente o SharedPreferences e o Editor antes de qualquer uso
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();  // Inicializando o Editor aqui

        String editarFlag = getIntent().getStringExtra("editar");
        Log.d("VALOR DA FLAG", "onCreate: RECEBENDO " + editarFlag );

        // Verifica se a chave "editar" é igual a "sim"
        if ("sim".equals(editarFlag)) {
            // Se estiver em modo de edição, usa o id atual sem incrementar
            id = prefs.getInt("id_venda", 1);
            Log.d("EDITANDO", "onCreate: USANDO ID EXISTENTE PARA VENDA " + id);
            Toast.makeText(this, "UMA VENDA EDITADA", Toast.LENGTH_SHORT).show();
        } else {
            // Se não estiver no modo de edição, incrementa o id normalmente
            id = prefs.getInt("id_venda", 1) ;
            Log.d("I.D.S", "onCreate: INICIANDO TRABALHO COM IDS " + id);
            id_venda_app = prefs.getInt("id_venda_app", 1) ;
            Log.d("I.D.S", "onCreate: INICIANDO TRABALHO COM IDS " + id_venda_app);
            id = id + 1 ;
            id_venda_app = id_venda_app + 1 ;
            ed.putInt("id_venda_app", id_venda_app).apply();
            ed.putInt("id_venda", id).apply();
            Log.d("INSERT", "onCreate: AUMENTANDO O VALOR PRA VENDA " + id);
            Log.d("FINALIZANDO", "onCreate: INCREMENTADO ID DA VENDA PARA " + id_venda_app);

            Toast.makeText(this, "ESTA É UMA VENDA NOVA", Toast.LENGTH_SHORT).show();
        }



        // Inicializar entrega_futura_venda como 0 (padrão)
        ed.putInt("entrega_futura_venda", 0).apply();
        Log.d("CheckBox", "Valor padrão de entrega_futura_venda definido para: 0");

        //
        classAuxiliar = new ClassAuxiliar();

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();
        creditoPrefs = new CreditoPrefs(this);


        //
        bd = new DatabaseHelper(this);
        listaProdutos = bd.getProdutos();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduto = findViewById(R.id.spProdutos);
        spProduto.setAdapter(adapter);
        //spProduto.requestFocus();


        // Chama o método getRelatorioVendas e loga o resultado detalhado
        ArrayList<VendasDomain> relatorioVendas = bd.getRelatorioVendas();
        Log.d("getRelatorioVendas", "Número de vendas retornadas: " + relatorioVendas.size());

        // Itera sobre cada venda e loga os detalhes
        for (VendasDomain venda : relatorioVendas) {
            Log.d("RelatorioVendas", "Código Venda: " + venda.getCodigo_venda());
            Log.d("RelatorioVendas", "Código Cliente: " + venda.getCodigo_venda());
            Log.d("RelatorioVendas", "Unidade Venda: " + venda.getUnidade_venda());
            Log.d("RelatorioVendas", "Produto Venda: " + venda.getProduto_venda());
            Log.d("RelatorioVendas", "Data Movimento: " + venda.getData_movimento());
            Log.d("RelatorioVendas", "Quantidade Venda: " + venda.getQuantidade_venda());
            Log.d("RelatorioVendas", "Preço Unitário: " + venda.getPreco_unitario());
            Log.d("RelatorioVendas", "Valor Total: " + venda.getValor_total());
            Log.d("RelatorioVendas", "Vendedor Venda: " + venda.getVendedor_venda());
            Log.d("RelatorioVendas", "Status Autorização: " + venda.getStatus_autorizacao_venda());
            Log.d("RelatorioVendas", "Entrega Futura: " + venda.getEntrega_futura_venda());
            Log.d("RelatorioVendas", "Entrega Futura Realizada: " + venda.getEntrega_futura_realizada());
            Log.d("RelatorioVendas", "Usuário Atual: " + venda.getUsuario_atual());
            Log.d("RelatorioVendas", "Data Cadastro: " + venda.getData_cadastro());
            Log.d("RelatorioVendas", "ID Venda App: " + venda.getCodigo_venda_app());
            Log.d("RelatorioVendas", "Chave Importação: " + venda.getChave_importacao());
            // Log.d("RelatorioVendas", "Formas de Pagamento: " + venda.get());
        }

        //

        //

        bd.VendaFuturaAtiva();



        boolean vendaFuturaAtiva = bd.VendaFuturaAtiva();

        // Loga o valor retornado pelo método
        Log.d("VENDAS", "Venda Futura Ativa: " + vendaFuturaAtiva);

        /************* LISTNER DO CHECBOX *****************/
        // Inicializa o CheckBox
        CheckBox checkBoxConfirmar = findViewById(R.id.checkbox_confirmar);

        // Adiciona o listener de mudança de estado do CheckBox
        checkBoxConfirmar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Se o checkbox for marcado, definimos entrega_futura_venda para 1, caso contrário permanece 0
            int entregaFutura = isChecked ? 1 : 0;

            // Armazena o valor no SharedPreferences
            ed.putInt("entrega_futura_venda", entregaFutura).apply();

            // Log para verificação
            Log.d("CheckBox", "Valor de entrega_futura_venda definido para: " + entregaFutura);
        });
        obterDados();

        //
        rvVendas = findViewById(R.id.rvVendas);
        rvVendas.setLayoutManager(new LinearLayoutManager(Vendas.this));

        /*
        listaProdutos = bd.getProdutos();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduto = findViewById(R.id.spProdutos);
        spProduto.setAdapter(adapter);
        //spProduto.requestFocus();*/

        //
        etQuantidade = findViewById(R.id.etQuantidade);
        etQuantidade.setText("");
        //etQuantidade.requestFocus();

        //
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new MoneyTextWatcher(etPreco));

        //
        txtTotalVenda = findViewById(R.id.textTotalVenda);
        txtTotalVenda.setText(R.string.zeros);

        textTotalItens = findViewById(R.id.textTotalItens);
        textTotalItens.setText(R.string.zero);

        // Verifica se a flag "editar" é "sim" para determinar o modo de edição ou criação de uma nova venda
        if ("sim".equals(editarFlag)) {

            // Caso seja edição, usa o id atual e loga a venda
            Log.d("EDITANDO VENDA", "Flag de edição ativa. Logando dados da venda com id_venda_app: " + id_venda_app);
            logarDadosCompletosVenda();
            Toast.makeText(this, "Editando uma venda existente", Toast.LENGTH_SHORT).show();
        } else {
            // Caso contrário, inicia uma nova venda e incrementa o ID
            Log.d("NOVA VENDA", "Flag de edição inativa. Criando nova venda com novo ID.");
            inicializarVendaPrincipal();
            Toast.makeText(this, "Iniciando uma nova venda", Toast.LENGTH_SHORT).show();
        }


        etPreco.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0") || etPreco.getText().toString().equals("") || etPreco.getText().toString().equals("R$ 0,00")) {
                    Toast.makeText(Vendas.this, "Quantidade e Preço não podem ser vazios.", Toast.LENGTH_LONG).show();
                } else {
                    adicionarProdutoAoPedido();
                    AttVendafutura();
                }

                handled = true;
            }
            return handled;
        });

        //
        btnAddProdutoLista = findViewById(R.id.btnAddProdutoLista);
        btnAddProdutoLista.setOnClickListener(view -> {
            Log.i("Vendas ", etPreco.getText().toString());
            String valEtPreco = "";
            if (!etPreco.getText().toString().equals("")) {
                valEtPreco = String.valueOf(classAuxiliar.converterValores(etPreco.getText().toString()));
            }

            /*if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0") || etPreco.getText().toString().equals("") || etPreco.getText().toString().equals("R$ 0,00")) {
                Toast.makeText(Vendas.this, "Quantidade e Preço não podem ser vazios.", Toast.LENGTH_LONG).show();
            }*/

            //
            if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0")) {
                ShowMsgToast("Informe a quantidade.");
            } else if (etPreco.getText().toString().equals("")
                    || valEtPreco.equals("R$ 0,00")
                    || valEtPreco.equals("0.0")
                    || valEtPreco.equals("0.00")) {
                ShowMsgToast("Informe o valor unitário.");
            } else {
                adicionarProdutoAoPedido();
                AttVendafutura();
                atualizarListaProdutos();

            }
        });

        /******************* VERIFICAR MARCAÇAO PREVIA ENTREGA FUTURA ****************/

        // Aqui chamamos o método para verificar se a venda é futura e armazenamos o estado original
        int idVendaAppLocal = prefs.getInt("id_venda_app", 0);
        int entregaFutura = bd.getEntregaFuturaVenda(idVendaAppLocal);
        Log.d("log id ", "onCreate: id venda para entrega futura " + idVendaAppLocal);
        estadoEntregaFuturaOriginal = entregaFutura; // Armazena o estado original
        Log.d("VENDA RECUPERADA", "onCreate: recebendo entrega futura " + entregaFutura);
        // Atualiza o estado do CheckBox de acordo
        checkBoxConfirmar.setChecked(entregaFutura == 1);  // Marca o CheckBox se for venda futura

        // Adiciona o listener de mudança de estado do CheckBox
        checkBoxConfirmar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Se o checkbox for marcado, definimos entrega_futura_venda para 1, caso contrário permanece 0
            int entregaFuturaAtual = isChecked ? 1 : 0;

            // Compara o novo estado com o estado original
            if (entregaFuturaAtual != estadoEntregaFuturaOriginal) {
                // Se houve mudança, atualizamos o estado no banco de dados
                bd.atualizarEntregaFutura(entregaFuturaAtual, idVendaAppLocal);
                Log.d("CheckBox", "Estado de entrega futura alterado e atualizado para: " + entregaFuturaAtual);
            } else {
                Log.d("CheckBox", "Nenhuma mudança no estado de entrega futura.");
            }

            // Atualiza o SharedPreferences (ou outra ação necessária)
            ed.putInt("entrega_futura_venda", entregaFuturaAtual).apply();
        });

        //
        findViewById(R.id.btnPagamento).setOnClickListener(view -> {

            if (textTotalItens.getText().toString().equals("0")) {
                Toast.makeText(Vendas.this, "Adicione Itens a Venda.", Toast.LENGTH_SHORT).show();
            } else {


              /*  id = id + 1;
                ed.putInt("id_venda", id_venda_app).apply();*/

                Atualizatabela();
                obterTotalItensPedido();

                //  bd.atualizarValoresVenda(id, Double.parseDouble(valor_unit_emissor),totalVenda);

                Intent intent1 = new Intent(Vendas.this, FinanceiroDaVenda.class);
                intent1.putExtra("codigo_cliente", id_cliente);
                intent1.putExtra("nome_cliente", nome_cliente);
                intent1.putExtra("latitude_cliente", latitude_cliente);
                intent1.putExtra("longitude_cliente", longitude_cliente);
                intent1.putExtra("valorVenda", txtTotalVenda.getText().toString());

                //DADOS EMISSOR WEB
                intent1.putExtra("produto", produto_emissor);
                intent1.putExtra("quantidade", quantidade_emissor);
                intent1.putExtra("valor_unit", valor_unit_emissor);
                intent1.putExtra("saldo", saldo);

                //
                intent1.putExtra("cpfcnpj", cpfcnpj);
                intent1.putExtra("endereco", endereco);
                intent1.putExtra("editandoVenda", editandoVenda);
                Log.d("ENVIANDO FINANCEIRO", "Enviando 'editandoVenda': " + editandoVenda);

                startActivity(intent1);

                finish();
            }});

        /***************** CLICK DO BOTAO APOS A CONSULTA DO PARAMETRO  PREÇO_FIXO ******************/


        spProduto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Consulta o parâmetro global de bloqueio de edição de preço
                boolean bloqueioEdicaoPreco = bd.BloqueioEdicaoPreco();  // Verifica se o bloqueio está ativo

                // Obtém o preço do produto selecionado
                String preco = bd.getMargemCliente(spProduto.getSelectedItem().toString(), id_cliente);
                valor_unit_emissor = preco;

                // Verifica se o preço é fixo (caso o bloqueio de edição esteja ativo)
                boolean isPrecoFixo = bd.isPrecoFixo();  // Método para verificar se o preço é fixo

                // Se o bloqueio de edição de preço estiver ativo
                if (bloqueioEdicaoPreco) {
                    // Se for preço fixo, desabilita a edição
                    if (isPrecoFixo) {
                        etPreco.setText(preco);  // Preenche o campo com o preço retornado
                        etPreco.setEnabled(false);  // Bloqueia a edição
                        // Toast.makeText(Vendas.this, "PREÇO FIXO ENCONTRADO. EDIÇÃO BLOQUEADA", Toast.LENGTH_SHORT).show();
                    } else {
                        // Se não for preço fixo, permite edição
                        etPreco.setText(preco);
                        etPreco.setEnabled(true);  // Permite edição
                    }
                } else {
                    // Se o bloqueio de edição de preço estiver inativo, segue o comportamento original
                    if (preco.equals("0,00")) {
                        etPreco.setEnabled(true);  // Permite edição
                        etPreco.setText("0,00");
                    } else {
                        etPreco.setText(preco);  // Preenche o campo com o preço retornado
                        etPreco.setEnabled(true);  // Permite edição, mesmo se for preço fixo
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


       /* spProduto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //
                String preco = bd.getMargemCliente(spProduto.getSelectedItem().toString(), id_cliente);
                boolean isPrecoFixo = bd.isPrecoFixo();
                if (preco.equals("0,00")) {
                    etPreco.setEnabled(true);
                    etPreco.setText("0,00");
                } else {
                    etPreco.setText(preco);
                    etPreco.setEnabled(!isPrecoFixo); // Bloqueia a edição se o preço for fixo
                   // Toast.makeText(Vendas.this, "PREÇO FIXO ENCONTRADO BLOQUEANDO EDIÇAO!.", Toast.LENGTH_SHORT).show();

                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });*/


        /*String[] somar = {"4.0", "3.0"};
        String[] subtrair = {"4.0", "3.0"};
        String[] multiplicar = {"4.0", "3.0"};
        String[] dividir = {"4.0", "3.0"};
        String[] comparar = {"4.0", "3.0"};

        classAuxiliar.somar(somar);
        classAuxiliar.subitrair(subtrair);
        classAuxiliar.multiplicar(multiplicar);
        classAuxiliar.dividir(dividir);
        classAuxiliar.comparar(comparar);*/
    }

    private void ShowMsgToast(String msg) {
        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    /************* INICIAR VENDA PRINCIPAL TABELA VENDAS APP *********************/

    private void inicializarVendaPrincipal() {
        // Armazenar dados em variáveis temporárias
        String codigoVenda = String.valueOf(id); // CODIGO_VENDA
        String codigoClienteVenda = id_cliente; // CODIGO_CLIENTE_VENDA
        String unidadeVenda = prefs.getString("unidade", ""); // UNIDADE_VENDA
        String produtoVenda = spProduto.getSelectedItem().toString(); // PRODUTO_VENDA
        String dataMovimento = classAuxiliar.formatarData(prefs.getString("data_movimento", "")); // DATA_MOVIMENTO
        String quantidadeVenda = etQuantidade.getText().toString(); // QUANTIDADE_VENDA
        String precoUnitario = ""; // PRECO_UNITARIO (vazio por enquanto)
        String valorTotal = ""; // VALOR_TOTAL (vazio por enquanto)
        String nomeVendedor = prefs.getString("nome_vendedor", "app"); // VENDEDOR_VENDA
        String statusAutorizacaoVenda = "0"; // STATUS_AUTORIZACAO_VENDA
        String entregaFuturaVenda = "0"; // ENTREGA_FUTURA_VENDA
        String entregaFuturaRealizada = "0"; // ENTREGA_FUTURA_REALIZADA
        String usuarioAtual = prefs.getString("usuario_atual", "app"); // USUARIO_ATUAL
        String dataCadastro = classAuxiliar.inserirDataAtual(); // DATA_CADASTRO
        String idVendaApp = String.valueOf(prefs.getInt("id_venda_app", 1)); // id_venda_app

        // Adiciona a venda no banco de dados
        bd.addVenda(new VendasDomain(
                codigoVenda,
                codigoClienteVenda,
                unidadeVenda,
                produtoVenda,
                dataMovimento,
                quantidadeVenda,
                precoUnitario,
                valorTotal,
                nomeVendedor,
                statusAutorizacaoVenda,
                entregaFuturaVenda,
                entregaFuturaRealizada,
                usuarioAtual,
                dataCadastro,
                idVendaApp,
                "0", // Campo adicional
                "" // Campo adicional
        ));

        // Log das informações
        Log.d("InicializarVenda", "Venda principal criada com as seguintes informações:");
        Log.d("InicializarVenda", "Código Venda: " + codigoVenda);
        Log.d("InicializarVenda", "Código Cliente Venda: " + codigoClienteVenda);
        Log.d("InicializarVenda", "Unidade Venda: " + unidadeVenda);
        Log.d("InicializarVenda", "Produto Venda: " + produtoVenda);
        Log.d("InicializarVenda", "Data Movimento: " + dataMovimento);
        Log.d("InicializarVenda", "Quantidade Venda: " + quantidadeVenda);
        Log.d("InicializarVenda", "Preço Unitário: " + precoUnitario);
        Log.d("InicializarVenda", "Valor Total: " + valorTotal);
        Log.d("InicializarVenda", "Nome Vendedor: " + nomeVendedor);
        Log.d("InicializarVenda", "Status Autorização Venda: " + statusAutorizacaoVenda);
        Log.d("InicializarVenda", "Entrega Futura Venda: " + entregaFuturaVenda);
        Log.d("InicializarVenda", "Entrega Futura Realizada: " + entregaFuturaRealizada);
        Log.d("InicializarVenda", "Usuário Atual: " + usuarioAtual);
        Log.d("InicializarVenda", "Data Cadastro: " + dataCadastro);
        Log.d("InicializarVenda", "ID Venda App: " + idVendaApp);
    }


    private  void obterDados(){
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {

                //SE A VENDA FOR NOVA
                if (id != 0){
                    //(params.getString("id_venda").equals("")) {
                    Log.d("Venda", "Iniciando uma nova venda.");


                    id_cliente = params.getString("codigo");
                    nome_cliente = params.getString("nome");
                    latitude_cliente = params.getString("latitude_cliente");
                    longitude_cliente = params.getString("longitude_cliente");
                    saldo = params.getString("saldo");
                    cpfcnpj = params.getString("cpfcnpj");
                    endereco = params.getString("endereco");
                    editandoVenda = "";


                    // Log dos dados para verificar se estão corretos
                    Log.d("DadosCliente", "ID Cliente: " + id_cliente);
                    Log.d("DadosCliente", "Nome Cliente: " + nome_cliente);
                    Log.d("DadosCliente", "Latitude: " + latitude_cliente);
                    Log.d("DadosCliente", "Longitude: " + longitude_cliente);
                    Log.d("DadosCliente", "Saldo: " + saldo);
                    Log.d("DadosCliente", "CPF/CNPJ: " + cpfcnpj);
                    Log.d("DadosCliente", "Endereço: " + endereco);
                    Log.d("DadosCliente", "Editando: " + editandoVenda);

                    // **Aqui inserimos a verificação e o armazenamento do limite de crédito**
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    int limiteCreditoPrimario = dbHelper.getLimiteCreditoCliente(id_cliente);
                    // Checa se o limite primário já foi definido e armazena o valor apropriado
                    if (creditoPrefs.getLimitePrimario() == null || creditoPrefs.getLimitePrimario().isEmpty()) {
                        // Se o limite primário ainda não foi definido, define-o com o valor atual do banco
                        creditoPrefs.setLimitePrimario(String.valueOf(limiteCreditoPrimario));
                        Log.d("LIMITE PRIMÁRIO", "Definindo limite primário de crédito para o cliente " + id_cliente + ": " + limiteCreditoPrimario);
                    } else {
                        // Se o limite primário já estiver definido, atualiza o limite de crédito original
                        creditoPrefs.setLimiteCreditoOriginal(String.valueOf(limiteCreditoPrimario));
                        Log.d("LIMITE ORIGINAL", "Atualizando limite de crédito original para o cliente " + id_cliente + ": " + limiteCreditoPrimario);
                    }

                }
                //SE FOR EDITAR A ÚLTIMA VENDA REALIZADA
                else {
                    Log.d("CAIU NA EDIÇAO", "obterDados: NAO E VENDA NOVA ");

                   /* Log.d("Venda", "Editando uma venda existente.");
                    //
                    id = Integer.parseInt(params.getString("id_venda"));
                    id_venda_app = Integer.parseInt(params.getString("id_venda_app"));
                    ed.putInt("id_venda_app", id_venda_app).apply();


                    id_cliente = params.getString("codigo");
                    nome_cliente = params.getString("nome");
                    latitude_cliente = params.getString("latitude_cliente");
                    longitude_cliente = params.getString("longitude_cliente");
                    saldo = params.getString("saldo");
                    cpfcnpj = params.getString("cpfcnpj");
                    endereco = params.getString("endereco");
                    editandoVenda = params.getString("editar");*/


                }


                //
                getSupportActionBar().setTitle("Data Mov. " + classAuxiliar.exibirDataAtual());

                //
                String nomeCliente = classAuxiliar.maiuscula1(nome_cliente.toLowerCase());
                getSupportActionBar().setSubtitle(nomeCliente);
                consultarInadimplencia();
            }
        }

    }

    /************ METODO ORIGINAL UNICO PRODUTO POR VENDA  ************/

    /*ADICIONAR VENDAS
    private void addVenda() {

        //
        String valorUnit = String.valueOf(classAuxiliar.converterValores(etPreco.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, etQuantidade.getText().toString()};
        String total = String.valueOf(classAuxiliar.multiplicar(multiplicar));


        //SETA OS DADOS PARA ENVIAR AO EMISSOR
        produto_emissor = spProduto.getSelectedItem().toString();
        quantidade_emissor = etQuantidade.getText().toString();
        valor_unit_emissor = valorUnit;

        //
        listaVendas = bd.getVendasCliente(prefs.getInt("id_venda_app", 1));
        adapter = new VendasAdapter(this, listaVendas);
        rvVendas.setAdapter(adapter);



        textTotalItens.setText(String.valueOf(listaVendas.size()));

        String v = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalVenda(String.valueOf(id_venda_app))));
        txtTotalVenda.setText(v);
        Log.e("TOTAL", v);
        Log.e("TOTAL", "VENDAS: " + bd.getValorTotalVenda(String.valueOf(id_venda_app)));

        etQuantidade.setText("");
        etPreco.setText(R.string.zeros);
        spProduto.requestFocus();



        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }*/

    /******** ATUALIZAÇAO DO CAMPO DE ENTREGA-FUTURA DENTRO DA VENDA *******/

    private void AttVendafutura(){

        // Recupera o valor de entrega futura salvo no SharedPreferences
        int entregaFutura = prefs.getInt("entrega_futura_venda", 0);
        Log.d("MARCAÇAO SALVA", "AttVendafutura: ENTREGA FUTURA  " + entregaFutura );

        // Atualiza o campo entrega_futura_venda apenas para a venda recém-adicionada (usando id_venda_app)
        int codigoVendaRecemAdicionada = prefs.getInt("id_venda", 0); // Obtenha o ID da venda recém-adicionada
        Log.d("ATUALIZAR NA VENDA ", "AttVendafutura: ID PRA USAR NA ATT " +codigoVendaRecemAdicionada);
        int resultado = bd.atualizarEntregaFutura(entregaFutura, codigoVendaRecemAdicionada);

        // Verifica se a atualização foi bem-sucedida
        if (resultado > 0) {
            Log.d("UpdateLog", "Campo entrega_futura_venda atualizado com sucesso para o código de venda: " + codigoVendaRecemAdicionada);
        } else {
            Log.d("UpdateLog", "Falha ao atualizar o campo entrega_futura_venda.");
        }

    }

    /***************** INSERIR PRODUTOS NO PEDIDO **************************/

    private void adicionarProdutoAoPedido() {
        // Captura as informações de produto
        String id_venda_app = String.valueOf(prefs.getInt("id_venda_app", 0));
        String produto = spProduto.getSelectedItem().toString();
        String quantidade = etQuantidade.getText().toString();
        quantidade_emissor = quantidade;
        Log.d("pegando QUANTIDADE", "adicionarProdutoAoPedido: PEGANDO A QUANITDADE " + quantidade_emissor);
        String precoUnitario = String.valueOf(classAuxiliar.converterValores(etPreco.getText().toString()));
        valor_unit_emissor = precoUnitario;
        Log.d("pegando PREÇO UNIT", "adicionarProdutoAoPedido: PEGANDO PREÇO UNITARIO " + quantidade_emissor);

        long resultadoInsercao = bd.addProdutoVenda(produto, Integer.parseInt(quantidade), Double.parseDouble(precoUnitario), String.valueOf(id_venda_app));
        if (resultadoInsercao != -1) {
            Log.d("Inserção", "Produto inserido com sucesso na tabela produtos_vendas_app, ID da linha: " + resultadoInsercao + ", ID da venda: " + id_venda_app + ", Produto: " + produto);
            // listarItensVendas();
            atualizarListaProdutos();
            resetarCamposVenda();
            atualizarListaProdutos();
            vendaAlterada = true;
        } else {
            Log.e("Inserção", "Falha ao inserir produto na tabela produtos_vendas_app.");
        }
    }




    private String calcularTotalVenda(String valorUnit, String quantidade) {
        String[] multiplicar = {valorUnit, quantidade};
        return String.valueOf(classAuxiliar.multiplicar(multiplicar));
    }

    /************** ATUALIZAR LISTA VISUAL DOS PRODUTOS ******************/

    private void atualizarListaProdutos() {
        // Recupera o código da venda atual
        String codigoVendaApp = String.valueOf(prefs.getInt("id_venda_app", 0));
        Log.d("ID PRA ATT", "atualizarListaProdutos: ID PRA ATUALIZAR TABELA EDIÇAO " + codigoVendaApp);
        ArrayList<ProdutoEmissor> produtosAtualizados = bd.getProdutosVenda(codigoVendaApp);

        // Atualiza listaProdutosVenda, verificando duplicações e inserindo ou atualizando produtos conforme necessário
        listaProdutosVenda.clear();
        listaProdutosVenda.addAll(produtosAtualizados);

        // Verifica e configura o adaptador para o RecyclerView
        if (adapter == null) {
            adapter = new ProdutosAdapter(this, listaProdutosVenda, codigoVendaApp);
            rvVendas.setAdapter(adapter);
            rvVendas.setLayoutManager(new LinearLayoutManager(this));
            vendaAlterada = true ;
        } else {
            adapter.notifyDataSetChanged();
        }

        // Calcula o total de itens e o valor total da venda
        int totalItens = 0;
        double totalVenda = 0.0;
        for (ProdutoEmissor produto : listaProdutosVenda) {
            try {
                int quantidade = Integer.parseInt(produto.getQuantidade());
                double valorUnitario = Double.parseDouble(produto.getValorUnitario());
                totalItens += quantidade;
                totalVenda += quantidade * valorUnitario;
            } catch (NumberFormatException e) {
                Log.e("atualizarListaProdutos", "Erro ao converter quantidade ou valor unitário", e);
            }
        }

        // Atualiza a exibição dos totais de itens e vendas
        int finalTotalItens = totalItens;
        double finalTotalVenda = totalVenda;
        runOnUiThread(() -> {
            textTotalItens.setText(String.valueOf(finalTotalItens));
            txtTotalVenda.setText(classAuxiliar.maskMoney(new BigDecimal(finalTotalVenda)));
            adapter.notifyDataSetChanged();
        });

        // Log dos totais calculados para depuração
        Log.d("atualizarListaProdutos", "Total de itens atualizado: " + totalItens);
        Log.d("atualizarListaProdutos", "Total de vendas atualizado: " + totalVenda);
    }


   /* private void atualizarListaProdutos() {
       String codigoVendaApp = String.valueOf(prefs.getInt("id_venda_app", 0));
        Log.d("ID PRA ATT", "atualizarListaProdutos: ID PRA ATUALIZAR TABELA EDIÇAO " + codigoVendaApp);


        // Limpa a lista de produtos para evitar duplicações
        //listaProdutosVenda.clear();
        Log.d("atualizarListaProdutos", "Lista de produtos limpa. Tamanho atual: " + listaProdutosVenda.size());

        // Adiciona produtos atualizados para o código de venda atual
        ArrayList<ProdutoEmissor> produtosAtualizados = bd.getProdutosVenda(codigoVendaApp);
        Log.d("ID PRA ATT LISTA", "atualizarListaProdutos: ID PRA ATT DA LISTA DE PRODSUTOS " + codigoVendaApp);
        listaProdutosVenda.addAll(produtosAtualizados);
        Log.d("atualizarListaProdutos", "Produtos atualizados adicionados. Novo tamanho da lista: " + listaProdutosVenda.size());

        // Verifique se o adaptador está nulo e atualize ou configure o adaptador
        if (adapter == null) {
            adapter = new ProdutosAdapter(this, listaProdutosVenda,codigoVendaApp);
            rvVendas.setAdapter(adapter);
            Log.d("atualizarListaProdutos", "Adapter de produtos inicializado.");
        } else {
            adapter.notifyDataSetChanged();
            Log.d("atualizarListaProdutos", "Adapter de produtos notificado sobre a alteração de dados.");
        }

        // Atualiza o total de itens e valor total
        textTotalItens.setText(String.valueOf(listaProdutosVenda.size()));
        double totalVenda = bd.listarProdutosVendasApp(codigoVendaApp);
        String valorFormatado = classAuxiliar.maskMoney(new BigDecimal(totalVenda));
        txtTotalVenda.setText(valorFormatado);

        Log.d("atualizarListaProdutos", "Total de vendas formatado e atualizado: " + valorFormatado);
    }*/


    private void atualizarResumoVenda() {
        textTotalItens.setText(String.valueOf(listaVendas.size()));
        String v = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalVenda(String.valueOf(id_venda_app))));
        txtTotalVenda.setText(v);
        Log.e("TOTAL", v);
        Log.e("TOTAL", "VENDAS: " + bd.getValorTotalVenda(String.valueOf(id_venda_app)));
    }

    private void resetarCamposVenda() {
        etQuantidade.setText("");
        //  etPreco.setText(R.string.zeros);
        spProduto.requestFocus();
    }

    private void esconderTeclado() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("Teclado", "Erro ao esconder o teclado: ", e);
        }
    }

    /******************* ATUALIZAR TABELA PRODUTO VENDAS APP DURANTE O PEDIDO ***********/

    private void Atualizatabela() {
        // Log para verificar se a flag foi salva corretamente
        boolean vendaAtualizada = prefs.getBoolean("vendaAtualizada", false);
        Log.d("LOG ATT TABELA", "Flag vendaAtualizada salva no SharedPreferences: " + vendaAtualizada);
        Log.d("LOG ATT TABELA", "Flag vendaEditada em memória: " + vendaAlterada);

        if (!vendaAtualizada || !vendaAlterada)  {
            Log.d("PULANDO ATT", "Atualizatabela: NAO HOUVVE ATERAÇOES EM NEHUM LUGAR  ");

        }else{

            // Recupera o ID da venda atual
            int idVendaAtual = prefs.getInt("id_venda_app", 0);

            // Converte o valor unitário do produto para double usando BigDecimal
            double valorUnitario = new BigDecimal(valor_unit_emissor.replace(",", ".").trim()).doubleValue();

            // Remove caracteres indesejados antes de converter o valor total da venda
            String totalVendaString = txtTotalVenda.getText().toString().replace("R$", "").replaceAll("[^\\d.]", "").trim();
            double totalVenda = new BigDecimal(totalVendaString).doubleValue();

            // Usa o método obterTotalItensPedido para calcular o total de itens como int
            int totalItens = obterTotalItensPedido();

            // Chama o método para atualizar os valores da venda no banco de dados
            bd.atualizarValoresVenda(idVendaAtual, valorUnitario, totalVenda, totalItens);
            // vendaAlterada = true;

            // Log para verificação dos dados
            Log.d("Atualizatabela", "Dados atualizados na tabela para ID: " + idVendaAtual + ", Valor Unitário: " + valorUnitario + ", Valor Total da Venda: " + totalVenda + ", Quantidade Total: " + totalItens);


        }


    }


    private int obterTotalItensPedido() {
        int totalItens = 0;

        // Recupera o código da venda atual
        String codigoVendaApp = String.valueOf(id_venda_app);

        // Recupera a lista de produtos para a venda atual
        ArrayList<ProdutoEmissor> produtosVenda = bd.getProdutosVenda(codigoVendaApp);

        // Soma as quantidades de cada produto convertendo de String para int
        for (ProdutoEmissor produto : produtosVenda) {
            try {
                totalItens += Integer.parseInt(produto.getQuantidade());
            } catch (NumberFormatException e) {
                Log.e("obterTotalItensPedido", "Erro ao converter quantidade para inteiro: " + produto.getQuantidade(), e);
            }
        }

        Log.d("TotalItensPedido", "Total de itens no pedido atual: " + totalItens);

        return totalItens;
    }

    /******************** CARREGA OS DADOS DA VENDA PRA EDIÇAO *****************/

    // Método para obter e logar todos os dados completos da venda
    private void logarDadosCompletosVenda() {

        int idVendaAppLocal = prefs.getInt("id_venda_app", 0);
        Log.d("LOGAR DADOS ", "logarDadosCompletosVenda: ID RECUPERADO DO SHARE " + idVendaAppLocal);

        // Chama o método para obter os dados completos da venda
        DadosCompletosDomain dadosCompletos = bd.obterDadosCompletosVenda(idVendaAppLocal);
        Log.d("DADOS COMPLETOS", "logarDadosCompletosVenda: CODIGO PARA BUSCA " + idVendaAppLocal);

        // Logando todos os dados da venda principal
        Log.d("DadosCompletosVenda", "Código Venda: " + dadosCompletos.getCodigoVenda());
        Log.d("DadosCompletosVenda", "Código Venda App: " + dadosCompletos.getCodigoVendaApp());
        Log.d("DadosCompletosVenda", "Código Cliente: " + dadosCompletos.getCodigoCliente());
        Log.d("DadosCompletosVenda", "Nome Cliente: " + dadosCompletos.getNomeCliente());
        Log.d("DadosCompletosVenda", "Unidade Venda: " + dadosCompletos.getUnidadeVenda());
        Log.d("DadosCompletosVenda", "Produto Venda: " + dadosCompletos.getProdutoVenda());
        Log.d("DadosCompletosVenda", "Data Movimento: " + dadosCompletos.getDataMovimento());
        Log.d("DadosCompletosVenda", "Quantidade Venda: " + dadosCompletos.getQuantidadeVenda());
        Log.d("DadosCompletosVenda", "Preço Unitário: " + dadosCompletos.getPrecoUnitario());
        Log.d("DadosCompletosVenda", "Valor Total: " + dadosCompletos.getValorTotal());
        Log.d("DadosCompletosVenda", "Vendedor Venda: " + dadosCompletos.getVendedorVenda());
        Log.d("DadosCompletosVenda", "Status Autorização Venda: " + dadosCompletos.getStatusAutorizacaoVenda());
        Log.d("DadosCompletosVenda", "Entrega Futura Venda: " + dadosCompletos.getEntregaFuturaVenda());
        Log.d("DadosCompletosVenda", "Entrega Futura Realizada: " + dadosCompletos.getEntregaFuturaRealizada());
        Log.d("DadosCompletosVenda", "Usuário Atual: " + dadosCompletos.getUsuarioAtual());
        Log.d("DadosCompletosVenda", "Data Cadastro: " + dadosCompletos.getDataCadastro());
        Log.d("DadosCompletosVenda", "Venda Finalizada App: " + dadosCompletos.getVendaFinalizadaApp());
        Log.d("DadosCompletosVenda", "Chave Importação: " + dadosCompletos.getChaveImportacao());


        // Configuração do RecyclerView e Adapter
        rvVendas = findViewById(R.id.rvVendas);
        String codigoVendaAppString = String.valueOf(idVendaAppLocal);
        rvVendas.setLayoutManager(new LinearLayoutManager(Vendas.this));
        adapter = new ProdutosAdapter(this, listaProdutosVenda,codigoVendaAppString);
        rvVendas.setAdapter(adapter);


        // Popula a lista de produtos do adapter com os produtos recuperados
        listaProdutosVenda.clear();
        listaProdutosVenda.addAll(dadosCompletos.getProdutosVenda());
        Log.d("EXIBINDO ", "logarDadosCompletosVenda: LISTA EXIBIDA " + listaProdutosVenda);

        // Notifica o adapter para atualizar a exibição dos produtos
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e("Adapter", "Adapter não foi inicializado. Verifique a configuração do RecyclerView.");
        }


        // Logando a lista de produtos da venda
        for (ProdutoEmissor produto : dadosCompletos.getProdutosVenda()) {
            Log.d("ProdutoVenda", "Nome Produto: " + produto.getNome());
            Log.d("ProdutoVenda", "Quantidade: " + produto.getQuantidade());
            Log.d("ProdutoVenda", "Preço Unitário: " + produto.getValorUnitario());
            //  Log.d("ProdutoVenda", "Código Venda App: " + produto.getCodigoVendaApp());
        }

        // Logando o total de itens da venda
        Log.d("DadosCompletosVenda", "Total de Itens na Venda: " + dadosCompletos.getTotalItens()) ;
        textTotalItens.setText(String.valueOf(dadosCompletos.getTotalItens()));
        double totalVenda = bd.listarProdutosVendasApp(codigoVendaAppString);
        String valorFormatado = classAuxiliar.maskMoney(new BigDecimal(totalVenda));
        txtTotalVenda.setText(valorFormatado);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // listarItensVendas();
    }

    /*public void listarItensVendas() {
        try {
            // **
            listaVendas = bd.getVendasCliente(prefs.getInt("id_venda_app", 1));
            adapter = new VendasAdapter(this, listaVendas);
            adapter.notifyDataSetChanged();
            rvVendas.setAdapter(adapter);

            // **
            textTotalItens.setText(String.valueOf(listaVendas.size()));
            String v = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalVenda(String.valueOf(id_venda_app))));
            txtTotalVenda.setText(v);
            Log.e("TOTAL", v);
            Log.e("TOTAL", "VENDAS: " + bd.getValorTotalVenda(String.valueOf(id_venda_app)));

            Log.e("LOG", "TESTE");

        } catch (Exception e) {
            Log.i("Financeiro", e.getMessage());
        }
    }*/

    public class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[^0-9]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    @Override
    protected void onDestroy() {

        ArrayList<Integer> codigosVenda = bd.listarEntregasFuturas();
        Log.d("CODIGOS DE VENDA", "Códigos de venda ao finalizar a atividade: " + codigosVenda.toString());
        //bd.close();
        super.onDestroy();
    }

    private void cancelarVenda() {
        //  SE ESTIVER EDITANDO NÃO CANCELAR A VENDA
        if (editandoVenda.equalsIgnoreCase("sim")) {
            finish();
        } else {

            //Cria o gerador do AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.logosiac);
            //define o titulo
            builder.setTitle("Atenção");
            //define a mensagem
            builder.setMessage("Você Deseja Realmente Cancelar Esta Venda?");
            //define um botão como positivo
            builder.setPositiveButton("Sim", (arg0, arg1) -> {

                // Excluir todos os produtos da venda atual do banco de dados
                int linhasAfetadas = bd.deleteProdutosPorVenda(String.valueOf(id_venda_app));

                // Log para verificar a exclusão
                if (linhasAfetadas > 0) {
                    Log.d("Cancelamento", "Venda cancelada com sucesso! " + linhasAfetadas + " produtos foram excluídos.");
                    Toast.makeText(Vendas.this, "Venda cancelada com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Log.d("Cancelamento", "Nenhum produto foi excluído. Verifique o código de venda.");
                }


                /* Restaurar limite de crédito ao cancelar uma venda a prazo
                String valorAprazo = creditoPrefs.getValorAprazo();
                if (valorAprazo != null && !valorAprazo.isEmpty()) {
                    BigDecimal valorRestituido = new BigDecimal(valorAprazo);
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    dbHelper.restituirLimiteCreditoCliente(id_cliente, valorRestituido); // Use id_cliente aqui
                    creditoPrefs.clear(); // Limpa as informações armazenadas após o cancelamento
                }*/




                //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();
                int i = bd.deleteVenda(prefs.getInt("id_venda_app", 0));
                if (i != 0) {
                    Toast.makeText(Vendas.this, "Esta Venda foi Cancelada!", Toast.LENGTH_LONG).show();
                    finish();
                } else if (textTotalItens.getText().toString().equals("0")) {
                    finish();
                }
            });
            //define um botão como negativo.
            builder.setNegativeButton("Não", (arg0, arg1) -> {
                //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            });
            //cria o AlertDialog
            alerta = builder.create();
            //Exibe alerta
            alerta.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vendas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                cancelarVenda();
                break;
            case R.id.action_cancelar_venda:
                cancelarVenda();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        cancelarVenda();
        //super.onBackPressed();
    }

    /*****************CONSULTA E TRATAMENTO DA INADIMPLENCIA*******************/

    private void consultarInadimplencia() {
        Log.d("Inadimplencia", "Verificando inadimplência para o cliente: " + nome_cliente + " (ID: " + id_cliente + ")");
        boolean isInadimplente = verificarInadimplencia(id_cliente);


        // Salvar o status de inadimplência no SharedPreferences
        ClienteInadimplenciaHelper inadimplenciaHelper = new ClienteInadimplenciaHelper(this);
        inadimplenciaHelper.salvarStatusInadimplencia(id_cliente, isInadimplente);

        if (isInadimplente) {
            boolean isBloqueioInadimplente = bd.isInadimplenteBloqueado();
            Log.d("Inadimplencia", "Bloqueio por inadimplência ativo: " + isBloqueioInadimplente);

            if (isBloqueioInadimplente) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Cliente Inadimplente");
                builder.setMessage("O cliente " + nome_cliente + " está inadimplente.");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();

                    /* Navega para a atividade Principal e exibe o HomeFragment
                    Intent intent = new Intent(Vendas.this, Principal2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("navigateToHome", true);
                    startActivity(intent);
                    finish();*/
                });
                AlertDialog alerta = builder.create();
                alerta.setCancelable(false);
                alerta.show();
                // builder.show();
            } else {
                Log.d("Inadimplencia", "O cliente está inadimplente, mas o bloqueio por inadimplência não está ativo.");
                // Segue o fluxo normal se o bloqueio não está ativo
            }
        } else {
            Log.d("Inadimplencia", "O cliente " + nome_cliente + " não está inadimplente.");
            // Segue o fluxo normal se o cliente não estiver inadimplente
        }
    }

    private boolean verificarInadimplencia(String clienteId) {
        Log.d("Inadimplencia", "ID do cliente recebido: " + clienteId);
        ArrayList<FinanceiroReceberClientes> contasReceber = bd.getContasReceberCliente(clienteId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date dataAtual = new Date();
        Log.d("Inadimplencia", "Data atual: " + sdf.format(dataAtual));

        boolean isInadimplente = false;

        for (FinanceiroReceberClientes conta : contasReceber) {
            String vencimento = conta.getVencimento_financeiro();
            Log.d("Inadimplencia", "Data de vencimento da conta: " + vencimento);
            try {
                Date dataVencimento = sdf.parse(vencimento);
                if (dataVencimento != null) {
                    Log.d("Inadimplencia", "Data de vencimento parsed: " + sdf.format(dataVencimento));
                    if (dataVencimento.before(dataAtual)) {
                        Log.d("Inadimplencia", "Conta vencida encontrada. Cliente inadimplente.");
                        isInadimplente = true;
                        break;
                    } else {
                        Log.d("Inadimplencia", "Conta ainda não vencida.");
                    }
                }
            } catch (ParseException e) {
                Log.e("Inadimplencia", "Erro ao analisar a data de vencimento: " + vencimento, e);
            }
        }

        if (isInadimplente) {
            boolean isBloqueioInadimplente = bd.isInadimplenteBloqueado();
            if (isBloqueioInadimplente) {
                Log.d("Inadimplencia", "Bloqueio por inadimplência ativo.");
                return true; // Cliente está inadimplente e o bloqueio está ativo
            } else {
                Log.d("Inadimplencia", "Bloqueio por inadimplência não está ativo.");
            }
        } else {
            Log.d("Inadimplencia", "Nenhuma conta vencida encontrada. Cliente não está inadimplente.");
        }

        return false; // Cliente não está inadimplente ou bloqueio não está ativo
    }

}