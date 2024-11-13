package br.com.zenitech.siacmobile.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.siacmobile.R;
import br.com.zenitech.siacmobile.domains.ListarVendasDomain;

public class   VendaFuturaAdapter extends RecyclerView.Adapter<VendaFuturaAdapter.VendaFuturaViewHolder> {
    private List<ListarVendasDomain> listaVendasFuturas;

    // Construtor
    public VendaFuturaAdapter(List<ListarVendasDomain> listaVendasFuturas) {
        this.listaVendasFuturas = listaVendasFuturas;
    }

    @NonNull
    @Override
    public VendaFuturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venda_futura, parent, false);
        return new VendaFuturaViewHolder(itemView);
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull VendaFuturaViewHolder holder, int position) {
        ListarVendasDomain venda = listaVendasFuturas.get(position);
        holder.txtCodigoVenda.setText(String.valueOf(venda.getCodigoVenda()));
        holder.txtCliente.setText(String.valueOf(venda.getCliente()));  // Exibe o código do cliente corretamente
        holder.txtNomeCliente.setText(venda.getNomeCliente());  // Exibe o nome do cliente corretamente
        holder.txtProduto.setText(venda.getProduto());
        holder.txtQuantidade.setText(String.valueOf(venda.getQuantidade()));
        holder.txtValorTotal.setText(String.format("R$ %.2f", venda.getValorTotal()));
        holder.txtUnidade.setText(venda.getUnidade());
        holder.txtPrecoUnitario.setText(String.format("R$ %.2f", venda.getPrecoUnitario()));
    }


    @Override
    public int getItemCount() {
        return listaVendasFuturas.size();
    }

    // ViewHolder inner class
    public static class VendaFuturaViewHolder extends RecyclerView.ViewHolder {
        TextView txtCodigoVenda, txtCliente, txtNomeCliente, txtProduto, txtQuantidade, txtValorTotal, txtUnidade, txtPrecoUnitario;

        public VendaFuturaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCodigoVenda = itemView.findViewById(R.id.txtCodigoVenda);
            txtCliente = itemView.findViewById(R.id.txtCliente);  // Campo para o código do cliente
            txtNomeCliente = itemView.findViewById(R.id.txtNomeCliente);  // Campo para o nome do cliente
            txtProduto = itemView.findViewById(R.id.txtProduto);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            txtValorTotal = itemView.findViewById(R.id.txtValorTotal);
            txtUnidade = itemView.findViewById(R.id.txtUnidade);
            txtPrecoUnitario = itemView.findViewById(R.id.txtPrecoUnitario);
        }
    }

    public void setFilter(List<ListarVendasDomain> newList) {
        listaVendasFuturas = new ArrayList<>();
        listaVendasFuturas.addAll(newList);
        notifyDataSetChanged();
    }
}
