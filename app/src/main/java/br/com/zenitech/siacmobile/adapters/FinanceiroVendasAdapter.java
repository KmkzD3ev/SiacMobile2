package br.com.zenitech.siacmobile.adapters;

import static br.com.zenitech.siacmobile.FinanceiroDaVenda.bgTotal;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.txtTotalFinanceiro;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.txtTotalItemFinanceiro;
import static br.com.zenitech.siacmobile.FinanceiroDaVenda.txtValorFormaPagamento;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.siacmobile.ClassAuxiliar;
import br.com.zenitech.siacmobile.CreditoPrefs;
import br.com.zenitech.siacmobile.DatabaseHelper;
import br.com.zenitech.siacmobile.R;
import br.com.zenitech.siacmobile.domains.FinanceiroVendasDomain;
import br.com.zenitech.siacmobile.domains.PosApp;

public class FinanceiroVendasAdapter extends RecyclerView.Adapter<FinanceiroVendasAdapter.ViewHolder> {

    private ClassAuxiliar classAuxiliar;
    private Context context;
    private ArrayList<FinanceiroVendasDomain> elementos;
    private ArrayList<String> valoresCompra;
    private CreditoPrefs creditoPrefs;
    private DatabaseHelper bd;

    // Variável de controle para garantir que a restituição ocorra apenas uma vez
    private boolean restituicaoRealizada = false;


    // Construtor do Adapter, recebe o contexto, a lista de elementos e a lista de valores de compra
    public FinanceiroVendasAdapter(Context context, ArrayList<FinanceiroVendasDomain> elementos, ArrayList<String> valoresCompra) {
        this.context = context;
        this.elementos = elementos;
        this.valoresCompra = valoresCompra;
        this.creditoPrefs = new CreditoPrefs(context);
        this.bd = new DatabaseHelper(context);
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //
        View view = inflater.inflate(R.layout.item_financeiro, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //
        final FinanceiroVendasDomain financeiroVendasDomain = elementos.get(position);
        classAuxiliar = new ClassAuxiliar();

        //
        TextView txtFormaPagamento = holder.txtFormaPagamento;
        txtFormaPagamento.setText(financeiroVendasDomain.getFpagamento_financeiro().replace(" _ ", ""));

        //
        TextView total = holder.txtFinanceiro;
        total.setText(classAuxiliar.maskMoney(new BigDecimal(financeiroVendasDomain.getValor_financeiro())));

        holder.btnExcluirFinanceiro.setOnClickListener(v -> {
            // Log para capturar o início do processo de remoção
            Log.d("REMOVER_ITEM", "Removendo item na posição: " + position + ", Forma de pagamento: " + financeiroVendasDomain.getFpagamento_financeiro());

            // Chamada do método de exclusão
            excluirItem(
                    financeiroVendasDomain.getCodigo_financeiro(),
                    financeiroVendasDomain.getId_financeiro_app(),
                    financeiroVendasDomain.getValor_financeiro(),
                    position,
                    financeiroVendasDomain.getFpagamento_financeiro()
            );
        });

        /*String val = classAuxiliar.maskMoney(new BigDecimal(financeiroVendasDomain.getValor_financeiro()));

        if(financeiroVendasDomain.getFpagamento_financeiro().replace(" _ ", "").equalsIgnoreCase("PROMISSORIA")){
            Intent i = new Intent(getContext(), Impressora.class);
            //Intent i = new Intent(getContext(), ImpressoraPOS.class);

            //
            i.putExtra("razao_social", nomeCliente);
            i.putExtra("tel_contato", "");
            i.putExtra("numero", txtDocumentoFormaPagamento.getText().toString());
            i.putExtra("vencimento", txtVencimentoFormaPagamento.getText().toString());
            i.putExtra("valor", val);
            //i.putExtra("valor", financeiroVendasDomain.getValor_financeiro());
            i.putExtra("id_cliente", codigo_cliente);
            i.putExtra("cpfcnpj", cpfcnpjCliente);
            i.putExtra("endereco", enderecoCliente);
            i.putExtra("imprimir", "Promissoria");

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        }*/
    }

    private void deleteItem(int position) {
        elementos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, elementos.size());
        //holder.itemView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //LinearLayout LlList;
        TextView txtFormaPagamento, txtFinanceiro;
        ImageButton btnExcluirFinanceiro;

        public ViewHolder(View itemView) {
            super(itemView);

            //
            //LlList = (LinearLayout) itemView.findViewById(R.id.LlList);
            txtFormaPagamento = (TextView) itemView.findViewById(R.id.txtFormaPagamento);
            txtFinanceiro = (TextView) itemView.findViewById(R.id.txtFinanceiro);
            btnExcluirFinanceiro = (ImageButton) itemView.findViewById(R.id.btnExcluirFinanceiro);
        }
    }

