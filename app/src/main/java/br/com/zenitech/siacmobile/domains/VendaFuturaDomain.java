package br.com.zenitech.siacmobile.domains;

import java.util.ArrayList;

public class VendaFuturaDomain {

    private int codigoVenda;
    private int codigoCliente;
    private String nomeCliente;
    private ArrayList<ProdutoEmissor> produtos;

    public VendaFuturaDomain(int codigoVenda, int codigoCliente, String nomeCliente, ArrayList<ProdutoEmissor> produtos) {
        this.codigoVenda = codigoVenda;
        this.codigoCliente = codigoCliente;
        this.nomeCliente = nomeCliente;
        this.produtos = produtos;
    }

    public int getCodigoVenda() {
        return codigoVenda;
    }

    public void setCodigoVenda(int codigoVenda) {
        this.codigoVenda = codigoVenda;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public ArrayList<ProdutoEmissor> getProdutos() {
        return produtos;
    }

    public void setProdutos(ArrayList<ProdutoEmissor> produtos) {
        this.produtos = produtos;
    }
}
