package br.com.zenitech.siacmobile;

import static br.com.zenitech.siacmobile.FinanceiroDaVenda.codigo_cliente;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.cpfcnpjCliente;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.enderecoCliente;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.nomeCliente;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.siacmobile.domains.FinanceiroVendasDomain;

public class BluetoothPrintActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2; // Código para solicitar ativação do Bluetooth
    private DatabaseHelper bd;
    private ArrayList<FinanceiroVendasDomain> listaFinanceiroCliente; // A lista que irá armazenar os dados de vendas
    private ClassAuxiliar classAuxiliar;
    private Configuracoes configuracoes;
    private SharedPreferences prefs; // Inicialização do SharedPreferences
    private static final String PREFS_NAME = "preferencias";
    private static final String PREF_USER_DECLINED_BT = "user_declined_bt";
    private SharedPreferences prefsBluetooth;
    private TextView tvErrorMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_print);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Impressora Bluetooth");


        // Inicializar o DatabaseHelper
        bd = new DatabaseHelper(this);
        classAuxiliar = new ClassAuxiliar();
        configuracoes = new Configuracoes();

        // Inicializar o SharedPreferences
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // Inicializar o SharedPreferences com nome mais sugestivo
        prefsBluetooth = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Solicitar permissões de Bluetooth
        solicitarPermissoesBluetooth();

        // Carregar os dados de vendas
        carregarDadosVendas();
        TextView tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // Verificar o contador de negativas de ativação do Bluetooth
        int negativas = prefsBluetooth.getInt("negativas_bt", 0);
        if (negativas >= 1) {
            //exibirAlertaBluetoothNegado();
           // Toast.makeText(this, "PRIMEIRA NEGATIVA ENCONTRADA", Toast.LENGTH_SHORT).show();
        } else {
            // Se não houver negativas e as permissões já estiverem concedidas, tenta ativar o Bluetooth diretamente
          //  ativarBluetooth();
        }


        // Verificar se o usuário já negou a ativação do Bluetooth
        if (prefsBluetooth.getBoolean(PREF_USER_DECLINED_BT, false)) {
            tvErrorMessage.setText("Bluetooth não ativado, impossível imprimir.");
            tvErrorMessage.setVisibility(View.VISIBLE);
        } else {
            // Solicitar permissões de Bluetooth
           // ativarBluetooth();
        }

        // Configurar botão de impressão
        Button btnPrint = findViewById(R.id.btn_print);
        btnPrint.setOnClickListener(v -> chamarImpressora());

        // Configurar botão Home
        Button btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(BluetoothPrintActivity.this, Principal2.class);
            startActivity(intent);
        });
    }


    private void carregarDadosVendas() {
        // Método para carregar as vendas a partir do banco de dados
        listaFinanceiroCliente = bd.getFinanceiroCliente(prefs.getInt("id_venda_app", 1)); // Ajustar conforme necessário

        if (listaFinanceiroCliente == null || listaFinanceiroCliente.isEmpty()) {
            tvErrorMessage.setText("Dados da venda com Erro.");
            tvErrorMessage.setVisibility(View.VISIBLE);
            finish(); // Encerra a atividade se não houver vendas
        }
    }

    private void solicitarPermissoesBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ativarBluetooth();
            } else {
                tvErrorMessage.setText("Permissoes nao Concedidas, impossível imprimir.");
                tvErrorMessage.setVisibility(View.VISIBLE);
                // Incrementa o contador de negativas no SharedPreferences
                int negativas = prefsBluetooth.getInt("negativas_bt", 0) + 1;
                prefsBluetooth.edit().putInt("negativas_bt", negativas).apply();
                //exibirAlertaBluetoothNegado();
            }
        }
    }


    private void ativarBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                chamarImpressora();  // Caso o Bluetooth tenha sido ativado com sucesso
            } else {
                // Usuário negou a ativação do Bluetooth, incrementar o contador
                tvErrorMessage.setText("Bluetooth não ativado, impossível imprimir.");
                tvErrorMessage.setVisibility(View.VISIBLE);
                int negativas = prefsBluetooth.getInt("negativas_bt", 0) + 1;
                prefsBluetooth.edit().putInt("negativas_bt", negativas).apply();
                if (negativas >= 2) {
                    exibirAlertaBluetoothNegado();

                }
            }
        }
    }

    private void exibirAlertaBluetoothNegado() {
        // Verificar se o alerta já foi mostrado
        if (!prefsBluetooth.getBoolean("alert_shown", false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ativação de Bluetooth Necessária");
            builder.setMessage("A ativação do Bluetooth é necessária para imprimir. Deseja ativá-lo agora?");
            builder.setPositiveButton("Ativar", (dialog, which) -> {
                ativarBluetooth();
            });
            builder.setNegativeButton("Não exibir novamente", (dialog, which) -> {
                prefsBluetooth.edit().putBoolean("alert_shown", true).apply(); // Salvar a preferência para não mostrar novamente
            });
            builder.setCancelable(false);
            builder.show();

            // Marcar que o alerta foi mostrado
            prefsBluetooth.edit().putBoolean("alert_shown", true).apply();
        }
    }



    private void chamarImpressora() {
        int v = 0; // Contador para controle de impressão

        for (FinanceiroVendasDomain financeiro : listaFinanceiroCliente) {
            // IMPRESSÃO DA PROMISSORIA
            if (financeiro.getFpagamento_financeiro().replace(" _ ", "").contains("PROMISSORIA")) {
                v++;
                String val = classAuxiliar.maskMoney(new BigDecimal(financeiro.getValor_financeiro()));
                Intent i = new Intent(this, configuracoes.GetDevice() ? ImpressoraPOS.class :
                        Impressora.class);

                // Passar dados para a impressão
                i.putExtra("razao_social", nomeCliente);
                i.putExtra("tel_contato", "");
                i.putExtra("numero", financeiro.getDocumento_financeiro());
                i.putExtra("vencimento", financeiro.getVencimento_financeiro());
                i.putExtra("valor", val);
                i.putExtra("id_cliente", codigo_cliente);
                i.putExtra("cpfcnpj", cpfcnpjCliente);
                i.putExtra("endereco", enderecoCliente);
                i.putExtra("imprimir", "Promissoria");

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }

            // IMPRESSÃO DO BOLETO
            String contaBancFormPag = bd.getContaBancariaFormaPagamento(financeiro.getFpagamento_financeiro().replace(" _ ", ""));
            if (!contaBancFormPag.equalsIgnoreCase("0")) {
                String CodContaBanc = bd.getCodContaBancaria(contaBancFormPag);
                if (CodContaBanc.equalsIgnoreCase("001") || CodContaBanc.equalsIgnoreCase("237")) {
                    String val = classAuxiliar.maskMoney(new BigDecimal(financeiro.getValor_financeiro()));
                    Intent i = new Intent(this, Impressora.class);
                    i.putExtra("razao_social", nomeCliente);
                    i.putExtra("tel_contato", "");
                    i.putExtra("numero", financeiro.getDocumento_financeiro());
                    i.putExtra("vencimento", financeiro.getVencimento_financeiro());
                    i.putExtra("valor", val);
                    i.putExtra("id_cliente", codigo_cliente);
                    i.putExtra("cpfcnpj", cpfcnpjCliente);
                    i.putExtra("endereco", enderecoCliente);
                    i.putExtra("imprimir", "Boleto");
                    i.putExtra("nota_fiscal", financeiro.getNota_fiscal());
                    i.putExtra("nContaBanco", contaBancFormPag);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        }

        if (v == 0) finish(); // Se nenhuma impressão foi feita, fecha a atividade
    }
}