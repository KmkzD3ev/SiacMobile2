package br.com.zenitech.siacmobile;

import static br.com.zenitech.siacmobile.Configuracoes.getApplicationName;
import static stone.utils.GlobalInformations.bluetoothAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.maps.android.SphericalUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import br.com.zenitech.siacmobile.adapters.FinanceiroVendasAdapter;
import br.com.zenitech.siacmobile.domains.Conta;
import br.com.zenitech.siacmobile.domains.FinanceiroReceberClientes;
import br.com.zenitech.siacmobile.domains.FinanceiroVendasDomain;
import br.com.zenitech.siacmobile.domains.PosApp;
import br.com.zenitech.siacmobile.domains.VendasPedidosComProdutosDomain;
import br.com.zenitech.siacmobile.interfaces.ILogin;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import stone.application.StoneStart;
import stone.user.UserModel;
import stone.utils.Stone;

public class FinanceiroDaVenda extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    protected static final int REQUEST_CHECK_SETTINGS = 1;
    private static final Logger log = LoggerFactory.getLogger(FinanceiroDaVenda.class);
    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private CreditoPrefs creditoPrefs;
    private boolean vendaEditada;

    public static String totalFinanceiro;
    private LinearLayoutCompat bandPrazo;
    public static TextView txtTotalFinanceiro;
    public static TextView txtTotalItemFinanceiro;
    private ArrayList<String> listaFormasPagamentoCliente, listaBandeiras, listaParcelas;
    private DatabaseHelper bd;
    private Spinner spFormasPagamentoCliente, spBandeira, spParcela;
    public static String codigo_cliente = "";
    public static String nomeCliente = "";
    public static String cpfcnpjCliente = "";
    public static String enderecoCliente = "";
    public static EditText txtDocumentoFormaPagamento;
    public static EditText txtVencimentoFormaPagamento, txtValorFormaPagamento;
    public static LinearLayout bgTotal;
    public EditText txtNotaFiscal;
    LinearLayout formFinan;
    private  Boolean iSPromissoria;

    private ArrayList<String> formasPagamentoPrazo; // Array com a s formas de pagamento A_PRAZO
    private ArrayList<String> valoresCompra;  // Armazena o valor da compra quando formas de pagamento A_PRAZO e Identificada
    private BigDecimal valorTotalAPrazo = BigDecimal.ZERO; // Variável para armazenar o valor total das formas de pagamento a prazo




    //LISTAR VENDAS
    private ArrayList<FinanceiroVendasDomain> listaFinanceiroCliente;
    private FinanceiroVendasAdapter adapter;
    private RecyclerView rvFinanceiro;

    private Button btnAddF, btnPagamento;

    int id = 1;
    int id_venda_app = 1;
    private String total_venda = "0.0";
    private ClassAuxiliar classAuxiliar;

    TextInputLayout tilDocumento, tilVencimento, tilNotaFiscal;
    private AlertDialog alerta;

    //DADOS PARA PASSAR AO EMISSOR WEB
    private String produto_emissor;
    private String quantidade_emissor;
    private String valor_unit_emissor;
    private Context context;

    // DADOS DE CORDENADAS *********
    double coord_latitude = 0.0;
    double coord_longitude = 0.0;
    String precisao = "";
    GPStracker coord;
    LatLng posicaoInicial;
    LatLng posicaiFinal;
    double distance;

    // DADOS CLIENTE
    double coordCliLat = 0.0, coordCliLon = 0.0;
    // DADOS DE CORDENADAS *********

    private SpotsDialog dialog;

    private VerificarOnline verificarOnline;



    private String vencimentoTemp;

    //UnidadesDomain unidades;
    Configuracoes configuracoes;

    PosApp posApp;
    String nDoc = "";

    //
    ArrayAdapter adapterSpBandeira, adapterSpParcela;
    private ArrayList<String> formasPagamento = new ArrayList<>(); //ARRAY PARA FORMAS DE PAGAMENTO
    private CheckBox checkbox_confirmar;



    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financeiro_da_venda);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Inicialize o DatabaseHelper
        bd = new DatabaseHelper(this);
        // Inicializando o CheckBox
        checkbox_confirmar = findViewById(R.id.checkbox_confirmar);
        creditoPrefs = new CreditoPrefs(this);
        vendaEditada = false;
        // Instanciando o SharedPreferences
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        id_venda_app = prefs.getInt("id_venda_app",0);
        Log.d("DEBUG_ID_VENDA_APP", "Valor de id_venda_app: " + id_venda_app);

        bd = new DatabaseHelper(this);
        // Chamada do método para exibir logs no logcat
        ArrayList<VendasPedidosComProdutosDomain> listaVendasComProdutos = bd.getRelatorioVendasComProdutos();



        // Obtendo os dados da última venda do cliente
        String[] dados_venda = bd.getUltimaVendasCliente();

         // Verificando se o array não está vazio
        if (dados_venda != null && dados_venda.length > 0) {
            // Iterando sobre os dados e logando cada elemento
            for (String dado : dados_venda) {
                Log.d("DADOS_VENDA", dado);
            }
        } else {
            Log.d("DADOS_VENDA", "Nenhuma venda encontrada para o cliente.");

        }



        // Inicializações e configurações
        spFormasPagamentoCliente = findViewById(R.id.spFormasPagamentoCliente);
        codigo_cliente = getIntent().getStringExtra("codigo_cliente");

        carregarFormasDePagamento();





        //
        formasPagamentoPrazo = new ArrayList<>();
        valoresCompra = new ArrayList<>();
        creditoPrefs = new CreditoPrefs(this);
        iSPromissoria = false;


        classAuxiliar = new ClassAuxiliar();
        context = this;
        coord = new GPStracker(context);
        // VERIFICA SE O GPS ESTÁ ATIVO
        if (coord.isGPSEnabled()) {
            coord.getLocation();
            //gps.getLatLon();
        }
        verificarOnline = new VerificarOnline();
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .setCancelable(false)
                .build();

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        id = prefs.getInt("id_financeiro_venda", 1);

        //
        bd = new DatabaseHelper(this);
        posApp = bd.getPos();

        //
        bgTotal = findViewById(R.id.bgTotal);

        //
        bandPrazo = findViewById(R.id.bandPrazo);
        rvFinanceiro = findViewById(R.id.rvFinanceiro);
        rvFinanceiro.setLayoutManager(new LinearLayoutManager(this));

        //
        tilDocumento = findViewById(R.id.tilDocumento);
        tilVencimento = findViewById(R.id.tilVencimento);
        tilNotaFiscal = findViewById(R.id.tilNotaFiscal);

        //Total Venda: R$
        //txtNomeClienteFinanceiro = (TextView) findViewById(R.id.txtNomeClienteFinanceiro);
        txtTotalFinanceiro = findViewById(R.id.txtTotalFinanceiro);
        //
        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        txtValorFormaPagamento.addTextChangedListener(new FinanceiroDaVenda.MoneyTextWatcher(txtValorFormaPagamento));

        txtDocumentoFormaPagamento = findViewById(R.id.txtDocumentoFormaPagamento);
        txtNotaFiscal = findViewById(R.id.txtNotaFiscal);

        //
        txtVencimentoFormaPagamento = findViewById(R.id.txtVencimentoFormaPagamento);
        txtVencimentoFormaPagamento.setText(classAuxiliar.exibirDataAtual());
        txtVencimentoFormaPagamento.addTextChangedListener(classAuxiliar.maskData("##/##/####", txtVencimentoFormaPagamento));

        txtTotalItemFinanceiro = findViewById(R.id.txtTotalItemFinanceiro);


        //
        formFinan = findViewById(R.id.formFinan);
        btnAddF = findViewById(R.id.btnAddF);
        btnAddF.setOnClickListener(v -> {

            /*********** Captura o valor inserido no campo txtValorFormaPagamento **************/

            String valorInserido = txtValorFormaPagamento.getText().toString().replaceAll("[^\\d.,]", "").replace(",", ".").trim();
            String[] fPag1 = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");

            // Adiciona a forma de pagamento ao array de formas de pagamento
            formasPagamento.add(Arrays.toString(fPag1));

            // Log para verificar a forma de pagamento inserida
            Log.d("FORMAPAGAMENTO ADCIONADA", "Forma de pagamento adicionada ao array: " + Arrays.toString(fPag1));

            // Log do array completo para verificar todas as formas de pagamento
            Log.d("CONTEUDO FORMAS PAGAMENTO", "Conteúdo atual do array formasPagamento: " + formasPagamento.toString());


            //**************Se a forma de pagamento for "A PRAZO", adicionar o valor ao array valoresCompra**************//
// Se a forma de pagamento for "A PRAZO", verificar o limite de crédito
            if (fPag1.length > 1 && "A PRAZO".equals(fPag1[1])) {
                // Verifica o limite antes de adicionar a forma de pagamento
                if (!verificarLimiteCreditoPrazo()) {
                    // Exibir mensagem de limite insuficiente
                    //Toast.makeText(FinanceiroDaVenda.this, "Limite de crédito insuficiente para adicionar esta forma de pagamento a prazo.", Toast.LENGTH_LONG).show();

                    // Impedir que a forma de pagamento seja adicionada e que qualquer outra ação prossiga
                    return;
                } else {
                    // Se o limite de crédito estiver OK, prosseguir com a adição
                    valoresCompra.add(valorInserido);

                    // Log para verificação
                    Log.d("ValorAdicionado", "Valor adicionado ao array: " + valorInserido);

                    // Salvando as informações de forma de pagamento e valor no CreditoPrefs
                    creditoPrefs.setFormaPagamentoPrazo(spFormasPagamentoCliente.getSelectedItem().toString());
                    creditoPrefs.setValorAprazo(valorInserido);
                    creditoPrefs.setIdCliente(codigo_cliente);

                    // Logs para verificação
                    Log.d("CREDITOPREFS", "Forma de pagamento a prazo salva: " + creditoPrefs.getFormaPagamentoPrazo());
                    Log.d("CREDITOPREFS", "Valor a prazo salvo: " + creditoPrefs.getValorAprazo());
                    Log.d("CREDITOPREFS", "ID do cliente salvo: " + creditoPrefs.getIdCliente());
                }
            }

            //SE O USUÁRIO NÃO ADICIONAR NENHUM VALOR
            String val = classAuxiliar.soNumeros(txtValorFormaPagamento.getText().toString());
            /*if (txtValorFormaPagamento.getText().toString().equals("") ||
                    txtValorFormaPagamento.getText().toString().equals("R$0,00") ||
                    txtValorFormaPagamento.getText().toString().equals("R$0,00")
            ) {*/
            if (val.equalsIgnoreCase("") || val.equalsIgnoreCase("000")) {
                //
                Toast.makeText(FinanceiroDaVenda.this, "Adicione uma valor para esta forma de pagamento.", Toast.LENGTH_LONG).show();
            } else {
                //
                String[] fPag = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");

                //SE A FORMA DE PAGAMENTO FOR IGUAL A PRAZO VERIFICA O NÚMERO DO DOCUMENTO E O TIPO DE BAIXA
                if (fPag[1].equals("A PRAZO")) {

                    //SE O NÚMERO DO DOCUMENTO ESTIVER VÁSIO MOSTRA A MENSAGEM
                    if (txtDocumentoFormaPagamento.getText().toString().equals("") && fPag[2].equals("1")) {
                        //
                        Toast.makeText(FinanceiroDaVenda.this, "Para essa forma de pagamento o número do documento é obrigatório.", Toast.LENGTH_LONG).show();
                    }
                    //SE A BAIXA FOR MANUAL VERIFICA O CAMPO VENCIMENTO
                    else if (fPag[3].equals("1")) {

                        //SE O CAMPO VENCIMENTO FOR IGUAL A 00/00/0000 PEDE QUE INFORME A DATA DO VENCIMENTO
                        if (txtVencimentoFormaPagamento.getText().toString().equals("") || txtVencimentoFormaPagamento.getText().toString().equals("00/00/0000")) {
                            //
                            Toast.makeText(FinanceiroDaVenda.this, "Data do vencimento é obrigatório.", Toast.LENGTH_LONG).show();
                        } //ADICIONA VALOR AO FINANCEIRO
                        else {

                            if (!verificarLimiteCreditoPrazo()) {
                                Toast.makeText(FinanceiroDaVenda.this, "Limite de crédito insuficiente.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            addFinanceiro();
                        }
                    }
                    //ADICIONA VALOR AO FINANCEIRO
                    else {
                        addFinanceiro();
                    }
                }
                //ADICIONA VALOR AO FINANCEIRO
                else {
                    addFinanceiro();
                }
            }
        });
        //

        btnPagamento = findViewById(R.id.btnPagamento);
        btnPagamento.setOnClickListener(v -> {



            _salvarFinanceiro();


        });

        //LINHA FINAL

        //unidades = bd.getUnidade();


        //
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {

                codigo_cliente = params.getString("codigo_cliente");
                Log.d("CREDITO PREFS", "onCreate:ENVIANDO PRO SHARE " + codigo_cliente);


                if (!Objects.requireNonNull(params.getString("saldo")).equalsIgnoreCase("")) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Saldo: " + classAuxiliar.maskMoney(classAuxiliar.converterValores(params.getString("saldo"))));
                } else {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Financeiro");
                }
                //getSupportActionBar().setSubtitle("R$ " + params.getString("valorVenda"));// + "  " + prefs.getInt("id_venda_app", 1)

                //
                Log.i("Financeiro", Objects.requireNonNull(params.getString("latitude_cliente")));
                //nome_cliente
                codigo_cliente = params.getString("codigo_cliente");
                if (!Objects.requireNonNull(params.getString("latitude_cliente")).equalsIgnoreCase("") &&
                        !Objects.requireNonNull(params.getString("longitude_cliente")).equalsIgnoreCase("")
                ) {
                    coordCliLat = Double.parseDouble(Objects.requireNonNull(params.getString("latitude_cliente")));
                    coordCliLon = Double.parseDouble(Objects.requireNonNull(params.getString("longitude_cliente")));
                }

                //txtNomeClienteFinanceiro.setText(params.getString("nome_cliente"));
                txtTotalFinanceiro.setText(params.getString("valorVenda"));

                txtValorFormaPagamento.setText(params.getString("valorVenda"));

                //DADOS EMISSOR WEB
                produto_emissor = params.getString("produto");
                quantidade_emissor = params.getString("quantidade");
                valor_unit_emissor = params.getString("valor_unit");

                // Extração e log do valor de 'editandoVenda'
                // Verifica se o valor de "editandoVenda" foi passado pelo Intent
                String editandoVenda = intent.getStringExtra("editandoVenda");
                Log.d("RECEBIDO_editandoVenda", "Valor recebido: " + editandoVenda);

                // Inicializar a instância de CreditoPrefs
                creditoPrefs = new CreditoPrefs(this);

                // Se o valor de editandoVenda for "sim", definimos vendaEditada como true e salvamos no SharedPreferences
                if ("sim".equalsIgnoreCase(editandoVenda)) {
                    vendaEditada = true;
                    creditoPrefs.setVendaEditada(true); // Salvando no SharedPreferences
                } else {
                    creditoPrefs.setVendaEditada(false); // Garante que é false se não for edição
                }


                // Se o valor de editandoVenda for "sim", definimos vendaEditada como true
                if ("sim".equalsIgnoreCase(editandoVenda)) {
                    vendaEditada = true;
                }

                // Log para verificar o valor final de vendaEditada
                Log.d("VENDA_EDITADA", "Flag vendaEditada marcada como: " + vendaEditada);


                //
                nomeCliente = params.getString("nome_cliente");
                cpfcnpjCliente = params.getString("cpfcnpj");
                enderecoCliente = params.getString("endereco");



                getSupportActionBar().setSubtitle(classAuxiliar.maiuscula1(Objects.requireNonNull(nomeCliente).toLowerCase()));
            }
        }

        //
        spFormasPagamentoCliente = findViewById(R.id.spFormasPagamentoCliente);
        spBandeira = findViewById(R.id.spBandeira);
        spParcela = findViewById(R.id.spParcela);




      /* listaFormasPagamentoCliente = bd.getFormasPagamentoCliente(codigo_cliente);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaFormasPagamentoCliente);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamentoCliente.setAdapter(adapter);*/

        /*spFormasPagamentoCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] fPag = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");
                if (bd.getCartaoTrue(fPag[0]).equalsIgnoreCase("1")) {
                    bandPrazo.setVisibility(View.VISIBLE);

                    //
                    listaBandeiras = bd.getBandeiraFPg(fPag[0]);
                    adapterSpBandeira = new ArrayAdapter(context, android.R.layout.simple_spinner_item, listaBandeiras);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spBandeira.setAdapter(adapterSpBandeira);
                    //spBandeira.setOnItemSelectedListener(FinanceiroDaVenda.this);
                    spBandeira.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            //Toast.makeText(context, spBandeira.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                            //
                            listaParcelas = bd.getPrazoFPg(spBandeira.getSelectedItem().toString(), fPag[0]);
                            adapterSpParcela = new ArrayAdapter(context, android.R.layout.simple_spinner_item, listaParcelas);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spParcela.setAdapter(adapterSpParcela);
                            spParcela.setOnItemSelectedListener(FinanceiroDaVenda.this);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else {
                    listaBandeiras = null;
                    listaParcelas = null;
                    bandPrazo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

        spFormasPagamentoCliente.setOnItemSelectedListener(FinanceiroDaVenda.this);
        atualizarValFin();
        logFormasPagamentoPrazo();

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();
        configuracoes = new Configuracoes();
        if (configuracoes.GetDevice()) {
            iniciarStone();
        }
    }

    /********************* verificar limite **************************/
    private boolean verificarLimiteCreditoPrazo() {
        if (!formasPagamento.isEmpty()) {
            BigDecimal totalValoresCompraPrazo = BigDecimal.ZERO;
            boolean temFormaAPrazo = false;

            // Percorre todas as formas de pagamento no array
            for (int i = 0; i < formasPagamento.size(); i++) {
                String formaPagamento = formasPagamento.get(i);
                Log.d("FORMA PAGAMENTO", "Forma de pagamento: " + formaPagamento);

                // Verifica se a forma de pagamento é "A PRAZO"
                if (formaPagamento.contains("A PRAZO")) {
                    if (!valoresCompra.isEmpty()) {
                        BigDecimal valorFormaPrazo = new BigDecimal(valoresCompra.get(0));
                        totalValoresCompraPrazo = totalValoresCompraPrazo.add(valorFormaPrazo);
                        temFormaAPrazo = true;
                    } else {
                        Log.d("VERIFICAÇAO FORMA_PRAZO", "verificarLimiteCreditoPrazo: Verificaçao concluida,Liberando a venda A_PRAZO");
                    }
                }
            }
            // Se houver pelo menos uma forma de pagamento "A PRAZO"
            if (temFormaAPrazo) {
                Log.d("VERIFICANDO LIMITE", "Verificando limite de crédito para formas de pagamento a prazo.");

                creditoPrefs.setValorAprazo(totalValoresCompraPrazo.toString());
                creditoPrefs.setIdCliente(codigo_cliente);

                // Verifica o limite de crédito original **antes da transação**
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                int limiteCreditoCliente = dbHelper.getLimiteCreditoCliente(codigo_cliente);
                creditoPrefs.setLimiteCreditoOriginal(String.valueOf(limiteCreditoCliente));
                Log.d("LIMITE ORIGINAL", "Limite original de crédito para o cliente " + codigo_cliente + ": " + limiteCreditoCliente);

                BigDecimal limiteCreditoBigDecimal = BigDecimal.valueOf(limiteCreditoCliente);

                // Se o valor da compra a prazo for maior que o limite disponível
                if (totalValoresCompraPrazo.compareTo(limiteCreditoBigDecimal) > 0) {
                    Log.d("LIMITE EXCEDIDO", "O valor da compra a prazo excede o limite de crédito.");
                    String mensagem = String.format("O valor da compra excede o limite de crédito disponível.\nCrédito disponível: R$ %.2f", limiteCreditoBigDecimal);
                    formasPagamento.clear();
                    valoresCompra.clear();
                    new AlertDialog.Builder(this)
                            .setTitle("Limite de Crédito Excedido")
                            .setMessage(mensagem)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return false; // <-- Interrompe a execução
                }

                // Se o valor da compra a prazo for menor ou igual ao limite disponível
                else {
                    Log.d("782 LANÇANDO VENDA ", "Atualizando limite de crédito após a transação.");

                    // Atualiza o limite de crédito no banco de dados **apenas após a verificação**
                    BigDecimal novoLimite = limiteCreditoBigDecimal.subtract(totalValoresCompraPrazo);
                    bd.updateLimiteCreditoCliente(codigo_cliente, novoLimite);

                    return true; // <-- Transação concluída com sucesso
                }
            }
        }
        return true; // Caso não haja formas de pagamento a prazo, continua normalmente
    }


    /**************** BLOQUEAR PAGAMENTO A PRAZO PARA INADIMPLENCIA *******************/