    public void excluirItem(String codigo, String codigo_financeiro_app, String totalVenda, int position, String fpagamento_financeiro) {
        FinanceiroVendasDomain financeiroVendasDomain = new FinanceiroVendasDomain(codigo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        DatabaseHelper bd = new DatabaseHelper(context);
        bd.deleteItemFinanceiro(financeiroVendasDomain);

        // Reduzir o contador de promissórias ou boletos, se aplicável
        if (fpagamento_financeiro.equalsIgnoreCase("PROMISSORIA")) {
            PosApp posApp = bd.getPos();
            bd.updatePosApp(String.valueOf(Integer.parseInt(posApp.getUltpromissoria()) - 1));
        } else if (fpagamento_financeiro.equalsIgnoreCase("BOLETO")) {
            PosApp posApp = bd.getPos();
            bd.updateUltimoBoleto(String.valueOf(Integer.parseInt(posApp.getUltboleto()) - 1));
        }

        // Verificar as formas de pagamento para restituir o limite de credito
        if (fpagamento_financeiro.equalsIgnoreCase("PROMISSORIA") ||
                fpagamento_financeiro.equalsIgnoreCase("VALE CONVENIO") ||
                fpagamento_financeiro.equalsIgnoreCase("CARTAO DEBITO") ||
                fpagamento_financeiro.equalsIgnoreCase("CARTAO CREDITO")) {

            Log.d("RESTITUIR LIMITE", "Forma de pagamento identificada. Restituindo limite de crédito.");
            if (!restituicaoRealizada) {
                // Obter o ID do cliente do CreditoPrefs
                String codigoCliente = creditoPrefs.getIdCliente();

                // Obter o limite original do cliente do CreditoPrefs
                BigDecimal limiteOriginal = new BigDecimal(creditoPrefs.getLimiteCreditoPrimario());
                Log.d("limite originalEX", "excluirItem: limite original" + limiteOriginal);

                // Verifica o limite de crédito disponível
                BigDecimal limiteAtual = new BigDecimal(bd.getLimiteCreditoCliente(codigoCliente));

                // Formatar o valor da venda para usar na restituição
                BigDecimal valorFormaPagamento = new BigDecimal(totalVenda.replaceAll("[^\\d.,]", "").replace(",", ".").trim());

                // Calcular o novo limite após a restituição
                BigDecimal novoLimite = limiteAtual.add(valorFormaPagamento);

                // Verificar se o novo limite excede o limite original
                if (novoLimite.compareTo(limiteOriginal) > 0) {
                    // Se exceder, ajustar o valor a ser restituído
                    BigDecimal valorRestituir = limiteOriginal.subtract(limiteAtual);
                    Log.d("RESTITUIR LIMITE", "Valor ajustado para restituição: " + valorRestituir);

                    if (valorRestituir.compareTo(BigDecimal.ZERO) > 0) {
                        // Restituir o limite de crédito ajustado
                        bd.restituirLimiteCreditoCliente(codigoCliente, valorRestituir);
                        Toast.makeText(context, "Limite de crédito restituído com sucesso!", Toast.LENGTH_SHORT).show();
                        // Marcar a restituição como realizada
                        restituicaoRealizada = true;
                        creditoPrefs.setRestituicaoRealizada(true);


                    } else {
                        Log.d("RESTITUIR LIMITE", "Nenhuma restituição foi feita, o limite já atingiu o valor original.");
                    }
                } else {
                    // Restituir normalmente, sem ultrapassar o limite original
                    bd.restituirLimiteCreditoCliente(codigoCliente, valorFormaPagamento);
                    Toast.makeText(context, "Limite de crédito restituído com sucesso!", Toast.LENGTH_SHORT).show();
                    // Marcar a restituição como realizada
                    restituicaoRealizada = true;
                }
            } else {
                Log.d("RESTITUIR LIMITE", "Restituição já foi realizada para essa venda. Nenhuma ação adicional necessária.");
            }
        }

        // Log antes da tentativa de remoção
        Log.d("ValoresCompra", "Array antes da remoção: " + valoresCompra.toString());

        // Formatar o valor antes de tentar removê-lo do array valoresCompra
        BigDecimal valorFormatadoBD = new BigDecimal(totalVenda.replaceAll("[^\\d.,]", "").replace(",", ".").trim());
        String valorFormatado = String.format("%.2f", valorFormatadoBD).replace(",", ".");

        if (valoresCompra.contains(valorFormatado)) {
            valoresCompra.remove(valorFormatado);
            Log.d("ValoresCompra", "Valor removido: " + valorFormatado);
        } else {
            Log.d("ValoresCompra", "Valor não encontrado no array: " + valorFormatado);
        }

        // Log após a tentativa de remoção
        Log.d("ValoresCompra", "Array após remoção: " + valoresCompra.toString());

        // Remover o item da lista e atualizar o RecyclerView
        elementos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, elementos.size());

        // Atualizar os valores na interface após remoção
        if (elementos.size() != 0) {
            String valor = bd.getValorTotalFinanceiro(codigo_financeiro_app);
            txtTotalItemFinanceiro.setText(classAuxiliar.maskMoney(new BigDecimal(valor)));
        } else {
            txtTotalItemFinanceiro.setText(classAuxiliar.maskMoney(new BigDecimal("0.0")));
        }

        // Subtrair o valor do financeiro pelo valor total de itens
        String valorFinanceiro = String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString()));
        String[] subtrair = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(classAuxiliar.subitrair(subtrair));

        txtValorFormaPagamento.setText(total);

        // Verificar se o valor financeiro é maior que o valor adicionado e ajustar a interface
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(context, R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(context, R.color.transparente));
        }
        try {
            if (valoresCompra.size() > 1) {  // Supondo que valoresCompra é a lista que está sendo acessada
                BigDecimal valorFormaPrazo = new BigDecimal(valoresCompra.get(1));  // Acessa o segundo elemento
                // Resto do código que usa valorFormaPrazo
            } else {
                Log.e("ERRO", "A lista valoresCompra não tem índices suficientes para acesso.");
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("IndexOutOfBoundsExcepT", "Tentativa de acesso a índice que não existe: " + e.getMessage());
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
}