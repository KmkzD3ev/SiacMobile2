package br.com.zenitech.siacmobile;

import static br.com.zenitech.siacmobile.Configuracoes.getApplicationName;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.siacmobile.adapters.RelatorioVendasPedidosAdapter;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;
import br.com.zenitech.siacmobile.domains.VendasPedidosDomain;
import stone.application.StoneStart;
import stone.utils.Stone;

public class RelatorioVendasPedido extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private AlertDialog alerta;
    private DatabaseHelper bd;
    ArrayList<VendasPedidosDomain> vendasDomains;
    RelatorioVendasPedidosAdapter adapter;
    private RecyclerView rvRelatorioVendas;
    ClassAuxiliar classAuxiliar;
    private Context context;
    private LinearLayout erroRelatorio;
    private Button venderProdutos;
    Configuracoes configuracoes;
    VendasPedidosDomain pedidos;
    TextView txtProdutoRelatorioVendas, txtQuantidadeRelatorioVendas, txtTotalRelatorioVendas, txtFormsPagRelat;
    String strFormPags = "";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_vendas_pedido);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();
        bd = new DatabaseHelper(this);
        classAuxiliar = new ClassAuxiliar();
        rvRelatorioVendas = findViewById(R.id.rvRelatorioVendas);
        rvRelatorioVendas.setLayoutManager(new LinearLayoutManager(context));
        vendasDomains = bd.getRelatorioVendasPedidos();
        adapter = new RelatorioVendasPedidosAdapter(this, vendasDomains);
        rvRelatorioVendas.setAdapter(adapter);
        txtFormsPagRelat = findViewById(R.id.txtFormsPagRelat);
        strFormPags = bd.getFormPagRelatorioVendasPedidos();
        txtFormsPagRelat.setText(strFormPags);
        txtProdutoRelatorioVendas = findViewById(R.id.txtProdutoRelatorioVendas);
        txtQuantidadeRelatorioVendas = findViewById(R.id.txtQuantidadeRelatorioVendas);
        txtTotalRelatorioVendas = findViewById(R.id.txtTotalRelatorioVendas);

        // Iterando sobre cada venda para logar os produtos associados
        for (VendasPedidosDomain venda : vendasDomains) {
            String codigoVendaApp = venda.getCodigo_venda_app();  // Pega o ID da venda atual

            // Chama a função getProdutosVenda para buscar os produtos relacionados a essa venda
            ArrayList<ProdutoEmissor> produtosVenda = bd.getProdutosVenda(codigoVendaApp);

            // Loga os produtos dessa venda para verificar a correspondência
            Log.d("ProdutosVenda", "Produtos para código de venda: " + codigoVendaApp);
            for (ProdutoEmissor produto : produtosVenda) {
                Log.d("ProdutosVenda", produto.toString());
            }
        }

        if (vendasDomains.isEmpty()) {
            erroRelatorio = findViewById(R.id.erroRelatorio);
            erroRelatorio.setVisibility(View.VISIBLE);
            venderProdutos = findViewById(R.id.venderProdutos);
            venderProdutos.setOnClickListener(v -> {
                startActivity(new Intent(context, VendasConsultarClientes.class));
                finish();
            });
        } else {
            String quantItens = "0";
            String valTotalPed = "0";

            for (int n = 0; n < vendasDomains.size(); n++) {
                pedidos = vendasDomains.get(n);
                String[] somarItens = {quantItens, pedidos.getQuantidade_venda()};
                quantItens = String.valueOf(classAuxiliar.somar(somarItens));
                String[] somarValTot = {valTotalPed, pedidos.getValor_total()};
                valTotalPed = String.valueOf(classAuxiliar.somar(somarValTot));
            }
            int totalQuantidadeProdutos = bd.getTotalQuantidadeProdutos();
            Log.d("RelatorioVendasPedido", "Quantidade total de produtos: " + totalQuantidadeProdutos);

            txtProdutoRelatorioVendas.setText(String.valueOf(vendasDomains.size()));
            double s = Double.parseDouble(quantItens);
            txtQuantidadeRelatorioVendas.setText(String.valueOf(totalQuantidadeProdutos));

            SharedPreferences prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("totalItensVendidos", totalQuantidadeProdutos);
            editor.apply();  // ou editor.commit() para garantir a escrita síncrona

            // txtTotalRelatorioVendas.setText(String.valueOf(classAuxiliar.maskMoney(new BigDecimal(valTotalPed))));
        }




        /**

         * ********* Caso Usuario realise uma venda sem impressao **************
         * ********* SOLICITA NOVAMENTE AS PERMISSOES NESSA TELA  **************/

        configuracoes = new Configuracoes();
        findViewById(R.id.btnPrintRelPed).setOnClickListener(v -> {
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
        });

        logTotalDeVendas();

        if (configuracoes.GetDevice()) {
            iniciarStone();
        }
    }

    private void logTotalDeVendas() {
        BigDecimal totalDeVendas = bd.calcularTotalVendas();
        Log.d("RelatorioVendasPEDIDO", "Total de vendas: " + totalDeVendas.toPlainString());

        // Atualiza o TextView com o valor total das vendas
        runOnUiThread(() -> {
            txtTotalRelatorioVendas.setText(classAuxiliar.maskMoney(totalDeVendas));

        });
    }


    void iniciarStone() {
        StoneStart.init(context);
        Stone.setAppName(getApplicationName(context));
    }

    private void iniciarImpressora() {
        closeActiveConnection(); // Adiciona esta linha para fechar conexões ativas
        Intent i;

        if (configuracoes.GetDevice()) {
            i = new Intent(context, ImpressoraPOS.class);
        } else {
            i = new Intent(context, Impressora.class);
        }

        i.putExtra("imprimir", "relatorio");
        startActivity(i);
    }

    /********************** TRATAMENTO DAS PERMISSOES ***************/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Permissões Bluetooth necessárias para imprimir o relatório.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }
            }
            iniciarImpressora();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.finish();
        }

        return super.onOptionsItemSelected(item);
    }
/*******************FECHA OUTRAS CONEXOES PRA EVITAR ERROS*****************/

    private synchronized void closeActiveConnection() {
        Impressora impressora = new Impressora();
        impressora.closeBluetoothConnection();
    }
}