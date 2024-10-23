package br.com.zenitech.siacmobile.domains;


public class ListarVendasDomain {
    private int codigoVenda;
    private int cliente;
    private String nomeCliente; // Adicionado o nome do cliente
    private String produto;
    private int quantidade;
    private double valorTotal;
    private String unidade;
    private double precoUnitario;

    // Construtor
    public ListarVendasDomain(int codigoVenda, int cliente, String nomeCliente, String produto, int quantidade, double valorTotal, String unidade, double precoUnitario) {
        this.codigoVenda = codigoVenda;
        this.cliente = cliente;
        this.nomeCliente = nomeCliente; // Inicializa o nome do cliente
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
    }

    // Getters
    public int getCodigoVenda() {
        return codigoVenda;
    }

    public int getCliente() {
        return cliente;
    }

    public String getNomeCliente() {
        return nomeCliente; // Getter para nome do cliente
    }

    public String getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    // Setters
    public void setCodigoVenda(int codigoVenda) {
        this.codigoVenda = codigoVenda;
    }

    public void setCliente(int cliente) {
        this.cliente = cliente;
    }

    public void setNomeCliente(String nomeCliente) { // Setter para nome do cliente
        this.nomeCliente = nomeCliente;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}
