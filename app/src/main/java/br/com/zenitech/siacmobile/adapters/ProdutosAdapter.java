package br.com.zenitech.siacmobile.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.siacmobile.ClassAuxiliar;
import br.com.zenitech.siacmobile.DatabaseHelper;
import br.com.zenitech.siacmobile.R;
import br.com.zenitech.siacmobile.domains.ProdutoEmissor;

import static br.com.zenitech.siacmobile.Vendas.textTotalItens;
import static br.com.zenitech.siacmobile.Vendas.txtTotalVenda;

public class ProdutosAdapter extends RecyclerView.Adapter<ProdutosAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ProdutoEmissor> listaProdutos;
    private DatabaseHelper bd;
    private ClassAuxiliar classAuxiliar;
    private String codigoVendaApp; // Código da venda do `VendasDomain`

    public ProdutosAdapter(Context context, ArrayList<ProdutoEmissor> listaProdutos, String codigoVendaApp) {
        this.context = context;
        this.listaProdutos = listaProdutos;
        this.bd = new DatabaseHelper(context);
        this.classAuxiliar = new ClassAuxiliar();
        this.codigoVendaApp = codigoVendaApp;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vendas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProdutoEmissor produto = listaProdutos.get(position);

        holder.txtProduto.setText(produto.getNome());
        holder.txtQuantidade.setText(produto.getQuantidade());
        holder.txtValor.setText(classAuxiliar.maskMoney(new BigDecimal(produto.getValorUnitario())));

        double totalProduto = Double.parseDouble(produto.getQuantidade()) * Double.parseDouble(produto.getValorUnitario());
        holder.txtTotal.setText(String.format("R$ %.2f", totalProduto));

        holder.btnExcluirVenda.setVisibility(View.VISIBLE);
        holder.btnExcluirVenda.setOnClickListener(v -> excluirItem(produto.getNome(), codigoVendaApp, totalProduto, position));
    }

    @Override
    public int getItemCount() {
        return listaProdutos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtProduto, txtQuantidade, txtValor, txtTotal;
        ImageButton btnExcluirVenda;

        public ViewHolder(View itemView) {
            super(itemView);
            txtProduto = itemView.findViewById(R.id.txtProduto);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            txtValor = itemView.findViewById(R.id.txtValor);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            btnExcluirVenda = itemView.findViewById(R.id.btnExcluirVenda);
        }
    }
    public void excluirItem(String produto, String codigoVendaApp, double totalProduto, int position) {
        // Captura o produto a ser removido
        ProdutoEmissor produtoRemovido = listaProdutos.get(position);
        double valorProdutoRemovido = Double.parseDouble(produtoRemovido.getQuantidade()) * Double.parseDouble(produtoRemovido.getValorUnitario());
        Log.d("Exclusão", "Produto a ser excluído: " + produtoRemovido.getNome() + ", Quantidade: " + produtoRemovido.getQuantidade() + ", Valor unitário: " + produtoRemovido.getValorUnitario());
        Log.d("Exclusão", "Valor total do produto a ser removido: " + valorProdutoRemovido);

        // Tentativa de remoção do produto do banco de dados
        int linhasDeletadas = bd.deleteProdutoVenda(produto, codigoVendaApp);
        Log.d("Exclusão BD", "Linhas deletadas: " + linhasDeletadas);

        if (linhasDeletadas > 0) {
            // Remoção do produto da lista de visualização
            listaProdutos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaProdutos.size());


            SharedPreferences prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("vendaAtualizada", true);
            editor.apply();

            // Log para verificar se a flag foi salva corretamente
            boolean vendaAtualizada = prefs.getBoolean("vendaAtualizada", false);
            Log.d("LOG ADAPTER", "Flag vendaAtualizada salva no SharedPreferences: " + vendaAtualizada);



            // Recalcula o total com os produtos restantes
            BigDecimal totalAtual = BigDecimal.ZERO;
            for (ProdutoEmissor item : listaProdutos) {
                double valorItem = Double.parseDouble(item.getQuantidade()) * Double.parseDouble(item.getValorUnitario());
                totalAtual = totalAtual.add(BigDecimal.valueOf(valorItem));
                Log.d("Atualização Total", "Produto: " + item.getNome() + ", Total Item: " + valorItem);
            }

            // Atualiza a visualização com o novo total
            txtTotalVenda.setText(classAuxiliar.maskMoney(totalAtual));
            textTotalItens.setText(String.valueOf(listaProdutos.size()));
            Log.d("Exclusão", "Produto removido com sucesso. Total de itens atualizado: " + listaProdutos.size());
            Log.d("Exclusão", "Novo total após remoção: " + classAuxiliar.maskMoney(totalAtual));

        } else {
            Log.e("Exclusão", "Falha ao excluir o produto. Verifique se ele existe no banco de dados.");
        }
    }


    private int obterTotalItensPedido() {
        int totalItens = 0;

        // Recupera o código da venda atual
      //  String codigoVendaApp = String.valueOf(id_venda_app);

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

        Log.d("TotalItensADAPTER", "Total de itens no pedido atual: " + totalItens);

        return totalItens;
    }

}
