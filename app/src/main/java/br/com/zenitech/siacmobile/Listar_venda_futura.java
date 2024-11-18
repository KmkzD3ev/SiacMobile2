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
    private ArrayList<VendaFuturaDomain> vendasTEST;  // Lista completa de vendas futuras
    private ArrayList<VendaFuturaDomain> vendasFiltradas; // Lista usada para a filtragem

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_venda_futura);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Vendas futuras ");

        recyclerViewVendasFuturas = findViewById(R.id.recyclerViewVendasFuturas);
        bd = new DatabaseHelper(this);

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
        vendasTEST = bd.listarDetalhesCompletosVendasFuturas();
        Log.d("ListarVendaFutura", "Número de vendas futuras recuperadas: " + vendasTEST.size());

        // Mantém uma cópia para a filtragem
        vendasFiltradas = new ArrayList<>(vendasTEST);

        // Atualiza o adapter com a lista completa
        adapter = new VendaFuturaAdapter(vendasFiltradas);
        recyclerViewVendasFuturas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_consultar_cliente_vendas, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false; // Não faz nada ao submeter o texto
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            // Se o campo de busca está vazio, restaurar a lista original
            adapter.setFilter(vendasTEST);
        } else {
            // Filtra a lista de vendas conforme o texto
            filtrarVendasFuturas(newText.toLowerCase());
        }
        return true;
    }

    private void filtrarVendasFuturas(String texto) {
        ArrayList<VendaFuturaDomain> listaFiltrada = new ArrayList<>();

        // Lógica de filtragem para vendas futuras
        for (VendaFuturaDomain venda : vendasTEST) {
            StringBuilder str = new StringBuilder();

            // Adiciona campos relevantes para a busca
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
}
