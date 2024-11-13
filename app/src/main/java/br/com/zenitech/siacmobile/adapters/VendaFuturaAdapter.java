package br.com.zenitech.siacmobile.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.zenitech.siacmobile.R;
import br.com.zenitech.siacmobile.domains.VendaFuturaDomain;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;

public class VendaFuturaAdapter extends RecyclerView.Adapter<VendaFuturaAdapter.VendaFuturaViewHolder> {
    private List<VendaFuturaDomain> listaVendasFuturas;

    // Construtor
    public VendaFuturaAdapter(List<VendaFuturaDomain> listaVendasFuturas) {
        this.listaVendasFuturas = listaVendasFuturas;
    }

    @NonNull
    @Override
    public VendaFuturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venda_futura, parent, false);
        return new VendaFuturaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VendaFuturaViewHolder holder, int position) {
        VendaFuturaDomain venda = listaVendasFuturas.get(position);

        // Configura os campos principais
        holder.txtCodigoVenda.setText(String.valueOf(venda.getCodigoVenda()));
        holder.txtCliente.setText(String.valueOf(venda.getCodigoCliente()));
        holder.txtNomeCliente.setText(venda.getNomeCliente());

        // Limpa o container de produtos para evitar duplicação ao reciclar views
        holder.containerProdutos.removeAllViews();

        // Adiciona os produtos dinamicamente no container
        for (ProdutoEmissor produto : venda.getProdutos()) {
            LinearLayout produtoLayout = new LinearLayout(holder.containerProdutos.getContext());
            produtoLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Cria o TextView para o nome do produto
            TextView txtProdutoNome = new TextView(holder.containerProdutos.getContext());
            txtProdutoNome.setText(produto.getNome());
            txtProdutoNome.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            // Cria o TextView para a quantidade do produto
            TextView txtProdutoQuantidade = new TextView(holder.containerProdutos.getContext());
            txtProdutoQuantidade.setText(String.valueOf(produto.getQuantidade()));

            // Adiciona as views ao layout de produto
            produtoLayout.addView(txtProdutoNome);
            produtoLayout.addView(txtProdutoQuantidade);

            // Adiciona o layout de produto ao container de produtos
            holder.containerProdutos.addView(produtoLayout);
        }
    }

    @Override
    public int getItemCount() {
        return listaVendasFuturas.size();
    }

    public static class VendaFuturaViewHolder extends RecyclerView.ViewHolder {
        TextView txtCodigoVenda, txtCliente, txtNomeCliente;
        LinearLayout containerProdutos;

        public VendaFuturaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCodigoVenda = itemView.findViewById(R.id.txtCodigoVenda);
            txtCliente = itemView.findViewById(R.id.txtCliente);
            txtNomeCliente = itemView.findViewById(R.id.txtNomeCliente);
            containerProdutos = itemView.findViewById(R.id.containerProdutos);
        }
    }

    // Método setFilter atualizado para aceitar VendaFuturaDomain
    public void setFilter(List<VendaFuturaDomain> newList) {
        listaVendasFuturas.clear();
        listaVendasFuturas.addAll(newList);
        notifyDataSetChanged();
    }
}
