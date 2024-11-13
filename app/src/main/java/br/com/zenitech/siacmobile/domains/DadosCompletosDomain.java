package br.com.zenitech.siacmobile.domains;

import java.util.ArrayList;

public class DadosCompletosDomain {

    // Atributos da venda principal
    private String codigoVenda;
    private String codigoVendaApp;
    private String codigoCliente;
    private String nomeCliente;
    private String unidadeVenda;
    private String produtoVenda;
    private String dataMovimento;
    private String quantidadeVenda;
    private String precoUnitario;
    private String valorTotal;
    private String vendedorVenda;
    private String statusAutorizacaoVenda;
    private String entregaFuturaVenda;
    private String entregaFuturaRealizada;
    private String usuarioAtual;
    private String dataCadastro;
    private String vendaFinalizadaApp;
    private String chaveImportacao;

    // Lista de produtos
    private ArrayList<ProdutoEmissor> produtosVenda;

    // Total de itens no pedido
    private int totalItens;

    // Construtor padrão
    public DadosCompletosDomain() {
        this.produtosVenda = new ArrayList<>();
    }

    // Getters e Setters
    public String getCodigoVenda() { return codigoVenda; }
    public void setCodigoVenda(String codigoVenda) { this.codigoVenda = codigoVenda; }

    public String getCodigoVendaApp() { return codigoVendaApp; }
    public void setCodigoVendaApp(String codigoVendaApp) { this.codigoVendaApp = codigoVendaApp; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getUnidadeVenda() { return unidadeVenda; }
    public void setUnidadeVenda(String unidadeVenda) { this.unidadeVenda = unidadeVenda; }

    public String getProdutoVenda() { return produtoVenda; }
    public void setProdutoVenda(String produtoVenda) { this.produtoVenda = produtoVenda; }

    public String getDataMovimento() { return dataMovimento; }
    public void setDataMovimento(String dataMovimento) { this.dataMovimento = dataMovimento; }

    public String getQuantidadeVenda() { return quantidadeVenda; }
    public void setQuantidadeVenda(String quantidadeVenda) { this.quantidadeVenda = quantidadeVenda; }

    public String getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(String precoUnitario) { this.precoUnitario = precoUnitario; }

    public String getValorTotal() { return valorTotal; }
    public void setValorTotal(String valorTotal) { this.valorTotal = valorTotal; }

    public String getVendedorVenda() { return vendedorVenda; }
    public void setVendedorVenda(String vendedorVenda) { this.vendedorVenda = vendedorVenda; }

    public String getStatusAutorizacaoVenda() { return statusAutorizacaoVenda; }
    public void setStatusAutorizacaoVenda(String statusAutorizacaoVenda) { this.statusAutorizacaoVenda = statusAutorizacaoVenda; }

    public String getEntregaFuturaVenda() { return entregaFuturaVenda; }
    public void setEntregaFuturaVenda(String entregaFuturaVenda) { this.entregaFuturaVenda = entregaFuturaVenda; }

    public String getEntregaFuturaRealizada() { return entregaFuturaRealizada; }
    public void setEntregaFuturaRealizada(String entregaFuturaRealizada) { this.entregaFuturaRealizada = entregaFuturaRealizada; }

    public String getUsuarioAtual() { return usuarioAtual; }
    public void setUsuarioAtual(String usuarioAtual) { this.usuarioAtual = usuarioAtual; }

    public String getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(String dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getVendaFinalizadaApp() { return vendaFinalizadaApp; }
    public void setVendaFinalizadaApp(String vendaFinalizadaApp) { this.vendaFinalizadaApp = vendaFinalizadaApp; }

    public String getChaveImportacao() { return chaveImportacao; }
    public void setChaveImportacao(String chaveImportacao) { this.chaveImportacao = chaveImportacao; }

    public ArrayList<ProdutoEmissor> getProdutosVenda() { return produtosVenda; }
    public void setProdutosVenda(ArrayList<ProdutoEmissor> produtosVenda) { this.produtosVenda = produtosVenda; }

    public int getTotalItens() { return totalItens; }
    public void setTotalItens(int totalItens) { this.totalItens = totalItens; }
}