// Carrega formas de pagamento no spinner com base na inadimplência
    public void carregarFormasDePagamento() {
        Log.d("FinanceiroDaVenda", "Iniciando carregamento das formas de pagamento para o cliente: " + codigo_cliente);

        ArrayList<String> formasDePagamento = new ArrayList<>();

        if (verificarInadimplencia(codigo_cliente)) {
            Log.d("FinanceiroDaVenda", "Cliente está inadimplente. Carregando apenas 'DINHEIRO _ A VISTA' e outras formas 'A VISTA'.");


            // Carregar todas as formas de pagamento disponíveis
            ArrayList<String> todasFormas = bd.getFormasPagamentoCliente(codigo_cliente);

            // Verifica todas as formas "A VISTA" e as adiciona
            for (String forma : todasFormas) {
                if (forma.contains("A VISTA") && !formasDePagamento.contains(forma)) {
                    formasDePagamento.add(forma); // Adiciona todas as formas que contenham "A VISTA"
                }
            }
        } else {
            Log.d("FinanceiroDaVenda", "Cliente não está inadimplente. Carregando todas as formas de pagamento disponíveis.");
            formasDePagamento = bd.getFormasPagamentoCliente(codigo_cliente);
        }

        // Usando o CustomSpinnerAdapter para mostrar apenas a primeira parte das formas de pagamento
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, formasDePagamento);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamentoCliente.setAdapter(adapter);

        Log.d("FinanceiroDaVenda", "Formas de pagamento configuradas no spinner." + formasDePagamento);
    }


    /****************** METODO PARA VERIFICAR INADIMPLENCIA  ***************/
    private boolean verificarInadimplencia(String clienteId) {
        // Implementação robusta de verificação de inadimplência
        ArrayList<FinanceiroReceberClientes> contasReceber = bd.getContasReceberCliente(clienteId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date dataAtual = new Date();
        boolean isInadimplente = false;

        for (FinanceiroReceberClientes conta : contasReceber) {
            String vencimento = conta.getVencimento_financeiro();
            try {
                Date dataVencimento = sdf.parse(vencimento);
                if (dataVencimento != null && dataVencimento.before(dataAtual)) {
                    Log.d("FinanceiroDaVenda", "Conta vencida encontrada. Cliente inadimplente.");
                    isInadimplente = true;
                    return true;
                }
            } catch (ParseException e) {
                Log.e("FinanceiroDaVenda", "Erro ao analisar a data de vencimento: " + vencimento, e);
            }
        }

        Log.d("FinanceiroDaVenda", "Nenhuma conta vencida encontrada. Cliente não está inadimplente.");
        return false;
    }


    /**************** ATUALIZAR VALOR DE CREDITO DISPONIVEL **********************/

    // Método para atualizar o limite de crédito do cliente após a venda
    private void atualizarLimiteCreditoCliente(String codigo_cliente, BigDecimal valorVenda) {
        // Busca o limite de crédito atual
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        int limiteAtual = dbHelper.getLimiteCreditoCliente(codigo_cliente);

        // Subtrai o valor da venda do limite atual
        BigDecimal novoLimite = BigDecimal.valueOf(limiteAtual).subtract(valorVenda);

        // Atualiza o limite no banco de dados
        dbHelper.updateLimiteCreditoCliente(codigo_cliente, novoLimite);

        Log.d("AtualizarLimiteCredito", "Novo limite de crédito do cliente: " + novoLimite);
    }


    /************** ATUALIZAR VENDA CONCLUIDA *******************/
    public void atualizarVendaConlcuida() {
        // Obtendo o SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        // Recuperando o id_venda sem valor padrão
      //  int idVenda = prefs.getInt("id_venda_app", 0);

        // Chamando o método marcarVendaComoFinalizada com o id recuperado
       bd.marcarVendaComoFinalizada(id_venda_app);
        Log.d("ATUALIZANDO", "AtUALIZANDO O CAMPO DA VENDA PRA 1: ");
    }


    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();
        // Verificar se o GPS foi aceito pelo operador
        isGPSEnabled();
        if (coord.isGPSEnabled()) {
            coord.getLocation();
            //gps.getLatLon();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(context);
        /*Em seguida, é necessário chamar o método setAppName da classe Stone,
        que recebe como parâmetro uma String referente ao nome da sua aplicação.*/
        Stone.setAppName(getApplicationName(context));
        //Ambiente de Sandbox "Teste"
        //Stone.setEnvironment(new Configuracoes().Ambiente());
        //Ambiente de Produção
        //Stone.setEnvironment((Environment.PRODUCTION));

        // Esse método deve ser executado para inicializar o SDK
        List<UserModel> userList = StoneStart.init(context);

        // Quando é retornado null, o SDK ainda não foi ativado
        /*if (userList != null) {
            // O SDK já foi ativado.
            _pinpadAtivado();

        } else {
            // Inicia a ativação do SDK
            ativarStoneCode();
        }*/
    }

    private void _verificarFPgVenda() {
        if (Objects.requireNonNull(prefs.getString("print_promissoria", "0")).equalsIgnoreCase("0")
                && Objects.requireNonNull(prefs.getString("print_boleto", "0")).equalsIgnoreCase("0")) {
            finish();
            return;
        }

        int v = 0;
        for (int a = 0; a < listaFinanceiroCliente.size(); a++) {
            //Log.e("FINANCEIRO", listaFinanceiroCliente.get(a).getFpagamento_financeiro());

            // IMPRESSÃO DA PROMISSORIA
            if (Objects.requireNonNull(prefs.getString("print_promissoria", "0")).equalsIgnoreCase("1")) {
                //if (listaFinanceiroCliente.get(a).getFpagamento_financeiro().replace(" _ ", "").equalsIgnoreCase("PROMISSORIA")) {
                if (listaFinanceiroCliente.get(a).getFpagamento_financeiro().replace(" _ ", "").contains("PROMISSORIA")) {
                    v++;
                    String val = classAuxiliar.maskMoney(new BigDecimal(listaFinanceiroCliente.get(a).getValor_financeiro()));
                    Intent i;
                    if (configuracoes.GetDevice()) {
                        i = new Intent(context, ImpressoraPOS.class);
                    } else {
                        i = new Intent(context, BluetoothPrintActivity.class);
                    }

                    //
                    i.putExtra("razao_social", nomeCliente);
                    i.putExtra("tel_contato", "");
                    //i.putExtra("numero", txtDocumentoFormaPagamento.getText().toString());
                    i.putExtra("numero", listaFinanceiroCliente.get(a).getDocumento_financeiro());
                    i.putExtra("vencimento", txtVencimentoFormaPagamento.getText().toString());
                    i.putExtra("valor", val);
                    //i.putExtra("valor", financeiroVendasDomain.getValor_financeiro());
                    i.putExtra("id_cliente", codigo_cliente);
                    i.putExtra("cpfcnpj", cpfcnpjCliente);
                    i.putExtra("endereco", enderecoCliente);
                    i.putExtra("imprimir", "Promissoria");


                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    //Intent intent = new Intent(this, SomeActivity.class);
                    //launchSomeActivity.launch(i);
                }
            }


            /*if (listaFinanceiroCliente.get(a).getFpagamento_financeiro().replace(" _ ", "").equalsIgnoreCase("PROMISSORIA")) {
                v++;
                String val = classAuxiliar.maskMoney(new BigDecimal(listaFinanceiroCliente.get(a).getValor_financeiro()));
                Intent i;
                if (configuracoes.GetDevice()) {
                    i = new Intent(context, ImpressoraPOS.class);
                } else {
                    i = new Intent(context, Impressora.class);
                }

                //
                i.putExtra("razao_social", nomeCliente);
                i.putExtra("tel_contato", "");
                //i.putExtra("numero", txtDocumentoFormaPagamento.getText().toString());
                i.putExtra("numero", listaFinanceiroCliente.get(a).getDocumento_financeiro());
                i.putExtra("vencimento", txtVencimentoFormaPagamento.getText().toString());
                i.putExtra("valor", val);
                //i.putExtra("valor", financeiroVendasDomain.getValor_financeiro());
                i.putExtra("id_cliente", codigo_cliente);
                i.putExtra("cpfcnpj", cpfcnpjCliente);
                i.putExtra("endereco", enderecoCliente);
                i.putExtra("imprimir", "Promissoria");

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                //Intent intent = new Intent(this, SomeActivity.class);
                //launchSomeActivity.launch(i);
            }
            */

            // IMPRESSÃO DO BOLETO
            if (Objects.requireNonNull(prefs.getString("print_boleto", "0")).equalsIgnoreCase("1")) {

                String contaBancFormPag = bd.getContaBancariaFormaPagamento(listaFinanceiroCliente.get(a).getFpagamento_financeiro().replace(" _ ", ""));
                // VERIFICA SE É PARA IMPRIMIR A PROMISSORIA, CASO SEJA 0, NÃO AVANÇA.

                if (!contaBancFormPag.equalsIgnoreCase("0")) {

                    String CodContaBanc = bd.getCodContaBancaria(contaBancFormPag);

                    // SE FOR IGUAL A 1 CÓD BB, IMPRIMI
                    if (CodContaBanc.equalsIgnoreCase("001") || CodContaBanc.equalsIgnoreCase("237")) {

                        Intent i;
                        String val = classAuxiliar.maskMoney(new BigDecimal(listaFinanceiroCliente.get(a).getValor_financeiro()));
                        if (configuracoes.GetDevice()) {
                            i = new Intent(context, ImpressoraPOS.class);
                        } else {
                            i = new Intent(context, Impressora.class);
                        }

                        i.putExtra("razao_social", nomeCliente);
                        i.putExtra("tel_contato", "");
                        i.putExtra("numero", listaFinanceiroCliente.get(a).getDocumento_financeiro());
                        i.putExtra("vencimento", listaFinanceiroCliente.get(a).getVencimento_financeiro());
                        i.putExtra("valor", val);
                        i.putExtra("id_cliente", codigo_cliente);
                        i.putExtra("cpfcnpj", cpfcnpjCliente);
                        i.putExtra("endereco", enderecoCliente);
                        i.putExtra("imprimir", "Boleto");
                        i.putExtra("nota_fiscal", listaFinanceiroCliente.get(a).getNota_fiscal());
                        i.putExtra("nContaBanco", contaBancFormPag);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        finish();
                    }
                }
            }
        }

        if (v == 0) finish();
    }
    // Create lanucher variable inside onAttach or onCreate or global
    /*ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // your operation....
                }
            });*/

    /*public void openYourActivity() {
        Intent intent = new Intent(this, SomeActivity.class);
        launchSomeActivity.launch(intent);
    }*/

    //
    private void _salvarFinanceiro() {
        atualizarVendaConlcuida();

        if (txtTotalItemFinanceiro.getText().equals("0,00")) {
            //
            Toast.makeText(FinanceiroDaVenda.this, "Adicione pelo menos uma forma de pagamento ao financeiro.", Toast.LENGTH_LONG).show();
        } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
            //
            Toast.makeText(FinanceiroDaVenda.this, "O valor do financeiro está diferente da venda.", Toast.LENGTH_LONG).show();
        } else {

            // SE O VENDEDOR PODE VENDER SEM COMPARAR A POSIÇÃO DO CLIENTE, PASSA DIRETO PARA A INSERÇÃO DO FINANCEIRO
            if (prefs.getString("verificar_posicao_cliente", "1").equalsIgnoreCase("0")) {
                finalizarFinanceiroVenda();
            } else {
                if (coordCliLat != 0.0) {
                    // VERIFICA SE O GPS ESTÁ ATIVO
                    if (!coord.isGPSEnabled()) {
                        //Toast.makeText(FinanceiroDaVenda.this, "Ative o GPS para finalizar a venda!.", Toast.LENGTH_LONG).show();
                        /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);*/

                        createLocationRequest();

                    } else {
                        coord.getLocation();
                        verifCordenadas();
                    }

                } else {
                    finalizarFinanceiroVenda();
                }
            }
        }
    }

    private void atualizarValFin() {
        try {
            //
            listaFinanceiroCliente = bd.getFinanceiroCliente(prefs.getInt("id_venda_app", 1));

            if (listaFinanceiroCliente != null && !listaFinanceiroCliente.isEmpty()) {
                for (FinanceiroVendasDomain item : listaFinanceiroCliente) {
                    // Obtém a forma de pagamento
                    String formaPagamentoRec = item.getFpagamento_financeiro();
                    Log.d("FORMA PAGAMENTO", "Forma de pagamento: " + formaPagamentoRec);

                    // Você pode fazer qualquer outra operação necessária com a forma de pagamento aqui
                }
            }
            adapter = new FinanceiroVendasAdapter(this, listaFinanceiroCliente, valoresCompra);
            rvFinanceiro.setAdapter(adapter);
            //
            String tif = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(prefs.getInt("id_venda_app", 1)))));
            txtTotalItemFinanceiro.setText(tif);

            //
            //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

            //
            String valorFinanceiro = String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString()));
            String valorFinanceiroAdd = String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString()));

            //SUBTRAIR O VALOR PELA QUANTIDADE
            String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
            String total = String.valueOf(classAuxiliar.subitrair(subtracao));

            txtValorFormaPagamento.setText(total);
        } catch (Exception e) {
            Log.i("Financeiro", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DialogFragment();
        newFragment.show(getSupportFragmentManager(), "dataPicker");
    }

    public void showDatePickerDialogAndroid10Plus(View v) {
        DialogFragment newFragment = new DialogFragment();
        newFragment.show(getSupportFragmentManager(), "dataPicker");
    }

    public void mostrarMsg() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Emissor Web");
        //define a mensagem
        String msg = "Deseja emitir a NFC-e?";
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();

                /*/
                Toast.makeText(FinanceiroDaVenda.this, "Venda Finalizada Com Sucesso.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(FinanceiroDaVenda.this, Principal.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                */

                //sair(); //
                //listaVendas = bd.getVendasCliente(prefs.getInt("id_venda_app", 1));
                //ArrayList<Produtos> listaProdutos;
                //listaProdutos = bd.getAllProdutos();
                //i.putExtra("produtos", listaProdutos);

                PackageManager packageManager = getPackageManager();
                String packageName = "br.com.zenitech.emissorweb";
                Intent i = packageManager.getLaunchIntentForPackage(packageName);
                //DADOS EMISSOR WEB
                i.putExtra("siac", "1");
                i.putExtra("produto", produto_emissor);
                i.putExtra("quantidade", quantidade_emissor);
                i.putExtra("valor_unit", valor_unit_emissor);
                i.putExtra("forma_pagamento", "DINHEIRO");

                startActivity(i);
                finish();
            }
        });

        //define um botão como negativo.
        builder.setNegativeButton("NÃO", (arg0, arg1) -> sair());
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    private void addFinanceiro() {

        //
        id = id + 1;
        ed.putInt("id_financeiro_venda", id).apply();

        //
        String[] fPag = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");

        String sql = "";
        sql += id + "\n";//CODIGO_FINANCEIRO
        sql += prefs.getString("unidade", "UNIDADE TESTE") + "\n";//UNIDADE_FINANCEIRO
        sql += classAuxiliar.inserirDataAtual() + "\n";//DATA_FINANCEIRO
        sql += codigo_cliente + "\n";//CODIGO_CLIENTE_FINANCEIRO
        sql += fPag[0] + "\n";//sql += spFormasPagamentoCliente.getSelectedItem().toString() + "\n";//FPAGAMENTO_FINANCEIRO
        sql += txtDocumentoFormaPagamento.getText().toString() + "\n";//DOCUMENTO_FINANCEIRO
        sql += String.valueOf(classAuxiliar.inserirData(classAuxiliar.formatarData(classAuxiliar.soNumeros(txtVencimentoFormaPagamento.getText().toString())))) + "\n";//VENCIMENTO_FINANCEIRO
        sql += String.valueOf(classAuxiliar.converterValores(txtValorFormaPagamento.getText().toString())) + "\n";//VALOR_FINANCEIRO
        sql += "0" + "\n";//STATUS_AUTORIZACAO
        sql += "0" + "\n";//PAGO
        sql += "0" + "\n";//VASILHAME_REF
        sql += "0" + "\n";//USUARIO_ATUAL_FINANCEIRO
        sql += classAuxiliar.inserirDataAtual() + "\n";//DATA_INCLUSAO
        sql += "" + "\n";//NOSSO_NUMERO_FINANCEIRO
        sql += "" + prefs.getInt("id_vendedor", 1) + "\n";//ID_VENDEDOR_FINANCEIRO
        sql += "" + prefs.getInt("id_venda_app", 1) + "\n";

        //SETAR O SQL NO LOG PARA CONSULTA
        Log.e("SQL", sql);

        String sBandeira = "", sPrazo = "";
        if (bd.getCartaoTrue(fPag[0]).equalsIgnoreCase("1")) {
            try {
                /*PackageManager packageManager = getPackageManager();
                String packageName = "br.com.zenitech.emissorweb";
                //Intent i = new Intent(packageName);
                //i.setPackage(packageName);
                Intent i = packageManager.getLaunchIntentForPackage(packageName);
                //DADOS EMISSOR WEB
                i.putExtra("siac", "1");
                i.putExtra("cpfCnpj_cliente", "000.000.000-00");
                i.putExtra("formaPagamento", "CARTÃO DE DÉBITO");//CRÉDITO
                i.putExtra("produto", "P 13");
                i.putExtra("qnt", "1");
                i.putExtra("vlt", txtValorFormaPagamento.getText().toString());

                startActivityForResult(i, 2);*/

                sBandeira = classAuxiliar.getIdBandeira(spBandeira.getSelectedItem().toString());
                sPrazo = spParcela.getSelectedItem().toString();
            } catch (Exception ignored) {

            }
        }
        //INSERIR FINANCEIRO
        bd.addFinanceiro(new FinanceiroVendasDomain(
                String.valueOf(id),//CODIGO_FINANCEIRO
                prefs.getString("unidade", "UNIDADE TESTE"),//UNIDADE_FINANCEIRO
                classAuxiliar.inserirDataAtual(),//DATA_FINANCEIRO
                codigo_cliente,//CODIGO_CLIENTE_FINANCEIRO
                fPag[0],//spFormasPagamentoCliente.getSelectedItem().toString(),//FPAGAMENTO_FINANCEIRO
                txtDocumentoFormaPagamento.getText().toString(),//DOCUMENTO_FINANCEIRO
                String.valueOf(classAuxiliar.inserirData(classAuxiliar.formatarData(classAuxiliar.soNumeros(txtVencimentoFormaPagamento.getText().toString())))),//VENCIMENTO_FINANCEIRO
                String.valueOf(classAuxiliar.converterValores(txtValorFormaPagamento.getText().toString())),//VALOR_FINANCEIRO
                "0",//STATUS_AUTORIZACAO
                "0",//PAGO
                "0",//VASILHAME_REF
                "0",//USUARIO_ATUAL_FINANCEIRO
                "" + classAuxiliar.inserirDataAtual(),//DATA_INCLUSAO
                "",//NOSSO_NUMERO_FINANCEIRO
                "" + prefs.getInt("id_vendedor", 1),//ID_VENDEDOR_FINANCEIRO
                "" + prefs.getInt("id_venda_app", 1),
                txtNotaFiscal.getText().toString(),
                bd.getIdAliquota(bd.getIdFPG(fPag[0]), sBandeira, sPrazo)
        ));

        //if (fPag[0].equalsIgnoreCase("PROMISSORIA")) {
        if (fPag[0].contains("PROMISSORIA")) {
            bd.updatePosApp(txtDocumentoFormaPagamento.getText().toString());
            txtDocumentoFormaPagamento.setText("");
        } else if (!bd.getContaBancariaFormaPagamento(fPag[0]).equalsIgnoreCase("0")) {
            bd.updateUltimoBoleto(txtDocumentoFormaPagamento.getText().toString());
            txtDocumentoFormaPagamento.setText("");
        }

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(prefs.getInt("id_venda_app", 1));
        adapter = new FinanceiroVendasAdapter(this, listaFinanceiroCliente, valoresCompra);
        rvFinanceiro.setAdapter(adapter);
        //
        String tif = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(prefs.getInt("id_venda_app", 1)))));
        txtTotalItemFinanceiro.setText(tif);

        //
        //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

        //
        String valorFinanceiro = String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(classAuxiliar.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {

            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }


        // SELECT PIX
        /*if (fPag[0].equals("PIX")) {
            Intent i = new Intent(context, Pix.class);
            startActivity(i);
        }*/


        //
        txtDocumentoFormaPagamento.setText("");
        tilDocumento.setVisibility(View.VISIBLE);
        spFormasPagamentoCliente.setSelection(0);


        //ESCONDER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void atualizarDataVencimento(String data) {
        txtVencimentoFormaPagamento.setText(data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        txtDocumentoFormaPagamento.setText("");
        String[] fPag = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");
        String selectedItem = spFormasPagamentoCliente.getSelectedItem().toString(); // Captura o item selecionado no Spinner
        creditoPrefs.setFormaPagamentoPrazo(selectedItem);

        // Logando o item selecionado para visualização no Logcat
        Log.d("ItemSelecionado", "Item selecionado ENVIADO AO PREFS: " + selectedItem);

        // Verificar se a forma de pagamento selecionada é "A PRAZO"
        String[] detalhesPagamento = selectedItem.split(" _ ");

        // Verificar se a forma de pagamento selecionada é "A PRAZO" e contém "PROMISSORIA"
        if (fPag.length > 1 && "A PRAZO".equals(fPag[1]) && fPag[0].contains("PROMISSORIA")) {
            iSPromissoria = true;
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            /*Verificar se o dispositivo é um smartphone
            Configuracoes configuracoes = new Configuracoes();
            if (!configuracoes.GetDevice()) { // Se for um smartphone
                if (!bluetoothAdapter.isEnabled()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Ativação de Bluetooth")
                            .setMessage("Ative o Bluetooth para imprimir a promissória.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // O botão de OK simplesmente fecha o diálogo
                                dialog.dismiss();
                            })
                            .setCancelable(false) // Impede que o alerta seja fechado sem ação do usuário
                            .show();
                }
            }*/
        }




        /*if (fPag.length > 1 && "A PRAZO".equals(fPag[1])) {
            Log.d("FormaPagamentoSelecionada", "Forma de pagamento selecionada a prazo: " + selectedItem);

            // Capturar o valor da compra inserido no campo txtValorFormaPagamento
            String valorCompraAtual = txtValorFormaPagamento.getText().toString(); // Assumindo que o valor da compra está no campo txtValorFormaPagamento

            // Limpar a lista e adicionar o valor atual para garantir que só tenha o valor atual
            valoresCompra.add(valorCompraAtual);
            Log.d("ValorCompra", "Valor da compra: " + valorCompraAtual);
            Log.d("ValoresCompra", "Valores da compra: " + valoresCompra);
        } else {
            Log.d("FormaPagamentoSelecionada", "Forma de pagamento selecionada não é a prazo: " + selectedItem);
            // Limpar a lista de valores caso a forma de pagamento não seja a prazo
            valoresCompra.clear();
        }*/

        // SELECT PIX
        if (fPag[0].equals("PIX")) {
            runOnUiThread(() -> {

            });
        }
        if (fPag[1].equals("A PRAZO")) {

            runOnUiThread(() -> {
                tilDocumento.setVisibility(View.VISIBLE);
                tilVencimento.setVisibility(View.VISIBLE);

                posApp = bd.getPos();
                //if (fPag[0].equalsIgnoreCase("PROMISSORIA")) {
                if (fPag[0].contains("PROMISSORIA")) {
                    int uprom = Integer.parseInt(posApp.getUltpromissoria());
                    //if (posApp.getUltpromissoria().equalsIgnoreCase("0")) {
                    if (uprom == 0) {
                        //nDoc = posApp.getSerie() + "00000001";
                        int n = (Integer.parseInt(posApp.getSerie()) * 1000000) + 1;
                        nDoc = String.valueOf(n);
                    } else {
                        /*String[] soma = {posApp.getUltpromissoria(), "1"};
                        String[] totSoma = String.valueOf(classAuxiliar.somar(soma)).split("[.]");
                        nDoc = classAuxiliar.soNumeros(totSoma[0]);*/
                        nDoc = String.valueOf(Integer.parseInt(posApp.getUltpromissoria()) + 1);
                    }

                    txtDocumentoFormaPagamento.setText(nDoc);
                    //  txtDocumentoFormaPagamento.setEnabled(false);
                } else if (!bd.getContaBancariaFormaPagamento(fPag[0]).equalsIgnoreCase("0") &&
                        !bd.getContaBancariaFormaPagamento(fPag[0]).equalsIgnoreCase("")) {
                    //else if (fPag[0].equalsIgnoreCase("BOLETO")) {
                    int ubol = Integer.parseInt(posApp.getUltboleto());
                    //if (posApp.getUltboleto().equalsIgnoreCase("0")) {
                    if (ubol == 0) {
                        //nDoc = posApp.getSerie() + "00000001";
                        int n = (Integer.parseInt(posApp.getSerie_boleto()) * 1000000) + 1;
                        nDoc = String.valueOf(n);
                    } else {
                        nDoc = String.valueOf(Integer.parseInt(posApp.getUltboleto()) + 1);
                        //String[] soma = {posApp.getUltpromissoria(), "1"};
                        //String[] totSoma = String.valueOf(classAuxiliar.somar(soma)).split("[.]");
                        //nDoc = classAuxiliar.soNumeros(totSoma[0]);
                    }

                    txtDocumentoFormaPagamento.setText(nDoc);
                    // txtDocumentoFormaPagamento.setEnabled(false);
                    tilNotaFiscal.setVisibility(View.VISIBLE);
                } else {
                    txtDocumentoFormaPagamento.setEnabled(true);
                }

                //int n = (serieBoleto * 100000000) + 1;

                //atualizarDataVencimento(classAuxiliar.dataFutura(bd.DiasPrazoCliente(fPag[0], codigo_cliente)));
                txtVencimentoFormaPagamento.setText(classAuxiliar.dataFutura(bd.DiasPrazoCliente(fPag[0], codigo_cliente)));

            });

            if (fPag[3].equals("1")) {

                runOnUiThread(() -> tilVencimento.setVisibility(View.VISIBLE));
            }

        } else {
            runOnUiThread(() -> {
                tilDocumento.setVisibility(View.VISIBLE);
                tilVencimento.setVisibility(View.GONE);
                txtDocumentoFormaPagamento.setEnabled(true);
                //
                tilNotaFiscal.setVisibility(View.GONE);
                txtNotaFiscal.setText("");
                //
                txtVencimentoFormaPagamento.setText(classAuxiliar.exibirDataAtual());

                Log.i("Fin", classAuxiliar.formatarData(classAuxiliar.soNumeros(txtVencimentoFormaPagamento.getText().toString())));
            });
        }

        runOnUiThread(() -> {
            if (bd.getCartaoTrue(fPag[0]).equalsIgnoreCase("1")) {
                bandPrazo.setVisibility(View.VISIBLE);

                //
                listaBandeiras = bd.getBandeiraFPg(fPag[0]);
                adapterSpBandeira = new ArrayAdapter(context, android.R.layout.simple_spinner_item, listaBandeiras);
                adapterSpBandeira.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spBandeira.setAdapter(adapterSpBandeira);
                //spBandeira.setOnItemSelectedListener(FinanceiroDaVenda.this);
                spBandeira.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        //Toast.makeText(context, spBandeira.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                        //
                        listaParcelas = bd.getPrazoFPg(spBandeira.getSelectedItem().toString(), fPag[0]);
                        adapterSpParcela = new ArrayAdapter(context, android.R.layout.simple_spinner_item, listaParcelas);
                        adapterSpParcela.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spParcela.setAdapter(adapterSpParcela);
                        //spParcela.setOnItemSelectedListener(FinanceiroDaVenda.this);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            } else {
                listaBandeiras = null;
                listaParcelas = null;
                bandPrazo.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

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

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean comparar() {

        //
        BigDecimal valorFinanceiro = new BigDecimal(String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString())));
        BigDecimal valorFinanceiroAdd = new BigDecimal(String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString())));

        if (valorFinanceiroAdd.compareTo(valorFinanceiro) == 1) {
            //
            if (valorFinanceiro.toString().equals(valorFinanceiroAdd.toString())) {

                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                sair();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        sair();
    }

    private void sair() {
        /*//
        Toast.makeText(FinanceiroDaVenda.this, "Venda Finalizada Com Sucesso.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(FinanceiroDaVenda.this, Principal2.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        super.finish();*/
        cancelarVenda();
    }

    private void cancelarVenda() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logosiac);
        //define o titulo
        builder.setTitle("Atenção");
        //define a mensagem
        builder.setMessage("Você Deseja Realmente Cancelar Esta Venda?");
        //define um botão como positivo
        builder.setPositiveButton("Sim", (arg0, arg1) -> {

            // Restaurar limite de crédito ao cancelar uma venda a prazo
            String valorAprazo = creditoPrefs.getValorAprazo();
            String codigoCliente = creditoPrefs.getIdCliente();
            Boolean restituicaoRealizada = creditoPrefs.getRestituicaoRealizada();
            Log.d("CANCELAR", "cancelarVenda: venda já restituída " + restituicaoRealizada);

            if (valorAprazo != null && !valorAprazo.isEmpty() && !codigoCliente.isEmpty() && !restituicaoRealizada) {
                BigDecimal valorRestituido = new BigDecimal(valorAprazo);
                DatabaseHelper dbHelper = new DatabaseHelper(context);

                // Verificar o limite atual e o limite primário do cliente
                int limiteAtual = dbHelper.getLimiteCreditoCliente(codigoCliente);
                BigDecimal limiteOriginal = new BigDecimal(creditoPrefs.getLimiteCreditoPrimario()); // Limite original para não ultrapassar
                BigDecimal limiteCreditoDisponivel = new BigDecimal(limiteAtual);
                Log.d("RESTITUIR LIMITE", "Limite atual antes da restituição: " + limiteAtual);

                // Só restituir se o limite de crédito foi utilizado (limite atual menor que o original)
                if (limiteCreditoDisponivel.compareTo(limiteOriginal) < 0) {
                    // Calcular o valor a ser restituído sem ultrapassar o limite original
                    BigDecimal valorRestituir = valorRestituido.min(limiteOriginal.subtract(limiteCreditoDisponivel));

                    if (valorRestituir.compareTo(BigDecimal.ZERO) > 0) {
                        // Restituir o limite de crédito ajustado
                        dbHelper.restituirLimiteCreditoCliente(codigoCliente, valorRestituir);
                        Log.d("RESTITUIR LIMITE", "Restituindo limite de crédito: " + valorRestituir + " para o cliente: " + codigoCliente);

                        // Marcar a restituição como realizada
                        restituicaoRealizada = true;
                        creditoPrefs.setRestituicaoRealizada(true);
                    } else {
                        Log.d("RESTITUIR LIMITE", "Nenhuma restituição foi feita, o limite já atingiu o valor original.");
                    }
                } else {
                    Log.d("RESTITUIR LIMITE", "Limite de crédito já está completo. Não será feita restituição.");
                }

                // Limpa as informações armazenadas após o cancelamento
                creditoPrefs.clear();
            }

            //Excluir todos os produtos da venda atual do banco de dados
            int linhasAfetadas = bd.deleteProdutosPorVenda(String.valueOf(id_venda_app));
            Log.d("ID PARA EXCLUSAO TOTAL", "Valor de id_venda_app: " + id_venda_app);

            // Log para verificar a exclusão
            if (linhasAfetadas > 0) {
                Log.d("FInanceiro_CANCELER", "Venda cancelada com sucesso! " + linhasAfetadas + " produtos foram excluídos.");
                Toast.makeText(FinanceiroDaVenda.this, "Venda cancelada com sucesso!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d("FInanceiro_CANCELER", "Nenhum produto foi excluído. Verifique o código de venda.");
            }

            //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();
            int i = bd.deleteVenda(prefs.getInt("id_venda_app", 0));
            if (i != 0) {
                //
                Toast.makeText(FinanceiroDaVenda.this, "Esta Venda foi Cancelada!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(FinanceiroDaVenda.this, Principal2.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                super.finish();
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


    /////////// ***********************************************


    public boolean raio(double latIni, double lonIni) {
        boolean result = false;

        posicaoInicial = new LatLng(latIni, lonIni);
        try {
            posicaiFinal = new LatLng(coordCliLat, coordCliLon);
        } catch (Exception ignored) {
            posicaiFinal = new LatLng(0.0, 0.0);
        }
        distance = SphericalUtil.computeDistanceBetween(posicaoInicial, posicaiFinal);
        double distanciaComparar = distance;

        //Log.e("LOG", "A Distancia é = " + (distance));
        Locale mL = new Locale("pt", "BR");
        //String.format(mL, "%4.3f%s", distance, unit);
        String unit = "m";
        if (distance >= 1000) {
            distance /= 1000;
            unit = "km";
        }
        Log.e("Distancia", "A Distancia é = " + String.format(mL, "%4.2f%s", distance, unit));
        Log.e("Distancia", "A Distancia é = " + distanciaComparar);
        //if (distance <= 0.1) {
        if (distanciaComparar <= 150 && unit.equalsIgnoreCase("m")) {
            result = true;
        } else {
            // Retirar depois da atualização
            //result = true;

            //
            if (verificarOnline.isOnline(context)) {
                String posCli = coordCliLat + ", " + coordCliLon;
                String posVen = coord_latitude + ", " + coord_longitude;
                String raio = String.format(mL, "%4.2f%s", distance, unit);
                String prec = precisao + "m";
                //
                enviarLog(posCli, posVen, raio, prec);
            }
            msg("Você parece estar a uns " + String.format(mL, "%4.2f%s", distance, unit) + " de onde o cliente está. Chegue mais perto para finalizar a venda!");
        }

        return result;
    }

    /**
     * P1: PEGA AS CORDENADAS
     * P2: VERIFICAR SE TEM INTERNET
     * P3: TEM INTERNET! VERIFICA SE O CLIENTE JÁ POSSUI AS CORDENADAS DA SUA CASA
     * P3.1: CLIENTE NÃO TEM COREDENAS! ATUALIZA O CADASTRO COM AS CORDENAS INFORMADA ANTERIORMENTE
     * P4: CALCULAR O RAIO DA CASA DO CLIENTE COM A POSIÇÃO DO ENTREGADOR
     */

    int verificacaoLocalidade = 0;

    // PEGAR AS CORDENADAS DO ENTREGADOR
    private void verifCordenadas() {

        //msg(String.valueOf(coord_latitude_pedido));
        // VERIFICA SE A ACTIVITY ESTÁ VISÍVEL
        if (VerificarActivityAtiva.isActivityVisible()) {
            //barra de progresso pontos
            dialog.show();


            String[] c = coord.getLatLon().split(",");
            coord_latitude = Double.parseDouble(c[0]);
            coord_longitude = Double.parseDouble(c[1]);

            precisao = coord.getPrecisao();

            //Log.i("POS", c[0] + ", " + c[1]);
            // VERIFICA SE AS CORDENADAS DO ENTREGADOR FORAM RECONHECIDAS
            if (coord_latitude != 0.0) {

                //msg("Peguei a latitude: " + coord_latitude + ", " + coord_longitude);
                verifClienteCordenada();
            }

            new Handler().postDelayed(() -> {

                // SE O TOTAL DE VERIFICAÇÃO FOR MAIR QUE 2 PARA DE CONSULTAR
                verificacaoLocalidade++;
                if (verificacaoLocalidade > 2) {
                    verificacaoLocalidade = 0;
                    Toast.makeText(context, "Não foi possível verificar suas cordenadas!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                // VERIFICA SE AS CORDENADAS DO ENTREGADOR FORAM RECONHECIDAS
                if (coord_latitude == 0.0) {
                    verifCordenadas();
                }

            }, 3000);
        }
    }


    /**
     *
     *    Função para somar os valores do array valoresCompra
     *
     *
     */

    private BigDecimal somarValoresCompra() {
        BigDecimal somaTotal = BigDecimal.ZERO; // Inicializa a variável somaTotal com zero

        // Percorre todos os valores do array valoresCompra
        for (String valor : valoresCompra) {
            try {
                // Converte o valor de String para BigDecimal e adiciona à somaTotal
                BigDecimal valorBigDecimal = new BigDecimal(valor);
                somaTotal = somaTotal.add(valorBigDecimal);
            } catch (NumberFormatException e) {
                Log.e("SomarValoresCompra", "Erro ao converter valor para BigDecimal: " + valor, e);
            }
        }

        return somaTotal; // Retorna a soma dos valores
    }


    // VERIFICAR AS CORDENADAS DO CLIENTE
    private void verifClienteCordenada() {

        //msg("Cordenadas do cliente: " + coordCliLat + ", " + coordCliLon);

        if (coordCliLat != 0.0) {
            verifRaio();
        }
    }

    //
    private void verifRaio() {
        if (raio(coord_latitude, coord_longitude)) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            // FINALIZA O PEDIDO
            finalizarFinanceiroVenda();
        } else {
            //msg("Você parece não está próximo ao cliente! Tente novamente.");

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void msg(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    private void isGPSEnabled() {
        if (!coord.isGPSEnabled()) {
            Log.i("principal", "GPS Desativado!");
        } else {
            isGPSPermisson();
        }
    }

    private void isGPSPermisson() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }
    public void showSequentialToasts(String firstMessage, String secondMessage) {
        Toast firstToast = Toast.makeText(getApplicationContext(), firstMessage, Toast.LENGTH_LONG);
        firstToast.show();
        new Handler().postDelayed(() -> {
            Log.d("ToastCondition", "iSPromissoria value: " + iSPromissoria);
            if (iSPromissoria) {
                Toast secondToast = Toast.makeText(getApplicationContext(), secondMessage, Toast.LENGTH_LONG);
                secondToast.show();
            }
        }, 1500);
    }


    private void finalizarFinanceiroVenda() {
        bd.updateFinalizarVenda(String.valueOf(prefs.getInt("id_venda_app", 1)));
        showSequentialToasts("Venda Finalizada Com Sucesso...", "Aguarde a impressão ");

        // Verifica se o Bluetooth foi negado pelo menos duas vezes
        int negativasBluetooth = prefs.getInt("negativas_bt", 0);

        // Verifica se a venda está sendo editada
        if (vendaEditada) {
            // Venda está sendo editada, verificar se precisa ativar Bluetooth ou não
            if (iSPromissoria && negativasBluetooth < 2) {
                // Se é uma promissória e ainda não houve duas negativas, tenta ativar o Bluetooth
                runOnUiThread(() -> {
                    Intent intent = new Intent(FinanceiroDaVenda.this, BluetoothPrintActivity.class);
                    startActivity(intent);
                });
            } else {
                // Se já houve duas negativas ou não é uma promissória, não ativa o Bluetooth
                //Toast.makeText(this, "Impressão não necessária ou Bluetooth desativado anteriormente.", Toast.LENGTH_SHORT).show();
                finalizarAcaoPosVenda(); // Chama método para finalizar outras ações de pós-venda
            }
        } else {
            // Para vendas que não estão sendo editadas, prossegue com a verificação normal
            if (iSPromissoria && negativasBluetooth < 2) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(FinanceiroDaVenda.this, BluetoothPrintActivity.class);
                    startActivity(intent);
                });
            } else {
                // Toast.makeText(this, "Impressão cancelada. Venda finalizada sem impressão.", Toast.LENGTH_SHORT).show();
                finalizarAcaoPosVenda();
            }
        }
    }

    private void finalizarAcaoPosVenda() {
        // Método para realizar ações finais após a venda, como retornar à tela principal
        Intent intent = new Intent(this, Principal2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    private void enviarLog(String posCli, String posVen, String raio, String precisao) {
        //
        final ILogin login = ILogin.retrofit.create(ILogin.class);

        //
        final Call<Conta> call = login.enviarLog(
                posCli,
                posVen,
                raio,
                precisao,
                "Marca: " + Build.BRAND + ", Modelo: " + Build.MODEL + ", SDK: " + Build.VERSION.SDK_INT,
                prefs.getString("serial", "")
        );

        call.enqueue(new Callback<Conta>() {
            @Override
            public void onResponse(Call<Conta> call, Response<Conta> response) {

                //
                final Conta sincronizacao = response.body();
                if (sincronizacao != null) {

                    //
                    runOnUiThread(() -> {
                    });
                }
            }

            @Override
            public void onFailure(Call<Conta> call, Throwable t) {
            }
        });
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(FinanceiroDaVenda.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    /**
     *  captura e armazena na list somente as formas de pagamento A_PRAZO
     */

    public void logFormasPagamentoPrazo() {
        formasPagamentoPrazo.clear(); // Limpa a lista antes de adicionar novos valoresS
        if (listaFormasPagamentoCliente != null && !listaFormasPagamentoCliente.isEmpty()) {
            for (String formaPagamento : listaFormasPagamentoCliente) {
                String[] detalhesPagamento = formaPagamento.split(" _ ");
                if (detalhesPagamento.length > 1 && "A PRAZO".equals(detalhesPagamento[1])) {
                    formasPagamentoPrazo.add(formaPagamento); // Adiciona à lista
                    Log.d("FormasPagamentoPrazo", "Forma de pagamento a prazo: " + formaPagamento);
                }
            }
        } else {
            Log.d("FormasPagamentoPrazo", "Nenhuma forma de pagamento encontrada.");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Todas as alterações necessárias foram feitas
                        coord.getLocation();
                        //verifCordenadas();
                        break;
                    case Activity.RESULT_CANCELED:
                        // O usuário cancelou o dialog, não fazendo as alterações requeridas
                        Toast.makeText(FinanceiroDaVenda.this, "Operação cancelada!", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(context, "Siac: " + data.getStringExtra("authorizationCode"), Toast.LENGTH_LONG).show();
                }
                //Toast.makeText(context, Objects.requireNonNull(data).getStringExtra("authorizationCode"), Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Todas as alterações necessárias foram feitas
                     ...
                        break;
                    case Activity.RESULT_CANCELED:
                        // O usuário cancelou o dialog, não fazendo as alterações requeridas
                     ...
                        break;
                    default:
                        break;
                }
                break;
        }
    }*/
}