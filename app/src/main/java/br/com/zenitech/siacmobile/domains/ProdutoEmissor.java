package br.com.zenitech.siacmobile.domains;

public class ProdutoEmissor {
    private String produto;
    private String quantidade;
    private String valorUnitario;

    public ProdutoEmissor(String produto, String quantidade, String valorUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    public String getNome() {
        return produto;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public String getValorUnitario() {
        return valorUnitario;
    }
    // Setters
    public void setNome(String nome) {
        this.produto = produto;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public void setValorUnitario(String valorUnitario) {
        this.valorUnitario = valorUnitario;
    }


    @Override
    public String toString() {
        return "Produto: " + produto + ", Quantidade: " + quantidade + ", Valor Unitário: " + valorUnitario;
    }
}
