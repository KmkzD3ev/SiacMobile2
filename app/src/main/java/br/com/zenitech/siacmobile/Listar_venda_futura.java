package br.com.zenitech.siacmobile;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.siacmobile.adapters.VendaFuturaAdapter;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;
import br.com.zenitech.siacmobile.domains.VendaFuturaDomain;

public class Listar_venda_futura extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerViewVendasFuturas;
    private VendaFuturaAdapter adapter;
    private DatabaseHelper bd;
    // Lista completa de vendas futuras
    private ArrayList<VendaFuturaDomain> vendasTEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_venda_futura);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Vendas futuras ");



        recyclerViewVendasFuturas = findViewById(R.id.recyclerViewVendasFuturas);
        bd = new DatabaseHelper(this);

       /* vendasTEST = bd.listarDetalhesCompletosVendasFuturas();  // Carrega todas as vendas futuras
        Log.d("ListarVendaFutura", "Número de vendas futuras recuperadas: " + vendasTEST.size());

// Logando os detalhes de cada venda futura
        for (VendaFuturaDomain venda : vendasTEST) {
            Log.d("DetalhesVendaFutura", "Código da Venda: " + venda.getCodigoVenda());
            Log.d("DetalhesVendaFutura", "Código do Cliente: " + venda.getCodigoCliente());
            Log.d("DetalhesVendaFutura", "Nome do Cliente: " + venda.getNomeCliente());

            // Se a venda tem produtos associados, loga cada produto e quantidade
            for (ProdutoEmissor produto : venda.getProdutos()) {
                Log.d("DetalhesVendaFutura", "Produto: " + produto.getNome() + ", Quantidade: " + produto.getQuantidade());
            }
        }*/

        // Configuração do RecyclerView
        recyclerViewVendasFuturas.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar e configurar o adapter com dados vazios
        adapter = new VendaFuturaAdapter(new ArrayList<>()); // Inicialmente vazio
        recyclerViewVendasFuturas.setAdapter(adapter);

        // Carregar dados
        carregarDados();
    }

    private void carregarDados() {
        // Carrega todas as vendas futuras usando o novo método
        List<VendaFuturaDomain> vendasTEST = bd.listarDetalhesCompletosVendasFuturas();
        Log.d("ListarVendaFutura", "Número de vendas futuras recuperadas: " + vendasTEST.size());

        // Loga os detalhes de cada venda futura
        for (VendaFuturaDomain venda : vendasTEST) {
            Log.d("DetalhesVendaFutura", "Código da Venda: " + venda.getCodigoVenda());
            Log.d("DetalhesVendaFutura", "Código do Cliente: " + venda.getCodigoCliente());
            Log.d("DetalhesVendaFutura", "Nome do Cliente: " + venda.getNomeCliente());

            // Se a venda tem produtos associados, loga cada produto e sua quantidade
            for (ProdutoEmissor produto : venda.getProdutos()) {
                Log.d("DetalhesVendaFutura", "Produto: " + produto.getNome() + ", Quantidade: " + produto.getQuantidade());
            }
        }

        // Atualiza o adapter com a lista de vendas futuras carregada
        adapter = new VendaFuturaAdapter(vendasTEST);
        recyclerViewVendasFuturas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Aqui você reutiliza o mesmo menu que já existe
        getMenuInflater().inflate(R.menu.menu_consultar_cliente_vendas, menu);

        // Configura o SearchView reutilizando o que já existe
        MenuItem menuItem = menu.findItem(R.id.menu_search); // O mesmo ID usado na consulta de clientes
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Não fazemos nada no submit
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Chama a função para filtrar a lista de vendas
        filtrarVendasFuturas(newText.toLowerCase());
        return true;
    }




    private void filtrarVendasFuturas(String texto) {
        ArrayList<VendaFuturaDomain> listaFiltrada = new ArrayList<>();

        // Lógica de filtragem para vendas futuras
        for (VendaFuturaDomain venda : vendasTEST) {
            // Cria uma string composta com todas as informações relevantes para a busca
            StringBuilder str = new StringBuilder();

            // Verifica se os campos não são nulos e os adiciona à string para a busca
            if (venda.getNomeCliente() != null) {
                str.append(venda.getNomeCliente().toLowerCase()).append(" ");
            }
            for (ProdutoEmissor produto : venda.getProdutos()) {
                str.append(produto.getNome().toLowerCase()).append(" ");
                str.append(String.valueOf(produto.getQuantidade())).append(" ");
            }
            str.append(String.valueOf(venda.getCodigoCliente())).append(" ");
            str.append(String.valueOf(venda.getCodigoVenda())).append(" ");

            // Se a string composta contiver o texto digitado, adicione à lista filtrada
            if (str.toString().contains(texto)) {
                listaFiltrada.add(venda);
            }
        }

        // Atualiza o adapter com a nova lista filtrada
        adapter.setFilter(listaFiltrada);
    }

   /* private void filtrarVendasFuturas(String texto) {
        ArrayList<ListarVendasDomain> listaFiltrada = new ArrayList<>();

        // Lógica de filtragem para vendas futuras
        for (ListarVendasDomain venda : vendasFuturas) {
            if (venda.getProduto().toLowerCase().contains(texto) ||
                    String.valueOf(venda.getCliente()).contains(texto)) {
                listaFiltrada.add(venda);
            }
        }

        // Atualiza o adapter com a nova lista filtrada
        adapter.setFilter(listaFiltrada);
    }*/
}
