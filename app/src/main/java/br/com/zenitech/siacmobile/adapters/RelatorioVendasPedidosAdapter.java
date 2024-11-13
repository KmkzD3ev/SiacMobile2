package br.com.zenitech.siacmobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.siacmobile.ClassAuxiliar;
import br.com.zenitech.siacmobile.DatabaseHelper;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;
import br.com.zenitech.siacmobile.R;
import br.com.zenitech.siacmobile.RelatorioVendasCliente;
import br.com.zenitech.siacmobile.domains.VendasPedidosDomain;

public class RelatorioVendasPedidosAdapter extends RecyclerView.Adapter<RelatorioVendasPedidosAdapter.ViewHolder> {

    private ClassAuxiliar classAuxiliar;
    private Context context;
    private ArrayList<VendasPedidosDomain> elementos;
    private DatabaseHelper bd;

    public RelatorioVendasPedidosAdapter(Context context, ArrayList<VendasPedidosDomain> elementos) {
        this.context = context;
        this.elementos = elementos;
        this.bd = new DatabaseHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_relatorio_vendas_pedidos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VendasPedidosDomain vendasDomain = elementos.get(position);
        classAuxiliar = new ClassAuxiliar();

        // Obter lista de produtos para a venda atual
        String codigoVendaApp = vendasDomain.getCodigo_venda_app();
        ArrayList<ProdutoEmissor> produtosVenda = bd.getProdutosVenda(codigoVendaApp);


        // Exibir dados básicos da venda
        //holder.txtProduto.setText(vendasDomain.getProduto_venda());
        holder.txtFormasPagamento.setText(vendasDomain.getFormas_pagamento());
      //  holder.txtQuantidade.setText(vendasDomain.getQuantidade_venda());
        holder.txtNomeCliente.setText(vendasDomain.getCodigo_cliente());
      //  holder.txtTotal.setText("R$ " + classAuxiliar.maskMoney(new BigDecimal(vendasDomain.getValor_total())));

        // Logando após vinculação dos dados aos TextViews
       // Log.d("ViewHolder", "Produto TextView: " + holder.txtProduto.getText().toString());
        Log.d("ViewHolder", "Formas de Pagamento TextView: " + holder.txtFormasPagamento.getText().toString());
      //  Log.d("ViewHolder", "Quantidade TextView: " + holder.txtQuantidade.getText().toString());
        Log.d("ViewHolder", "Nome Cliente TextView: " + holder.txtNomeCliente.getText().toString());
      //  Log.d("ViewHolder", "Total TextView: " + holder.txtTotal.getText().toString());

        // Limpar o container de produtos para evitar duplicação ao reciclar views
        holder.containerProdutos.removeAllViews();

        // Adicionar produtos ao container de forma detalhada
        for (ProdutoEmissor produto : produtosVenda) {
            // Cria um layout horizontal para cada produto
            LinearLayout productLayout = new LinearLayout(context);
            productLayout.setOrientation(LinearLayout.HORIZONTAL);
            productLayout.setLayoutParams(new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Define TextView para o nome do produto
            TextView produtoTextView = new TextView(context);
            produtoTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)); // Reduz a proporção
            produtoTextView.setText(produto.getNome());
            produtoTextView.setTextSize(14);
            produtoTextView.setTypeface(null, Typeface.BOLD);
           // produtoTextView.setTextColor(Color.RED);

            // Define TextView para a quantidade
            TextView quantidadeTextView = new TextView(context);
            quantidadeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Mantém a proporção
            quantidadeTextView.setText(String.valueOf(produto.getQuantidade()));
            quantidadeTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            quantidadeTextView.setTextSize(14);

            // Define TextView para o valor unitário
            TextView valorUnitarioTextView = new TextView(context);
            valorUnitarioTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Mantém a proporção
            valorUnitarioTextView.setText(classAuxiliar.maskMoney(new BigDecimal(produto.getValorUnitario())));
            valorUnitarioTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            valorUnitarioTextView.setTextSize(14);

            // Adiciona os TextViews ao layout horizontal
            productLayout.addView(produtoTextView);
            productLayout.addView(quantidadeTextView);
            productLayout.addView(valorUnitarioTextView);

            // Adiciona o layout do produto ao container vertical
            holder.containerProdutos.addView(productLayout);

            // Log para verificação
            Log.d("ProdutosVenda", "Produto adicionado: " + produto.toString());
        }

        // Configurar evento de clique
        holder.llRelatorioVendas.setOnClickListener(v -> {
            Intent i = new Intent(context, RelatorioVendasCliente.class);
            i.putExtra("produto", vendasDomain.getProduto_venda());
            i.putExtra("quantidade", vendasDomain.getQuantidade_venda());
            i.putExtra("total", classAuxiliar.maskMoney(new BigDecimal(vendasDomain.getValor_total())));
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llRelatorioVendas, containerProdutos;
        TextView  txtValor, txtNomeCliente, txtFormasPagamento;
        ImageButton btnExcluirVenda;

        public ViewHolder(View itemView) {
            super(itemView);
            llRelatorioVendas = itemView.findViewById(R.id.llRelatorioVendas);
            //txtProduto = itemView.findViewById(R.id.txtProduto);
            //txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            txtValor = itemView.findViewById(R.id.txtValor);
           // txtTotal = itemView.findViewById(R.id.txtTotal);
            txtNomeCliente = itemView.findViewById(R.id.txtNomeCliente);
            txtFormasPagamento = itemView.findViewById(R.id.txtFormasPagamento);
            btnExcluirVenda = itemView.findViewById(R.id.btnExcluirVenda);
            containerProdutos = itemView.findViewById(R.id.containerProdutos);
        }
    }
}
