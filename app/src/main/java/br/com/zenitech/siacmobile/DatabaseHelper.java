package br.com.zenitech.siacmobile;

import static br.com.zenitech.siacmobile.Configuracoes.VERSAO_BD;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;


import br.com.zenitech.siacmobile.domains.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = "DatabaseHelper";
    private final String DB_PATH;
    private static final String DB_NAME = "siacmobileDB";
    public SQLiteDatabase myDataBase;
    final Context context;
    private final ClassAuxiliar aux = new ClassAuxiliar();
    private boolean isPrecoFixo = false; //VARIAVEL PRA GUARDAR RESULTADO DE PREÇO FIXO ENCONTRADO

    //CONSTANTES CLIENTES
    private static final String TABELA_CLIENTES = "clientes";
    private static final String CODIGO_CLIENTE = "codigo_cliente";
    private static final String NOME_CLIENTE = "nome_cliente";
    private static final String LATITUDE_CLIENTE = "latitude_cliente";
    private static final String LONGITUDE_CLIENTE = "longitude_cliente";
    private static final String SALDO_CLIENTE = "saldo";
    private static final String LIMITE_CREDITO_CLIENTE = "limite_credito_cliente"; // Constante para o campo limite_credito_cliente

    private static final String[] COLUNAS_CLIENTES = {CODIGO_CLIENTE, NOME_CLIENTE, LATITUDE_CLIENTE, LONGITUDE_CLIENTE, SALDO_CLIENTE, LIMITE_CREDITO_CLIENTE};
     // Constante para o campo saldo



    //CONSTANTES PRODUTOS
    private static final String TABELA_PRODUTOS = "produtos";
    private static final String CODIGO_PRODUTO = "codigo_produto";
    private static final String DESCRICAO_PRODUTO = "descricao_produto";

    private static final String[] COLUNAS_PRODUTOS = {CODIGO_PRODUTO, DESCRICAO_PRODUTO};

    @SuppressLint("SdCardPath")
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSAO_BD);
        this.context = context;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            //this.DB_PATH = context.getDatabasePath(DB_NAME).getPath() + File.separator;
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";

        } else {
            //String DB_PATH = Environment.getDataDirectory() + "/data/my.trial.app/databases/";
            //myPath = DB_PATH + dbName;
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        }*/
        this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        Log.e(TAG, " DatabaseHelper - " + DB_PATH);
    }

    void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.i(TAG, "Error copying database: " + e.getMessage());
                throw new Error("Error copying database");
            }
        }
    }

    public boolean checkDataBase() {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File arquivo = new File(path + "/siacmobileDB.db"); //.db pasta);
        FileInputStream myInput = new FileInputStream(arquivo);

        //InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            try {
                db.execSQL("ALTER TABLE pos ADD COLUMN bloqueio_edicao_preco INTEGER DEFAULT 0;");
                Log.d("DatabaseUpgrade", "Coluna bloqueio_edicao_preco adicionada à tabela pos.");
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();

            }
    }

    //
    public void addCliente(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NOME_CLIENTE, clientes.getNome_cliente());
        db.insert(TABELA_CLIENTES, null, values);
        db.close();
    }

    //CONSULTAR CLIENTE
    public Clientes getCliente(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABELA_CLIENTES, // TABELA
                COLUNAS_CLIENTES, // COLUNAS
                " codigo = ?", // COLUNAS PARA COMPARAR
                new String[]{String.valueOf(codigo)}, // PARAMETROS
                null, // GROUP BY
                null, // HAVING
                null, // ORDER BY
                null // LIMIT
        );

        //
        if (cursor == null) {
            return null;
        } else {
            cursor.moveToFirst();
            Clientes clientes = cursorToCliente(cursor);
            return clientes;
        }
    }

    //
    private Clientes cursorToCliente(Cursor cursor) {
        Clientes clientes = new Clientes(null, null, null, null, null, null, null, null);
        //clientes.setCodigo(Integer.parseInt(cursor.getString(0)));
        clientes.setCodigo_cliente(cursor.getString(0));
        clientes.setNome_cliente(cursor.getString(1));
        clientes.setLatitude_cliente(cursor.getString(2));
        clientes.setLongitude_cliente(cursor.getString(3));
        clientes.setSaldo(cursor.getString(4));
        clientes.setCpfcnpj(cursor.getString(5));
        clientes.setEndereco(cursor.getString(6));
        clientes.setApelido_cliente(cursor.getString(7));
        return clientes;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<Clientes> getAllClientes() {
        ArrayList<Clientes> listaClientes = new ArrayList<>();
        String query = "SELECT * FROM clientes ORDER BY codigo_cliente, nome_cliente";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Clientes clientes = cursorToCliente(cursor);
                listaClientes.add(clientes);
            } while (cursor.moveToNext());
        }

        return listaClientes;
    }

    //ALTERAR CLIENTE
    public int updateCliete(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOME_CLIENTE, clientes.getNome_cliente());

        int i = db.update(
                TABELA_CLIENTES,
                values,
                CODIGO_CLIENTE + " = ?",
                new String[]{String.valueOf(clientes.getCodigo_cliente())}
        );
        db.close();
        return i;
    }

    //
    public int deleteCliente(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_CLIENTES,
                CODIGO_CLIENTE + " = ?",
                new String[]{String.valueOf(clientes.getCodigo_cliente())}
        );
        db.close();
        return i;
    }

    //
    private UnidadesDomain cursorToUnidades(Cursor cursor) {
        UnidadesDomain unidades = new UnidadesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        unidades.setId_unidade(cursor.getString(0));
        unidades.setDescricao_unidade(cursor.getString(1));
        unidades.setRazao_social(cursor.getString(2));
        unidades.setCnpj(cursor.getString(3));
        unidades.setEndereco(cursor.getString(4));
        unidades.setNumero(cursor.getString(5));
        unidades.setBairro(cursor.getString(6));
        unidades.setCep(cursor.getString(7));
        unidades.setTelefone(cursor.getString(8));
        unidades.setIe(cursor.getString(9));
        unidades.setCidade(cursor.getString(10));
        unidades.setUf(cursor.getString(11));
        unidades.setCodigo_ibge(cursor.getString(12));
        unidades.setUrl_consulta(cursor.getString(13));
        return unidades;
    }


    //SOMAR O VALOR DO FINANCEIRO
    public String getIdUnidade(String unidade) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT id_unidade FROM unidades WHERE descricao_unidade = '" + unidade + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String id = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return id;
    }//SOMAR O VALOR DO FINANCEIRO

    public UnidadesDomain getUnidade() {

        UnidadesDomain unidades = new UnidadesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String query = "SELECT * FROM unidades LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        unidades = cursorToUnidades(cursor);
                    } while (cursor.moveToNext());
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.endTransaction();
            //db.close();
        }
        db.endTransaction();
        db.close();
        return unidades;
    }

    /*********************** OBTER LIMITE DE CREDITO ****************/

    public int getLimiteCreditoCliente(String codigo_cliente) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(IFNULL(" + LIMITE_CREDITO_CLIENTE + ", 0)) as total_limite FROM " + TABELA_CLIENTES + " WHERE " + CODIGO_CLIENTE + " = ?";
        String[] selectionArgs = new String[]{codigo_cliente};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int totalLimite = (int) cursor.getDouble(cursor.getColumnIndex("total_limite"));
                Log.d(TAG, "Total limite de crédito para o cliente " + codigo_cliente + ": " + totalLimite);
                cursor.close();
                return totalLimite;
            } else {
                cursor.close();
                Log.d(TAG, "Cliente " + codigo_cliente + " não encontrado.");
                return 0;
            }
        } else {
            Log.d(TAG, "Cursor nulo. Cliente " + codigo_cliente + " não encontrado.");
            return 0;
        }
    }

    /**************** METODO DE ATUALIZAR LIMITE DE CREDITO APOS TRANSAÇOES **************/

    public void updateLimiteCreditoCliente(String codigo_cliente, BigDecimal novoLimite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Coloca o novo valor no ContentValues para a coluna correspondente ao limite de crédito
        values.put(LIMITE_CREDITO_CLIENTE, novoLimite.doubleValue());

        // Atualiza a tabela onde o código do cliente for o informado
        int rowsAffected = db.update(TABELA_CLIENTES, values, CODIGO_CLIENTE + " = ?", new String[]{codigo_cliente});

        // Loga o resultado da operação
        if (rowsAffected > 0) {
            Log.d(TAG, "Limite de crédito atualizado com sucesso para o cliente " + codigo_cliente + ". Novo limite: " + novoLimite);
        } else {
            Log.d(TAG, "Falha ao atualizar o limite de crédito para o cliente " + codigo_cliente);
        }
    }


    /*************** METODO DE CONSULTA VENDA FUTURA *********/
    public boolean isVendaFuturaAtiva() {
        int vendaFutura = 0;  // Valor padrão para inativo (0 = não é venda futura)
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta SQL para buscar o parâmetro 'venda_futura' na tabela desejada
        String selectQuery = "SELECT venda_futura FROM pos";  // Altere 'pos' pelo nome da sua tabela, se necessário
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                // Obtém o valor do campo 'venda_futura'
                vendaFutura = cursor.getInt(cursor.getColumnIndexOrThrow("venda_futura"));
                Log.d("isVendaFuturaAtiva", "Valor retornado pelo banco: " + vendaFutura);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }

        // Retorna true se 'venda_futura' for igual a 1 (ativo), false se for 0 (inativo)
        return vendaFutura == 1;
    }

    /*****************NOVO METODO ENTREGA FUTURA **************/
    @SuppressLint("Range")
    public ArrayList<VendaFuturaDomain> listarDetalhesCompletosVendasFuturas() {
        ArrayList<VendaFuturaDomain> listaVendasFuturas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta com JOIN para buscar dados da venda e os produtos
        String query = "SELECT v.codigo_venda, v.codigo_cliente, c.nome_cliente, " +
                "p.produto, p.quantidade " +
                "FROM vendas_app v " +
                "JOIN clientes c ON v.codigo_cliente = c.codigo_cliente " +
                "JOIN produtos_vendas_app p ON v.codigo_venda = p.codigo_venda_app " +
                "WHERE v.entrega_futura_venda = 1";

        Cursor cursor = db.rawQuery(query, null);

        // Mapa temporário para rastrear as vendas únicas pelo código de venda
        HashMap<Integer, VendaFuturaDomain> vendasMap = new HashMap<>();

        if (cursor.moveToFirst()) {
            do {
                // Extraindo dados da venda e do cliente
                int codigoVenda = cursor.getInt(cursor.getColumnIndex("codigo_venda"));
                int codigoCliente = cursor.getInt(cursor.getColumnIndex("codigo_cliente"));
                String nomeCliente = cursor.getString(cursor.getColumnIndex("nome_cliente"));

                // Extraindo dados de produtos associados
                String produto = cursor.getString(cursor.getColumnIndex("produto"));
                int quantidade = cursor.getInt(cursor.getColumnIndex("quantidade"));
                ProdutoEmissor produtoEmissor = new ProdutoEmissor(produto, String.valueOf(quantidade)," ");

                // Verificar se a venda já está no mapa
                if (vendasMap.containsKey(codigoVenda)) {
                    // Adiciona o produto ao objeto de venda existente
                    vendasMap.get(codigoVenda).getProdutos().add(produtoEmissor);
                } else {
                    // Cria uma nova venda e adiciona ao mapa e à lista de vendas
                    ArrayList<ProdutoEmissor> produtos = new ArrayList<>();
                    produtos.add(produtoEmissor);
                    VendaFuturaDomain novaVenda = new VendaFuturaDomain(codigoVenda, codigoCliente, nomeCliente, produtos);
                    vendasMap.put(codigoVenda, novaVenda);
                    listaVendasFuturas.add(novaVenda);
                }

            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "Nenhuma venda futura encontrada.");
        }
        cursor.close();
        return listaVendasFuturas;
    }

   /* @SuppressLint("Range")
    public ArrayList<String> listarCodigosEntregaFutura() {
        ArrayList<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT codigo_venda, entrega_futura_venda FROM vendas_app", null);

        if (cursor.moveToFirst()) {
            do {
                int codigoVenda = cursor.getInt(cursor.getColumnIndex("codigo_venda"));
                int entregaFutura = cursor.getInt(cursor.getColumnIndex("entrega_futura_venda"));
                lista.add("Código Venda: " + codigoVenda + ", Entrega Futura: " + entregaFutura);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }*/


    //########## PRODUTOS ############

    //
    private Produtos cursorToProdutos(Cursor cursor) {
        Produtos produtos = new Produtos(null, null);
        produtos.setCodigo_produto(cursor.getString(0));
        produtos.setDescricao_produto(cursor.getString(1));
        return produtos;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<Produtos> getAllProdutos() {
        ArrayList<Produtos> listaProdutos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PRODUTOS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Produtos prods = cursorToProdutos(cursor);
                listaProdutos.add(prods);

                /*
                String codigo_produto = cursor.getString(0);
                String descricao_produto = cursor.getString(1);
                Produtos prod = new Produtos(codigo_produto, descricao_produto);
                listaProdutos.add(prod);
                */
            } while (cursor.moveToNext());
        }

        return listaProdutos;
    }

    public ArrayList<String> getProdutos() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "Select * From " + TABELA_PRODUTOS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String codigo_produto = cursor.getString(cursor.getColumnIndexOrThrow("codigo_produto"));
                    String descricao_produto = cursor.getString(cursor.getColumnIndexOrThrow("descricao_produto"));
                    //list.add(codigo_produto + " " + descricao_produto);
                    list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return list;
    }

    //############### VENDAS ###############

    //CONSTANTES VENDAS
    private static final String TABELA_VENDAS = "vendas_app";
    private static final String CODIGO_VENDA = "codigo_venda";
    private static final String CODIGO_CLIENTE_VENDA = "codigo_cliente";
    private static final String UNIDADE_VENDA = "unidade_venda";
    private static final String PRODUTO_VENDA = "produto_venda";
    private static final String DATA_MOVIMENTO = "data_movimento";
    private static final String QUANTIDADE_VENDA = "quantidade_venda";
    private static final String PRECO_UNITARIO = "preco_unitario";
    private static final String VALOR_TOTAL = "valor_total";
    private static final String VENDEDOR_VENDA = "vendedor_venda";
    private static final String STATUS_AUTORIZACAO_VENDA = "status_autorizacao_venda";
    private static final String ENTREGA_FUTURA_VENDA = "entrega_futura_venda";
    private static final String ENTREGA_FUTURA_REALIZADA = "entrega_futura_realizada";
    private static final String USUARIO_ATUAL = "usuario_atual";
    private static final String DATA_CADASTRO = "data_cadastro";
    private static final String CODIGO_VENDA_APP = "codigo_venda_app";
    private static final String VENDA_FINALIZADA_APP = "venda_finalizada_app";
    private static final String CHAVE_IMPORTACAO_APP = "chave_importacao";

    private static final String[] COLUNAS_VENDAS = {
            CODIGO_VENDA,
            CODIGO_CLIENTE_VENDA,
            UNIDADE_VENDA,
            PRODUTO_VENDA,
            DATA_MOVIMENTO,
            QUANTIDADE_VENDA,
            PRECO_UNITARIO,
            VALOR_TOTAL,
            VENDEDOR_VENDA,
            STATUS_AUTORIZACAO_VENDA,
            ENTREGA_FUTURA_VENDA,
            ENTREGA_FUTURA_REALIZADA,
            USUARIO_ATUAL,
            DATA_CADASTRO,
            CODIGO_VENDA_APP,
            VENDA_FINALIZADA_APP,
            CHAVE_IMPORTACAO_APP
    };

    //
    private VendasDomain cursorToVendas(Cursor cursor) {
        VendasDomain vendas = new VendasDomain(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null, null, null);
        vendas.setCodigo_venda(cursor.getString(0));
        vendas.setCodigo_cliente(cursor.getString(1));
        vendas.setUnidade_venda(cursor.getString(2));
        vendas.setProduto_venda(cursor.getString(3));
        vendas.setData_movimento(cursor.getString(4));
        vendas.setQuantidade_venda(cursor.getString(5));
        vendas.setPreco_unitario(cursor.getString(6));
        vendas.setValor_total(cursor.getString(7));
        vendas.setVendedor_venda(cursor.getString(8));
        vendas.setStatus_autorizacao_venda(cursor.getString(9));
        vendas.setEntrega_futura_venda(cursor.getString(10));
        vendas.setEntrega_futura_realizada(cursor.getString(11));
        vendas.setUsuario_atual(cursor.getString(12));
        vendas.setData_cadastro(cursor.getString(13));
        vendas.setCodigo_venda_app(cursor.getString(14));
        vendas.setVenda_finalizada_app(cursor.getString(15));
        return vendas;
    }

    //
    private VendasPedidosDomain cursorToVendasPedidos(Cursor cursor) {
        VendasPedidosDomain vendas = new VendasPedidosDomain(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null, null, null, null, null);
        vendas.setCodigo_venda(cursor.getString(0));
        vendas.setCodigo_cliente(cursor.getString(1));
        vendas.setUnidade_venda(cursor.getString(2));
        vendas.setProduto_venda(cursor.getString(3));
        vendas.setData_movimento(cursor.getString(4));
        vendas.setQuantidade_venda(cursor.getString(5));
        vendas.setPreco_unitario(cursor.getString(6));
        vendas.setValor_total(cursor.getString(7));
        vendas.setVendedor_venda(cursor.getString(8));
        vendas.setStatus_autorizacao_venda(cursor.getString(9));
        vendas.setEntrega_futura_venda(cursor.getString(10));
        vendas.setEntrega_futura_realizada(cursor.getString(11));
        vendas.setUsuario_atual(cursor.getString(12));
        vendas.setData_cadastro(cursor.getString(13));
        vendas.setCodigo_venda_app(cursor.getString(14));
        vendas.setVenda_finalizada_app(cursor.getString(15));
        // ASSUME COMO FORMAS DE PAGAMENTO PARA LISTAGEM
        vendas.setFormas_pagamento(cursor.getString(16));
        return vendas;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<VendasDomain> getAllVendas() {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        //String query = "SELECT * FROM " + TABELA_VENDAS;
        String query = "SELECT ven.codigo_venda,ven.codigo_cliente,ven.unidade_venda,ven.produto_venda,ven.data_movimento,ven.quantidade_venda," +
                "ven.preco_unitario,ven.valor_total,ven.vendedor_venda,ven.status_autorizacao_venda,ven.entrega_futura_venda,ven.entrega_futura_realizada," +
                "ven.usuario_atual,ven.data_cadastro,ven.codigo_venda_app,ven.venda_finalizada_app,ven.chave_importacao  " +
                "FROM " + TABELA_VENDAS + " ven " +
                "INNER JOIN " + TABELA_FINANCEIRO + " fin ON fin.id_financeiro_app = ven.codigo_venda_app " +
                "WHERE ven.venda_finalizada_app = '1'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<FinanceiroReceberClientes> getAllRecebidos() {
        ArrayList<FinanceiroReceberClientes> listaVendas = new ArrayList<>();

        String query = "SELECT * FROM recebidos";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberClientes vendas = cursorToContasReceberCliente(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<ValesDomain> getAllVales() {
        ArrayList<ValesDomain> listaVales = new ArrayList<>();

        String query = "SELECT * FROM vale v WHERE v.situacao_vale = 'UTILIZADO'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ValesDomain vendas = cursorToValesDomain(cursor);
                listaVales.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVales;
    }

    //LISTAR TODOS OS ITENS DA VENDA
    public ArrayList<VendasDomain> getVendasCliente(int codigo_venda_app) {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_VENDAS + " WHERE codigo_venda_app = '" + codigo_venda_app + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaVendas;
    }

    /************* LISTAR PRODUTOS NOVA TABELA ****************/

    @SuppressLint("Range")
    public ArrayList<ProdutoEmissor> getProdutosVenda(String codigoVendaApp) {
        ArrayList<ProdutoEmissor> listaProdutos = new ArrayList<>();

        String query = "SELECT produto, quantidade, preco_unitario FROM produtos_vendas_app WHERE codigo_venda_app = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{codigoVendaApp});

        if (cursor.moveToFirst()) {
            do {
                // Certifique-se de que cada campo corresponde ao tipo esperado em ProdutoEmissor
                String produtoNome = cursor.getString(cursor.getColumnIndexOrThrow("produto"));
                String quantidade = cursor.getString(cursor.getColumnIndexOrThrow("quantidade"));
                String valorUnitario = cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario"));

                // Cria uma instância de ProdutoEmissor com os dados do cursor
                ProdutoEmissor produto = new ProdutoEmissor(produtoNome, quantidade, valorUnitario);
                listaProdutos.add(produto);

                Log.d("DatabaseHelper", produto.toString());
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "Nenhum produto encontrado para o código de venda: " + codigoVendaApp);
        }

        cursor.close();
        return listaProdutos;
    }

    /************* METODO PRA LISTA DE IMPRESSAO ********************/
    public ArrayList<VendasPedidosComProdutosDomain> getRelatorioVendasComProdutos() {
        Log.d("DatabaseHelper", "Executando getRelatorioVendasComProdutos()");

        ArrayList<VendasPedidosComProdutosDomain> listaVendas = new ArrayList<>();

        String query = "SELECT codigo_venda, nome_cliente AS codigo_cliente, unidade_venda, produto_venda, data_movimento, " +
                "quantidade_venda, preco_unitario, (preco_unitario * quantidade_venda) valor_total, " +
                "vendedor_venda, status_autorizacao_venda, entrega_futura_venda, " +
                "entrega_futura_realizada, usuario_atual, data_cadastro, codigo_venda_app, " +
                "venda_finalizada_app chave_importacao, " +
                "(" +
                "SELECT GROUP_CONCAT(fin.fpagamento_financeiro || ':  ' || REPLACE('R$ ' || printf('%.2f', fin.valor_financeiro),'.',','), '\n') " +
                "FROM financeiro fin " +
                "WHERE fin.id_financeiro_app = codigo_venda_app " +
                ") formas_pagamento " +
                "FROM " + TABELA_VENDAS + " " +
                "INNER JOIN clientes ON clientes.codigo_cliente = vendas_app.codigo_cliente " +
                "WHERE venda_finalizada_app = '1'" +
                "ORDER BY produto_venda";

        Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                // Cria um objeto VendasPedidosComProdutosDomain com os dados do cursor
                VendasPedidosComProdutosDomain venda = cursorToVendasComProdutos(cursor);

                // Log dos dados principais da venda
                Log.d("LOG_VENDA_IMPRESSAO", "Código: " + venda.getCodigo_venda() + ", Cliente: " + venda.getCodigo_cliente() +
                        ", Produto: " + venda.getProduto_venda() + ", Valor Total: " + venda.getValor_total());

                // Obtém o código de venda para buscar os produtos associados
                String codigoVendaApp = venda.getCodigo_venda_app();
                if (codigoVendaApp != null && !codigoVendaApp.isEmpty()) {
                    // Busca produtos associados a esse código de venda
                    ArrayList<ProdutoEmissor> produtos = getProdutosVenda(codigoVendaApp);
                    venda.setListaProdutos(produtos); // Configura a lista de produtos na venda atual

                    // Log dos produtos associados
                    for (ProdutoEmissor produto : produtos) {
                        Log.d("Produto_IMPRESSAO", "Nome: " + produto.getNome() + ", Quantidade: " + produto.getQuantidade() +
                                ", Valor Unitário: " + produto.getValorUnitario());
                    }
                }

                listaVendas.add(venda);
            } while (cursor.moveToNext());
        }

        cursor.close();
        myDataBase.close();
        return listaVendas;
    }

    /************** QUANTIDADE TOTAL DE ITENS DA TABELA PRODUTOS VENDAS APP  ************************/

    @SuppressLint("Range")
    public int getTotalQuantidadeProdutos() {
        int totalQuantidade = 0;
        String query = "SELECT SUM(quantidade) as TotalQuantidade FROM produtos_vendas_app";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst() && !cursor.isNull(cursor.getColumnIndex("TotalQuantidade"))) {
            totalQuantidade = cursor.getInt(cursor.getColumnIndex("TotalQuantidade"));
        }

        Log.d("DatabaseHelper", "Total de quantidade de todos os produtos na tabela: " + totalQuantidade);
        cursor.close();
        db.close();
        return totalQuantidade;
    }



    // Método auxiliar para converter o cursor em VendasPedidosComProdutosDomain


    @SuppressLint("Range")
    private VendasPedidosComProdutosDomain cursorToVendasComProdutos(Cursor cursor) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        // Converte e formata os valores numéricos antes de passá-los para o construtor
        String quantidadeVenda = decimalFormat.format(cursor.getDouble(cursor.getColumnIndex("quantidade_venda")));
        String precoUnitario = decimalFormat.format(cursor.getDouble(cursor.getColumnIndex("preco_unitario")));
        String valorTotal = decimalFormat.format(cursor.getDouble(cursor.getColumnIndex("valor_total")));

        return new VendasPedidosComProdutosDomain(
                cursor.getString(cursor.getColumnIndex("codigo_venda")),
                cursor.getString(cursor.getColumnIndex("codigo_cliente")),
                cursor.getString(cursor.getColumnIndex("unidade_venda")),
                cursor.getString(cursor.getColumnIndex("produto_venda")),
                cursor.getString(cursor.getColumnIndex("data_movimento")),
                quantidadeVenda, // Formato como String para exibição, ou Double, se desejar para cálculos
                precoUnitario,
                valorTotal,
                cursor.getString(cursor.getColumnIndex("vendedor_venda")),
                cursor.getString(cursor.getColumnIndex("status_autorizacao_venda")),
                cursor.getString(cursor.getColumnIndex("entrega_futura_venda")),
                cursor.getString(cursor.getColumnIndex("entrega_futura_realizada")),
                cursor.getString(cursor.getColumnIndex("usuario_atual")),
                cursor.getString(cursor.getColumnIndex("data_cadastro")),
                cursor.getString(cursor.getColumnIndex("codigo_venda_app")),
                cursor.getString(cursor.getColumnIndex("chave_importacao")), // alias para venda_finalizada_app
                cursor.getString(cursor.getColumnIndex("formas_pagamento"))
        );
    }


    //LISTAR TODOS OS ITENS DA VENDA
    public String[] getUltimaVendasCliente() {

        String query = "SELECT " +
                "ven." + CODIGO_VENDA + ", " +
                "ven." + CODIGO_VENDA_APP + ", " +
                "cli." + CODIGO_CLIENTE + ", " +
                "cli." + NOME_CLIENTE +
                " FROM " + TABELA_VENDAS + " ven" +
                " INNER JOIN " + TABELA_CLIENTES + " cli ON cli." + CODIGO_CLIENTE + " = ven." + CODIGO_CLIENTE_VENDA +
                " WHERE ven." + VENDA_FINALIZADA_APP + " = 1" +
                " ORDER BY " + "ven." + CODIGO_VENDA_APP + " DESC" +
                " LIMIT 1";

        Log.i("SQL", "getUltimaVendasCliente - " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        cursor.moveToFirst();
        String[] id = {};
        try {
            id = new String[]{
                    cursor.getString(cursor.getColumnIndexOrThrow(CODIGO_VENDA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CODIGO_VENDA_APP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CODIGO_CLIENTE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(NOME_CLIENTE))
            };
        } catch (Exception e) {

        }
        return id;
    }
    /************** METODO DE INSERÇAO PRODUTO_VENDA_APP ***********************/

    public long addProdutoVenda(String produto, int quantidade, double precoUnitario, String codigoVendaApp,int entregaFutura) {
        // Cria um objeto ContentValues para empacotar os valores a serem inseridos
        ContentValues values = new ContentValues();
        values.put("produto", produto);
        values.put("quantidade", quantidade);
        values.put("preco_unitario", precoUnitario);
        values.put("codigo_venda_app", codigoVendaApp);
        values.put("entrega_futura" , entregaFutura);

        // Insere os dados na tabela e retorna o ID da nova linha inserida ou -1 em caso de erro
        return this.getWritableDatabase().insert("produtos_vendas_app", null, values);
    }


    /************ EXCLUIR PRODUTO NOVA TABELA ********************************/

    public int deleteProdutoVenda(String produto, String codigoVendaApp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();  // Inicia uma transação para garantir a integridade dos dados

        try {
            // Log para verificar qual produto e código de venda estão sendo excluídos
            Log.d("EXCLUIR PRODUTO", "Excluindo produto: " + produto + ", Código de venda: " + codigoVendaApp);

            // Obtenha o valor do produto antes de excluí-lo
            double valorProduto = getValorProduto(produto, codigoVendaApp);

            // Define a cláusula WHERE e os argumentos para identificar o produto específico em produtos_vendas_app
            String whereClauseProdutos = "produto = ? AND codigo_venda_app = ?";
            String[] whereArgsProdutos = {produto, codigoVendaApp};

            // Executa a exclusão na tabela produtos_vendas_app
            int linhasAfetadasProdutos = db.delete("produtos_vendas_app", whereClauseProdutos, whereArgsProdutos);

            int linhasAfetadasVendas = 0;
            if (linhasAfetadasProdutos > 0) {
                Log.d("EXCLUIR PRODUTO", "Produto excluído de produtos_vendas_app: " + produto);

                // Executa a exclusão na tabela vendas_app
                String whereClauseVendas = "produto_venda = ? AND codigo_venda_app = ?";
                String[] whereArgsVendas = {produto, codigoVendaApp};

                linhasAfetadasVendas = db.delete("vendas_app", whereClauseVendas, whereArgsVendas);
                Log.d("EXCLUIR VENDA", "Produto excluído de vendas_app: " + produto);

                // Subtrair o valor do produto do total da venda, se necessário
                if (linhasAfetadasVendas > 0) {
                    Log.d("ATUALIZAR VALOR", "Subtraindo do total da venda o valor: R$ " + valorProduto);
                    //atualizarValorTotalVenda(codigoVendaApp, valorProduto);
                }
            }

            db.setTransactionSuccessful();  // Marca a transação como bem sucedida
            return linhasAfetadasProdutos + linhasAfetadasVendas;  // Retorna o total de linhas afetadas

        } catch (Exception e) {
            Log.e("DB ERROR", "Erro ao excluir produto: " + e.getMessage());
            return 0;  // Em caso de erro retorna 0
        } finally {
            db.endTransaction();  // Finaliza a transação
            db.close();  // Fecha a conexão com o banco de dados
        }
    }


    // Método para obter o valor do produto a ser excluído
    @SuppressLint("Range")
    private double getValorProduto(String produto, String codigoVendaApp) {
        double valorProduto = 0.0;
        String query = "SELECT quantidade,  preco_unitario FROM produtos_vendas_app WHERE produto = ? AND codigo_venda_app = ?";
        String[] whereArgs = {produto, codigoVendaApp};

        Cursor cursor = this.getReadableDatabase().rawQuery(query, whereArgs);
        if (cursor.moveToFirst()) {
             int quantidade = cursor.getInt(cursor.getColumnIndex("quantidade"));
            double valorUnitario = cursor.getDouble(cursor.getColumnIndex("preco_unitario"));
            valorProduto = quantidade * valorUnitario;
        }
        cursor.close();

        return valorProduto;
    }

/**************** OBTER TODOS OS DADOS DE UMA VENDA  PRA EDIÇAO ***************/

public DadosCompletosDomain obterDadosCompletosVenda(int codigoVendaApp) {
    DadosCompletosDomain dadosCompletos = new DadosCompletosDomain();

    // 1. Obter dados da venda principal
    String queryVenda = "SELECT " +
            "ven." + CODIGO_VENDA + ", " +
            "ven." + CODIGO_VENDA_APP + ", " +
            "cli." + CODIGO_CLIENTE + ", " +
            "cli." + NOME_CLIENTE + ", " +
            "ven." + UNIDADE_VENDA + ", " +
            "ven." + PRODUTO_VENDA + ", " +
            "ven." + DATA_MOVIMENTO + ", " +
            "ven." + QUANTIDADE_VENDA + ", " +
            "ven." + PRECO_UNITARIO + ", " +
            "ven." + VALOR_TOTAL + ", " +
            "ven." + VENDEDOR_VENDA + ", " +
            "ven." + STATUS_AUTORIZACAO_VENDA + ", " +
            "ven." + ENTREGA_FUTURA_VENDA + ", " +
            "ven." + ENTREGA_FUTURA_REALIZADA + ", " +
            "ven." + USUARIO_ATUAL + ", " +
            "ven." + DATA_CADASTRO + ", " +
            "ven." + VENDA_FINALIZADA_APP + ", " +
            "ven." + CHAVE_IMPORTACAO_APP +
            " FROM " + TABELA_VENDAS + " ven" +
            " INNER JOIN " + TABELA_CLIENTES + " cli ON cli." + CODIGO_CLIENTE + " = ven." + CODIGO_CLIENTE_VENDA +
            " WHERE ven." + CODIGO_VENDA_APP + " = ?";

    myDataBase = this.getReadableDatabase();
    Cursor cursorVenda = myDataBase.rawQuery(queryVenda, new String[]{String.valueOf(codigoVendaApp)});

    if (cursorVenda.moveToFirst()) {
        dadosCompletos.setCodigoVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(CODIGO_VENDA)));
        dadosCompletos.setCodigoVendaApp(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(CODIGO_VENDA_APP)));
        dadosCompletos.setCodigoCliente(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(CODIGO_CLIENTE)));
        dadosCompletos.setNomeCliente(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(NOME_CLIENTE)));
        dadosCompletos.setUnidadeVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(UNIDADE_VENDA)));
        dadosCompletos.setProdutoVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(PRODUTO_VENDA)));
        dadosCompletos.setDataMovimento(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(DATA_MOVIMENTO)));
        dadosCompletos.setQuantidadeVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(QUANTIDADE_VENDA)));
        dadosCompletos.setPrecoUnitario(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(PRECO_UNITARIO)));
        dadosCompletos.setValorTotal(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(VALOR_TOTAL)));
        dadosCompletos.setVendedorVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(VENDEDOR_VENDA)));
        dadosCompletos.setStatusAutorizacaoVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(STATUS_AUTORIZACAO_VENDA)));
        dadosCompletos.setEntregaFuturaVenda(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(ENTREGA_FUTURA_VENDA)));
        dadosCompletos.setEntregaFuturaRealizada(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(ENTREGA_FUTURA_REALIZADA)));
        dadosCompletos.setUsuarioAtual(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(USUARIO_ATUAL)));
        dadosCompletos.setDataCadastro(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(DATA_CADASTRO)));
        dadosCompletos.setVendaFinalizadaApp(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(VENDA_FINALIZADA_APP)));
        dadosCompletos.setChaveImportacao(cursorVenda.getString(cursorVenda.getColumnIndexOrThrow(CHAVE_IMPORTACAO_APP)));
    }
    cursorVenda.close();

    // 2. Obter lista de produtos da venda
    ArrayList<ProdutoEmissor> produtosVenda = getProdutosVenda(String.valueOf(codigoVendaApp));
    dadosCompletos.setProdutosVenda(produtosVenda);

    // 3. Calcular total de itens na venda
    int totalItens = 0;
    for (ProdutoEmissor produto : produtosVenda) {
        try {
            totalItens += Integer.parseInt(produto.getQuantidade());
        } catch (NumberFormatException e) {
            Log.e("DadosCompletosVenda", "Erro ao converter quantidade para inteiro: " + produto.getQuantidade(), e);
        }
    }
    dadosCompletos.setTotalItens(totalItens);

    Log.d("DadosCompletosVenda", "Dados completos da venda recuperados para o código: " + codigoVendaApp);

    return dadosCompletos;
}


    /***************** METODO DE CONSULTA PRODUTO_VENDAS_APP ***********/

    @SuppressLint("Range")
    public double listarProdutosVendasApp(String codigoVendaApp) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id, produto, quantidade, preco_unitario FROM produtos_vendas_app WHERE codigo_venda_app = ?";
        Cursor cursor = db.rawQuery(query, new String[]{codigoVendaApp});

        double totalVenda = 0.0;

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String produto = cursor.getString(cursor.getColumnIndex("produto"));
                int quantidade = cursor.getInt(cursor.getColumnIndex("quantidade"));
                 double precoUnitario = cursor.getDouble(cursor.getColumnIndex("preco_unitario"));

                double totalProduto = quantidade * precoUnitario;
                totalVenda += totalProduto;

                Log.d("DatabaseHelper", "Produto: " + produto + ", Quantidade: " + quantidade + ", Preço Unitário: " + precoUnitario + ", Total Produto: " + totalProduto);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "Nenhum produto encontrado para o código de venda: " + codigoVendaApp);
        }
        cursor.close();
        return totalVenda;
    }

    /************** OBTER TOTAL DE VENDAS REGISTRADAS ****************/

    public BigDecimal calcularTotalVendas() {
        SQLiteDatabase db = this.getReadableDatabase();
        BigDecimal totalVendas = BigDecimal.ZERO;

        String query = "SELECT quantidade, preco_unitario FROM produtos_vendas_app";
        Cursor cursor = db.rawQuery(query, null);

        try {
            while (cursor.moveToNext()) {
                int quantidade = cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"));
                BigDecimal precoUnitario = BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("preco_unitario")));
                BigDecimal totalItem = precoUnitario.multiply(BigDecimal.valueOf(quantidade));

                // Logs para verificação detalhada
                Log.d("CalculoItemVenda", "Produto com quantidade: " + quantidade + ", Preço unitário: " + precoUnitario + ", Total item: " + totalItem);

                // Acumula o total
                totalVendas = totalVendas.add(totalItem);
            }

            // Log do total acumulado após a iteração
            Log.d("CalculoTotalVendas", "Total acumulado das vendas: " + totalVendas);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        db.close();
        return totalVendas;
    }
    /********** DELET GERAL VENDAS ******************/

    public int deleteProdutosPorVenda(String codigoVendaApp) {
        // Define a cláusula WHERE para remover todos os produtos com o mesmo código de venda
        String whereClause = "codigo_venda_app = ?";
        String[] whereArgs = {codigoVendaApp};

        // Executa a exclusão e retorna o número de linhas afetadas
        SQLiteDatabase db = this.getWritableDatabase();
        int linhasAfetadas = db.delete("produtos_vendas_app", whereClause, whereArgs);
        db.close();

        // Log para verificar se a exclusão ocorreu corretamente
        if (linhasAfetadas > 0) {
            Log.d("DeleteProdutosPorVenda", "Excluídos " + linhasAfetadas + " produtos para a venda com código: " + codigoVendaApp);
        } else {
            Log.d("DeleteProdutosPorVenda", "Nenhum produto encontrado para exclusão com o código de venda: " + codigoVendaApp);
        }

        return linhasAfetadas;
    }




    //LISTAR TODOS OS ITENS DA VENDA
    public String[] getClienteUltimaVendas(String id_cliente) {

        String query = "SELECT * FROM clientes WHERE codigo_cliente = " + id_cliente;

        Log.i("SQL", "getClienteUltimaVendas - " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        cursor.moveToFirst();
        String[] id = {};
        try {
            id = new String[]{
                    cursor.getString(cursor.getColumnIndexOrThrow("saldo")),
                    cursor.getString(cursor.getColumnIndexOrThrow("cpfcnpj")),
                    cursor.getString(cursor.getColumnIndexOrThrow("endereco"))
            };
        } catch (Exception e) {

        }
        return id;
    }

    //
    public void addVenda(VendasDomain vendas) {
        //SQLiteDatabase db = this.getWritableDatabase();
        myDataBase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_VENDA, vendas.getCodigo_venda());
        values.put(CODIGO_CLIENTE_VENDA, vendas.getCodigo_cliente());
        values.put(UNIDADE_VENDA, vendas.getUnidade_venda());
        values.put(PRODUTO_VENDA, vendas.getProduto_venda());
        values.put(DATA_MOVIMENTO, vendas.getData_movimento());
        values.put(QUANTIDADE_VENDA, vendas.getQuantidade_venda());
        values.put(PRECO_UNITARIO, vendas.getPreco_unitario());
        values.put(VALOR_TOTAL, vendas.getValor_total());
        values.put(VENDEDOR_VENDA, vendas.getVendedor_venda());
        values.put(STATUS_AUTORIZACAO_VENDA, vendas.getStatus_autorizacao_venda());
        values.put(ENTREGA_FUTURA_VENDA, vendas.getEntrega_futura_venda());
        values.put(ENTREGA_FUTURA_REALIZADA, vendas.getEntrega_futura_realizada());
        values.put(USUARIO_ATUAL, vendas.getUsuario_atual());
        values.put(DATA_CADASTRO, vendas.getData_cadastro());
        values.put(CODIGO_VENDA_APP, vendas.getCodigo_venda_app());
        values.put(VENDA_FINALIZADA_APP, vendas.getVenda_finalizada_app());
        values.put(CHAVE_IMPORTACAO_APP, vendas.getChave_importacao());
        myDataBase.insert(TABELA_VENDAS, null, values);
        myDataBase.close();
    }

    //LISTAR TODOS OS CLIENTES
    public String getValorTotalVenda(String codigo_venda_app) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_total) FROM " + TABELA_VENDAS + " WHERE " + CODIGO_VENDA_APP + " = '" + codigo_venda_app + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return total;
    }

    //ALTERAR CHAVE VENDA
    public int updateVendaApp(String nVenda, String nChave) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHAVE_IMPORTACAO_APP, String.valueOf(nChave));

        int i = db.update(
                TABELA_VENDAS,
                values,
                CODIGO_VENDA + " = ?",
                new String[]{String.valueOf(nVenda)}
        );
        db.close();
        return i;
    }

    public ArrayList<VendasDomain> vendasNaoSinc() {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT " +
                "codigo_venda, " +
                "codigo_cliente, " +
                "unidade_venda, " +
                "produto_venda, " +
                "data_movimento, " +
                "SUM(quantidade_venda) quantidade_venda, " +
                "preco_unitario, " +
                "SUM(valor_total) valor_total, " +
                "vendedor_venda, " +
                "status_autorizacao_venda, " +
                "entrega_futura_venda, " +
                "entrega_futura_realizada, " +
                "usuario_atual, " +
                "data_cadastro, " +
                "codigo_venda_app, " +
                "venda_finalizada_app " +
                "chave_importacao " +
                "FROM " + TABELA_VENDAS + " WHERE venda_finalizada_app = '1' AND chave_importacao = '' GROUP BY " + PRODUTO_VENDA;

        Log.e("SQL = ", query);


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        myDataBase.close();
        return listaVendas;
    }

    //CONSULTAR VENDA
    public VendasDomain getVenda(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABELA_VENDAS, // TABELA
                COLUNAS_VENDAS, // COLUNAS
                " codigo_venda = ?", // COLUNAS PARA COMPARAR
                new String[]{String.valueOf(codigo)}, // PARAMETROS
                null, // GROUP BY
                null, // HAVING
                null, // ORDER BY
                null // LIMIT
        );

        //
        if (cursor == null) {
            return null;
        } else {
            cursor.moveToFirst();
            VendasDomain vendas = cursorToVendas(cursor);
            return vendas;
        }
    }

    //
    public int deleteItemVenda(VendasDomain vendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_VENDA + " = ?",
                new String[]{String.valueOf(vendasDomain.getCodigo_venda())}
        );
        db.close();
        return i;
    }

    //
    public int deleteVenda(int codigo_venda_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_VENDA_APP + " = ?",
                new String[]{String.valueOf(codigo_venda_app)}
        );
        db.close();
        return i;
    }

    /***************** METODO PARA RESTITUIR LIMITE DE CREDITO ********************/

    public void restituirLimiteCreditoCliente(String codigoCliente, BigDecimal valorRestituido) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Log para saber qual cliente estamos processando
        Log.d("RESTITUIR LIMITE", "Processando restituição para o cliente: " + codigoCliente);

        // Consultar o limite atual de crédito do cliente
        String query = "SELECT " + LIMITE_CREDITO_CLIENTE + " FROM " + TABELA_CLIENTES + " WHERE " + CODIGO_CLIENTE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{codigoCliente});

        if (cursor.moveToFirst()) {
            // Obter o limite atual de crédito
            @SuppressLint("Range") BigDecimal limiteAtual = new BigDecimal(cursor.getDouble(cursor.getColumnIndex(LIMITE_CREDITO_CLIENTE)));

            // Log para verificar o limite atual antes da restituição
            Log.d("RESTITUIR LIMITE", "Limite atual antes da restituição: " + limiteAtual.toString());

            // Adicionar o valor restituído ao limite atual
            BigDecimal novoLimite = limiteAtual.add(valorRestituido);

            // Log para verificar o valor que está sendo restituído
            Log.d("RESTITUIR LIMITE", "Valor restituído: " + valorRestituido.toString());

            // Log para verificar o novo limite de crédito após a restituição
            Log.d("RESTITUIR LIMITE", "Novo limite de crédito após restituição: " + novoLimite.toString());

            // Atualizar o limite de crédito no banco de dados
            ContentValues values = new ContentValues();
            values.put(LIMITE_CREDITO_CLIENTE, novoLimite.doubleValue());

            int rowsUpdated = db.update(TABELA_CLIENTES, values, CODIGO_CLIENTE + " = ?", new String[]{codigoCliente});

            // Log para confirmar se a atualização foi bem-sucedida
            if (rowsUpdated > 0) {
                Log.d("RESTITUIR LIMITE", "Limite de crédito atualizado com sucesso para o cliente: " + codigoCliente);
            } else {
                Log.e("RESTITUIR LIMITE", "Erro ao atualizar o limite de crédito para o cliente: " + codigoCliente);
            }
        } else {
            // Log para o caso de não encontrar o cliente no banco
            Log.e("RESTITUIR LIMITE", "Cliente não encontrado: " + codigoCliente);
        }

        cursor.close();
        db.close();
    }

    /****************** RECUPERAÇAO ENTREGA FUTURA PARA EDIÇAO ******************/
    public int getEntregaFuturaVenda(int codigoVenda) {
        SQLiteDatabase db = this.getReadableDatabase();
        int entregaFutura = 0;

        // Consulta o campo 'entrega_futura_venda' com base no 'codigo_venda'
        Cursor cursor = db.rawQuery("SELECT entrega_futura_venda FROM vendas_app WHERE codigo_venda = ?", new String[] { String.valueOf(codigoVenda) });

        // Se houver um resultado, atribui o valor ao 'entregaFutura'
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int entregaFuturaVenda = cursor.getInt(cursor.getColumnIndex("entrega_futura_venda"));
            entregaFutura = entregaFuturaVenda;
            Log.d("DatabaseHelper", "Entrega futura encontrada para codigo_venda " + codigoVenda + ": " + entregaFutura);
        } else {
            Log.d("DatabaseHelper", "Nenhuma venda encontrada com codigo_venda " + codigoVenda);
        }

        cursor.close();
        db.close();

        return entregaFutura;
    }



    /**************** METODO PARA CONSULTA PARAMETRO BLOQUEIO EDIÇAO DE PREÇOS ***************/

    public boolean BloqueioEdicaoPreco() {
        int bloqueio = 0;  // Valor padrão para inativo
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT bloqueio_edicao_preco FROM pos";
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                bloqueio = cursor.getInt(cursor.getColumnIndexOrThrow("bloqueio_edicao_preco"));
                Log.d("BloqueioEdicaoPreco", "Valor retornado pelo banco: " + bloqueio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }

        return bloqueio == 1;  // Retorna true se o bloqueio for 1 (ativo), false se for 0 (inativo)
    }


    /***************** METODO DA IDENTIFICAR PARAMETRO VENDA FUTURA ******************/

    public boolean VendaFuturaAtiva() {
        int vendaFutura = 0;  // Valor padrão para inativo (0 = não é venda futura)
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta SQL para buscar o parâmetro 'venda_futura' na tabela desejada
        String selectQuery = "SELECT bloqueio_venda_futura FROM pos";
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                // Obtém o valor do campo 'venda_futura'
                vendaFutura = cursor.getInt(cursor.getColumnIndexOrThrow("bloqueio_venda_futura"));
                Log.d("Venda Futura Ativa", "Valor retornado pelo banco: " + vendaFutura);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }

        // Retorna true se 'venda_futura' for igual a 1 (ativo), false se for 0 (inativo)
        return vendaFutura == 1;
    }


    /******************** METODO PARA ATUALIZAR ENTREGA_FUTURA **************/

    public int atualizarEntregaFutura(int entregaFutura, int codigoVenda) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Preparar a atualização
        ContentValues contentValues = new ContentValues();
        contentValues.put("entrega_futura_venda", entregaFutura);

        // Logando o valor que será atualizado
        Log.d("UpdateLog", "Tentando atualizar 'entrega_futura_venda' para: " + entregaFutura + " para o codigo_venda: " + codigoVenda);

        // Atualiza o campo 'entrega_futura_venda' apenas para o registro com o código de venda específico
        int result = db.update("vendas_app", contentValues, "codigo_venda = ?", new String[] { String.valueOf(codigoVenda) });

        // Verifica e loga as linhas afetadas após a atualização
        if (result > 0) {
            Log.d("DATAHELPER SUCCESS", "Atualização bem-sucedida: " + result + " linha(s) afetada(s). EntregaFutura: " + entregaFutura);

            // Consultar a linha afetada
            Cursor cursor = db.rawQuery("SELECT codigo_venda, entrega_futura_venda FROM vendas_app WHERE codigo_venda = ?", new String[] { String.valueOf(codigoVenda) });

            Log.d("Affected Rows", "Linhas afetadas:");

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int codigoVendaAtual = cursor.getInt(cursor.getColumnIndex("codigo_venda"));
                    @SuppressLint("Range") int entregaFuturaAtual = cursor.getInt(cursor.getColumnIndex("entrega_futura_venda"));

                    Log.d("Affected Row", "codigo_venda: " + codigoVendaAtual + ", entrega_futura_venda atual: " + entregaFuturaAtual);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d("DatabaseHelper FAIL", "Falha na atualização: Nenhuma linha afetada.");
        }

        db.close();
        return result;
    }


    /*************** CONSULTAR E LISTAR APENAS O CAMPO ENTREGA_FUTURA _VENDA ***********************/

    public ArrayList<Integer> listarEntregasFuturas() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> listaEntregasFuturas = new ArrayList<>();

        // Consulta para selecionar o campo 'entrega_futura_venda' da tabela
        Cursor cursor = db.rawQuery("SELECT entrega_futura_venda FROM vendas_app", null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int entregaFutura = cursor.getInt(cursor.getColumnIndex("entrega_futura_venda"));
                listaEntregasFuturas.add(entregaFutura);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listaEntregasFuturas;
    }


    /************ ATUALIZAR DADOS DA VENDA APOS A CRIAÇÃO PRIMÁRIA ****************/

    public void atualizarValoresVenda(int idVenda, double valorUnitario, double totalVenda, int quantidade ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("preco_unitario", valorUnitario); // Valor unitário em formato double
        valores.put("valor_total", totalVenda);       // Valor total em formato double
        valores.put("quantidade_venda", quantidade);  // Quantidade sem espaços extras

        // Tentativa de atualização da venda no banco de dados
        int resultado = db.update("vendas_app", valores, "codigo_venda = ?", new String[] { String.valueOf(idVenda) });

        // Log para documentar a tentativa de atualização
        if (resultado > 0) {
            Log.d("DB Update", "Venda ID " + idVenda + " atualizada com sucesso. Preço Unitário: " + valorUnitario + ", Valor Total: " + totalVenda + ", Quantidade: " + quantidade);
        } else {
            Log.d("DB Update", "Falha ao atualizar a venda ID " + idVenda + ". Nenhuma linha afetada.");
        }
    }


    /******************** ATUALIZAR VENDA FINALIZADA *****************/
    public void marcarVendaComoFinalizada(int id_venda_app) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Define explicitamente o valor do campo venda_finalizada_app como '1'
        values.put("venda_finalizada_app", "1");

        // Atualiza o registro na tabela TABELA_VENDAS onde o ID corresponde ao codigoVendaApp
        int rowsAffected = db.update(TABELA_VENDAS, values, "codigo_venda_app = ?", new String[]{String.valueOf(id_venda_app)});

        if (rowsAffected > 0) {
            Log.d("marcarVendaFinalizada", "Venda com ID " + id_venda_app + " marcada como finalizada.");
        } else {
            Log.e("marcarVendaFinalizada", "Falha ao marcar a venda com ID " + id_venda_app);
        }

        db.close();
    }





  /*  public int atualizarValoresVenda(int idVenda, double valorUnitario, double valorTotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("preco_unitario", valorUnitario);
        valores.put("valor_total", valorTotal);

        // Atualizando a linha
        return db.update(TABELA_VENDAS, valores, "id_venda = ?", new String[] { String.valueOf(idVenda) });
    }*/
/**********************************************************************************/

    //########## RELATÓRIOS DE VENDA ############
    //LISTAR TODOS OS CLIENTES
    /*
    ", " +
                "COUNT(" + QUANTIDADE_VENDA + ") AS quantidade_venda, " +
                "SUM(" + VALOR_TOTAL + ") AS valor_total " +



                private static final String TABELA_VENDAS = "vendas_app";
    private static final String CODIGO_VENDA = "codigo_venda";
    private static final String CODIGO_CLIENTE_VENDA = "codigo_cliente";
    private static final String UNIDADE_VENDA = "unidade_venda";
    private static final String PRODUTO_VENDA = "produto_venda";
    private static final String DATA_MOVIMENTO = "data_movimento";
    private static final String QUANTIDADE_VENDA = "quantidade_venda";
    private static final String PRECO_UNITARIO = "preco_unitario";
    private static final String VALOR_TOTAL = "valor_total";
    private static final String VENDEDOR_VENDA = "vendedor_venda";
    private static final String STATUS_AUTORIZACAO_VENDA = "status_autorizacao_venda";
    private static final String ENTREGA_FUTURA_VENDA = "entrega_futura_venda";
    private static final String ENTREGA_FUTURA_REALIZADA = "entrega_futura_realizada";
    private static final String USUARIO_ATUAL = "usuario_atual";
    private static final String DATA_CADASTRO = "data_cadastro";
    private static final String CODIGO_VENDA_APP = "codigo_venda_app";
     */
    @SuppressLint("Range")
    public ArrayList<VendasDomain> getRelatorioVendas() {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT " +
                "codigo_venda, " +
                "codigo_cliente, " +
                "unidade_venda, " +
                "produto_venda, " +
                "data_movimento, " +
                "SUM(quantidade_venda) quantidade_venda, " +
                "preco_unitario, " +
                "SUM(valor_total) valor_total, " +
                "vendedor_venda, " +
                "status_autorizacao_venda, " +
                "entrega_futura_venda, " +
                "entrega_futura_realizada, " +
                "usuario_atual, " +
                "data_cadastro, " +
                "codigo_venda_app, " +
                "venda_finalizada_app, " +
                "chave_importacao " +
                "FROM " + TABELA_VENDAS + " WHERE venda_finalizada_app = '1' GROUP BY " + PRODUTO_VENDA;

        Log.e("SQL = ", query);


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);

                // Logando os dados brutos
                Log.d("DadosBrutos", "Código Venda: " + cursor.getString(cursor.getColumnIndex("codigo_venda")));
                Log.d("DadosBrutos", "Código Cliente: " + cursor.getString(cursor.getColumnIndex("codigo_cliente")));
                Log.d("DadosBrutos", "Produto Venda: " + cursor.getString(cursor.getColumnIndex("produto_venda")));
                Log.d("DadosBrutos", "Quantidade: " + cursor.getString(cursor.getColumnIndex("quantidade_venda")));
                Log.d("DadosBrutos", "Valor Total: " + cursor.getString(cursor.getColumnIndex("valor_total")));
                Log.d("DadosBrutos", "Formas de Pagamento: " + cursor.getString(cursor.getColumnIndex("venda_finalizada_app")));

                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        Log.d("getRelatorioVendas", "Número de vendas retornadas: " + cursor.getCount());

        //myDataBase.close();
        Log.d("LOG LISTA", "getRelatorioVendas: LISTA VENDAS" + listaVendas.toString());
        return listaVendas;
    }



    public ArrayList<VendasPedidosDomain> getRelatorioVendasPedidos() {
        ArrayList<VendasPedidosDomain> listaVendas = new ArrayList<>();

        //"SUM(quantidade_venda) quantidade_venda, " +
        //"SUM(valor_total) valor_total, " +
        String query = "SELECT  codigo_venda, nome_cliente AS codigo_cliente,  unidade_venda,  produto_venda,  data_movimento, " +
                "quantidade_venda,  preco_unitario,  (preco_unitario * quantidade_venda) valor_total, " +
                "vendedor_venda,  status_autorizacao_venda,  entrega_futura_venda, " +
                "entrega_futura_realizada,  usuario_atual,  data_cadastro,  codigo_venda_app, " +
                "venda_finalizada_app chave_importacao, " +
                "(" +
                "SELECT GROUP_CONCAT(fin.fpagamento_financeiro || ':  ' || [REPLACE]('R$ ' || printf('%.2f', fin.valor_financeiro),'.',','), '\n') " +
                "FROM financeiro fin " +
                "WHERE fin.id_financeiro_app = codigo_venda_app " +
                ") formas_pagamento " +
                "FROM " + TABELA_VENDAS + " " +
                "INNER JOIN clientes ON clientes.codigo_cliente = vendas_app.codigo_cliente " +
                "WHERE venda_finalizada_app = '1'" +
                "UNION\n" +
                "SELECT val.codigo_vale AS codigo_venda," +
                "       cli.nome_cliente AS codigo_cliente," +
                "       '' AS unidade_venda," +
                "       val.produto_vale AS produto_venda," +
                "       '' AS data_movimento," +
                "       '1' AS quantidade_venda," +
                "       (val.valor_vale / 100) AS preco_unitario," +
                "       (val.valor_vale / 100) AS valor_total," +
                "       '' AS vendedor_venda," +
                "       '' AS status_autorizacao_venda," +
                "       '' AS entrega_futura_venda," +
                "       '' AS entrega_futura_realizada," +
                "       '' AS usuario_atual," +
                "       '' AS data_cadastro," +
                "       '' AS codigo_venda_app," +
                "       '' AS chave_importacao," +
                "       'VALE-PRODUTO' || ':  ' || [REPLACE]('R$ ' || printf('%.2f', val.valor_vale / 100), '.', ',') AS formas_pagamento" +
                "  FROM vale val" +
                "       INNER JOIN" +
                "       clientes cli ON cli.codigo_cliente = val.codigo_cliente_vale" +
                " WHERE val.situacao_vale = 'UTILIZADO'" +
                "ORDER BY produto_venda";
        //" ORDER BY produto_venda";// GROUP BY " + PRODUTO_VENDA

        /*String query = "SELECT vapp.codigo_venda, " +
                "       cli.nome_cliente AS codigo_cliente, " +
                "       vapp.unidade_venda, " +
                "       vapp.produto_venda, " +
                "       vapp.data_movimento, " +
                "       vapp.quantidade_venda, " +
                "       vapp.preco_unitario, " +
                "       (vapp.preco_unitario * vapp.quantidade_venda) valor_total, " +
                "       vapp.vendedor_venda, " +
                "       vapp.status_autorizacao_venda, " +
                "       vapp.entrega_futura_venda, " +
                "       vapp.entrega_futura_realizada, " +
                "       vapp.usuario_atual, " +
                "       vapp.data_cadastro, " +
                "       vapp.codigo_venda_app, " +
                "       vapp.venda_finalizada_app chave_importacao, " +
                "       (fin.fpagamento_financeiro || ':  ' || [REPLACE]('R$ ' || printf('%.2f', fin.valor_financeiro), '.', ',') ) AS formas_pagamento " +
                "  FROM vendas_app vapp " +
                "       INNER JOIN " +
                "       clientes cli ON cli.codigo_cliente = vapp.codigo_cliente " +
                "       INNER JOIN " +
                "       financeiro fin ON fin.id_financeiro_app = codigo_venda_app " +
                " WHERE vapp.venda_finalizada_app = '1' " +
                " ORDER BY vapp.produto_venda;";*/

        Log.e("SQL = ", query);


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                VendasPedidosDomain vendas = cursorToVendasPedidos(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        //myDataBase.close();
        return listaVendas;
    }

    /*******************************************************************/
    @SuppressLint("Range")
    public ArrayList<ProdutoEmissor> getProdutosPorPedido(String codigoVendaApp) {
        ArrayList<ProdutoEmissor> listaProdutos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta usando os nomes corretos das colunas: 'produto', 'quantidade', 'preco_unitario'
        Cursor cursor = db.rawQuery("SELECT produto, quantidade, preco_unitario FROM produtos_vendas_app WHERE codigo_venda_app = ?", new String[]{codigoVendaApp});

        if (cursor.moveToFirst()) {
            do {
                // Obtendo o valor da coluna 'produto' e armazenando na variável para o nome do produto
                String produto = cursor.getString(cursor.getColumnIndex("produto"));
                String quantidade = cursor.getString(cursor.getColumnIndex("quantidade"));
                String valorUnitario = cursor.getString(cursor.getColumnIndex("preco_unitario"));

                // Criando o objeto ProdutoEmissor com os dados da linha atual
                ProdutoEmissor Produto = new ProdutoEmissor(produto, quantidade, valorUnitario);
                listaProdutos.add(Produto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaProdutos;
    }




    public String getFormPagRelatorioVendasPedidos() {
        //(sum(fin.valor_financeiro) * 100)
        StringBuilder formsP = new StringBuilder();
        String query = "SELECT fin.fpagamento_financeiro, " +
                "       sum(fin.valor_financeiro) valor_financeiro " +
                "FROM financeiro fin " +
                "       INNER JOIN " +
                "       vendas_app vapp ON vapp.codigo_venda_app = fin.id_financeiro_app " +
                "WHERE vapp.venda_finalizada_app = '1' " +
                "GROUP BY fpagamento_financeiro " +
                "UNION " +
                "SELECT 'VALE-PRODUTO' AS fpagamento_financeiro, sum(val.valor_vale) AS valor_financeiro " +
                "FROM vale val " +
                "WHERE val.situacao_vale = 'UTILIZADO' " +
                "GROUP BY fpagamento_financeiro";

        Log.e("SQLvale", query);


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                /*VendasPedidosDomain vendas = cursorToVendasPedidos(cursor);
                listaVendas.add(vendas);*/

                formsP.append(cursor.getString(0));//cursor.getColumnIndexOrThrow("fpagamento_financeiro")
                formsP.append(": ");
                formsP.append(aux.maskMoney(new BigDecimal(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro")))));//aux.converterValores
                formsP.append("\n");
            } while (cursor.moveToNext());
        }

        return formsP.toString();
    }
    /********************************************************************/
    public ArrayList<VendasPedidosDomain> getRelatorioUnificado() {
        ArrayList<VendasPedidosDomain> listaVendas = new ArrayList<>();

        // Consulta para obter todas as vendas que possuem correspondência na tabela produtos_vendas_app
        String query = "SELECT " +
                "vendas.codigo_venda, " +
                "vendas.codigo_cliente, " +
                "produtos.produto AS produto_venda, " +
                "vendas.data_movimento, " +
                "IFNULL(SUM(produtos.quantidade), 0) AS quantidade_venda, " +
                "IFNULL(produtos.preco_unitario, 0) AS preco_unitario, " +
                "IFNULL(SUM(produtos.preco_unitario * produtos.quantidade), 0) AS valor_total, " +
                "vendas.vendedor_venda, " +
                "vendas.status_autorizacao_venda, " +
                "vendas.entrega_futura_venda, " +
                "vendas.entrega_futura_realizada, " +
                "vendas.usuario_atual, " +
                "vendas.data_cadastro, " +
                "vendas.codigo_venda_app, " +
                "vendas.venda_finalizada_app, " +
                "vendas.chave_importacao " +
                "FROM " + TABELA_VENDAS + " AS vendas " +
                "LEFT JOIN produtos_vendas_app AS produtos ON vendas.codigo_venda_app = produtos.codigo_venda_app " +
                "GROUP BY vendas.codigo_venda_app " +

                "UNION ALL " +

                // Consulta para obter todas as vendas que estão apenas em produtos_vendas_app, sem correspondência em TABELA_VENDAS
                "SELECT " +
                "NULL AS codigo_venda, " +
                "NULL AS codigo_cliente, " +
                "produtos.produto AS produto_venda, " +
                "NULL AS data_movimento, " +
                "SUM(produtos.quantidade) AS quantidade_venda, " +
                "produtos.preco_unitario, " +
                "SUM(produtos.preco_unitario * produtos.quantidade) AS valor_total, " +
                "NULL AS vendedor_venda, " +
                "NULL AS status_autorizacao_venda, " +
                "NULL AS entrega_futura_venda, " +
                "NULL AS entrega_futura_realizada, " +
                "NULL AS usuario_atual, " +
                "NULL AS data_cadastro, " +
                "produtos.codigo_venda_app, " +
                "NULL AS venda_finalizada_app, " +
                "NULL AS chave_importacao " +
                "FROM produtos_vendas_app AS produtos " +
                "LEFT JOIN " + TABELA_VENDAS + " AS vendas ON produtos.codigo_venda_app = vendas.codigo_venda_app " +
                "WHERE vendas.codigo_venda_app IS NULL " +
                "GROUP BY produtos.codigo_venda_app";

        Log.d("SQL Query", "Query executada: " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasPedidosDomain vendas = cursorToVendasPedidos(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        Log.d("getRelatorioVendas", "Número de vendas retornadas: " + cursor.getCount());

        return listaVendas;
    }
/*******************************************************************/

    //
    public ArrayList<RelatorioVendasClientesDomain> getRelatorioVendasClientes(String produto) {
        RelatorioVendasClientesDomain dRelatorio = new RelatorioVendasClientesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        ArrayList<RelatorioVendasClientesDomain> listaVendasS = new ArrayList<>();

        String query = "SELECT " +
                "ven.codigo_venda codigo_venda, " +
                "ven.codigo_cliente codigo_cliente, " +
                "ven.unidade_venda unidade_venda, " +
                "ven.produto_venda produto_venda, " +
                "ven.data_movimento data_movimento, " +
                "SUM(ven.quantidade_venda) quantidade_venda, " +
                "ven.preco_unitario preco_unitario, " +
                "SUM(ven.valor_total) valor_total, " +
                "ven.vendedor_venda vendedor_venda, " +
                "ven.status_autorizacao_venda status_autorizacao_venda, " +
                "ven.entrega_futura_venda entrega_futura_venda, " +
                "ven.entrega_futura_realizada entrega_futura_realizada, " +
                "ven.usuario_atual usuario_atual, " +
                "ven.data_cadastro data_cadastro, " +
                "ven.codigo_venda_app codigo_venda_app, " +
                "(SELECT cli.nome_cliente FROM " + TABELA_CLIENTES + " AS cli WHERE cli.codigo_cliente = ven.codigo_cliente) nome " +
                " FROM " + TABELA_VENDAS + " AS ven " +
                " WHERE ven.produto_venda = '" + produto + "' AND ven.venda_finalizada_app = '1' " +
                " GROUP BY ven.codigo_cliente " +
                " ORDER BY nome";

        Log.e("SQL ", query);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                dRelatorio = new RelatorioVendasClientesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                dRelatorio.setCodigo_venda(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda")));
                dRelatorio.setCodigo_cliente(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente")));
                dRelatorio.setUnidade_venda(cursor.getString(cursor.getColumnIndexOrThrow("unidade_venda")));
                dRelatorio.setProduto_venda(cursor.getString(cursor.getColumnIndexOrThrow("produto_venda")));
                dRelatorio.setData_movimento(cursor.getString(cursor.getColumnIndexOrThrow("data_movimento")));
                dRelatorio.setQuantidade_venda(cursor.getString(cursor.getColumnIndexOrThrow("quantidade_venda")));
                dRelatorio.setPreco_unitario(cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario")));
                dRelatorio.setValor_total(cursor.getString(cursor.getColumnIndexOrThrow("valor_total")));
                dRelatorio.setVendedor_venda(cursor.getString(cursor.getColumnIndexOrThrow("vendedor_venda")));
                dRelatorio.setStatus_autorizacao_venda(cursor.getString(cursor.getColumnIndexOrThrow("status_autorizacao_venda")));
                dRelatorio.setEntrega_futura_venda(cursor.getString(cursor.getColumnIndexOrThrow("entrega_futura_venda")));
                dRelatorio.setEntrega_futura_realizada(cursor.getString(cursor.getColumnIndexOrThrow("entrega_futura_realizada")));
                dRelatorio.setUsuario_atual(cursor.getString(cursor.getColumnIndexOrThrow("usuario_atual")));
                dRelatorio.setData_cadastro(cursor.getString(cursor.getColumnIndexOrThrow("data_cadastro")));
                dRelatorio.setCodigo_venda_app(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda_app")));
                dRelatorio.setNome(cursor.getString(cursor.getColumnIndexOrThrow("nome")));

                listaVendasS.add(dRelatorio);
            } while (cursor.moveToNext());
        }

        /*if (cursor.moveToFirst()) {
            do {
                RelatorioVendasClientesDomain vendas = cursorToRelatorioVendasClientes(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }*/

        return listaVendasS;
    }

    //
    public ArrayList<FinanceiroReceberDomain> getRelatorioContasReceber() {
        ArrayList<FinanceiroReceberDomain> listaVendas = new ArrayList<>();

        String query = "SELECT " +
                "codigo_financeiro, " +
                "unidade_financeiro, " +
                "data_financeiro, " +
                "codigo_cliente_financeiro, " +
                "fpagamento_financeiro, " +
                "documento_financeiro, " +
                "vencimento_financeiro, " +
                "valor_financeiro valor_financeiro, " +
                "status_autorizacao, " +
                "SUM(pago), " +
                "vasilhame_ref, " +
                "usuario_atual, " +
                "data_inclusao, " +
                "nosso_numero_financeiro, " +
                "id_vendedor_financeiro, " +
                "id_financeiro_app " +
                "FROM recebidos " +
                "GROUP BY fpagamento_financeiro";

        //Log.e("SQL = ", query);


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberDomain vendas = cursorToFinanceiroReceberDomain(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    //
    public FinanceiroVendasDomain getBaixaRecebida(String codigo_finan) {
        FinanceiroVendasDomain listaVendas = new FinanceiroVendasDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        String query = "SELECT " +
                "codigo_financeiro, unidade_financeiro, data_financeiro, " +
                "codigo_cliente_financeiro, fpagamento_financeiro, " +
                "documento_financeiro, vencimento_financeiro, " +
                "SUM(valor_financeiro) valor_financeiro, status_autorizacao, " +
                "pago, vasilhame_ref, usuario_atual, data_inclusao, " +
                "nosso_numero_financeiro, id_vendedor_financeiro, id_financeiro_app " +
                "FROM recebidos " +
                "WHERE codigo_financeiro = " + codigo_finan + " " +
                "GROUP BY fpagamento_financeiro";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                listaVendas = cursorToFinanceiroVendasDomain(cursor);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    public String getTotalRecebido(String codigo_finan) {
        String valor_financeiro = "0";
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        //
        String selectQuery = "SELECT pago " +
                "FROM recebidos " +
                "WHERE codigo_financeiro = " + codigo_finan + " " +
                "LIMIT 1";
        Log.e("sql", "ContasReceber: " + selectQuery);
        Log.e("ErrorCR", selectQuery);
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    valor_financeiro = cursor.getString(0);
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("ContasReceber", e.getMessage());
        }

        /*db.endTransaction();
        db.close();*/
        return valor_financeiro;
    }

    public String getTotalRecebidoList(String codigo_finan) {
        String valor_financeiro = "0";
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        //valor_financeiro
        String selectQuery = "SELECT valor_financeiro " +
                "FROM recebidos " +
                "WHERE codigo_financeiro = " + codigo_finan + " " +
                "LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    valor_financeiro = cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"));
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*db.endTransaction();
        db.close();*/
        return valor_financeiro;
    }

    public String getValorFinReceberCli(String codigo_finan) {
        String valor_financeiro = "0";
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        //
        String selectQuery = "SELECT fir.valor_financeiro FROM financeiro_receber fir WHERE fir.codigo_financeiro = '" + codigo_finan + "' LIMIT 1";

        Log.e("ErrorCR", selectQuery);
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    valor_financeiro = cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"));
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*db.endTransaction();
        db.close();*/
        return valor_financeiro;
    }


    //########## FORMAS PAGAMENTO CLIENTE ############

    //CONSTANTES FORMAS PAGAMENTO CLIENTE
    private static final String TABELA_FORMAS_PAGAMENTO = "formas_pagamento";
    private static final String CODIGO_PAGAMENTO = "codigo_pagamento";
    private static final String DESCRICAO_FORMA_PAGAMENTO = "descricao_forma_pagamento";
    private static final String TIPO_FORMA_PAGAMENTO = "tipo_forma_pagamento";
    private static final String AUTO_NUM_PAGAMENTO = "auto_num_pagamento";
    private static final String BAIXA_FORMA_PAGAMENTO = "baixa_forma_pagamento";
    private static final String USUARIO_ATUAL_FORMA_PAGAMENTO = "usuario_atual";
    private static final String DATA_CADASTRO_FORMA_PAGAMENTO = "data_cadastro";
    private static final String ATIVO_FORMA_PAGAMENTO = "ativo";
    private static final String CONTA_BANCARIA_FORMA_PAGAMENTO = "conta_bancaria";


    private static final String[] COLUNAS_FORMAS_PAGAMENTO = {
            CODIGO_PAGAMENTO,
            DESCRICAO_FORMA_PAGAMENTO,
            TIPO_FORMA_PAGAMENTO,
            AUTO_NUM_PAGAMENTO,
            BAIXA_FORMA_PAGAMENTO,
            USUARIO_ATUAL_FORMA_PAGAMENTO,
            DATA_CADASTRO_FORMA_PAGAMENTO,
            ATIVO_FORMA_PAGAMENTO,
            CONTA_BANCARIA_FORMA_PAGAMENTO
    };

    //
    private FormasPagamentoDomain cursorToFormasPagamentoDomain(Cursor cursor) {
        FormasPagamentoDomain formasPagamentoDomain = new FormasPagamentoDomain(null, null, null, null, null, null, null, null, null);

        formasPagamentoDomain.setCodigo_pagamento(cursor.getString(0));
        formasPagamentoDomain.setDescricao_forma_pagamento(cursor.getString(1));
        formasPagamentoDomain.setTipo_forma_pagamento(cursor.getString(2));
        formasPagamentoDomain.setAuto_num_pagamento(cursor.getString(3));
        formasPagamentoDomain.setBaixa_forma_pagamento(cursor.getString(4));
        formasPagamentoDomain.setUsuario_atual(cursor.getString(5));
        formasPagamentoDomain.setData_cadastro(cursor.getString(6));
        formasPagamentoDomain.setAtivo(cursor.getString(7));
        formasPagamentoDomain.setConta_bancaria(cursor.getString(8));

        return formasPagamentoDomain;
    }

    // ######### POS #############################
    public String getPosBaixaPrazo() {
        String baixa_a_prazo = "0";
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        //
        String selectQuery = "SELECT * " +
                "FROM pos ";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    baixa_a_prazo = cursor.getString(cursor.getColumnIndexOrThrow("baixa_a_prazo"));
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*db.endTransaction();
        db.close();*/
        return baixa_a_prazo;
    }

    /************** CONSULTA DE INADIMPLENCIA POR PARAMETRO ***************/

    public boolean isInadimplenteBloqueado() {
        int bloqueio = 0;  // Valor padrão para inativo
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT bloqueio_inadimplente FROM pos";
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                bloqueio = cursor.getInt(cursor.getColumnIndexOrThrow("bloqueio_inadimplente"));
                Log.d("isInadimplenteBloqueado", "Valor retornado pelo banco: " + bloqueio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }

        return bloqueio == 1;  // Retorna true se o bloqueio for 1 (ativo), false se for 0 (inativo)
    }



//########## FORMAS PAGAMENTO CLIENTE ############

    //CONSTANTES FORMAS PAGAMENTO CLIENTE
    private static final String TABELA_FORMAS_PAGAMENTO_CLIENTE = "formas_pagamento_cliente";
    private static final String CODIGO_PAGAMENTO_CLIENTE = "codigo_pagamento_cliente";
    private static final String PAGAMENTO_CLIENTE = "pagamento_cliente";
    private static final String PAGAMENTO_PRAZO_CLIENTE = "pagamento_prazo_cliente";
    private static final String CLIENTE_PAGAMENTO = "cliente_pagamento";
    private static final String USUARIO = "usuario";

    private static final String[] COLUNAS_FORMAS_PAGAMENTO_CLIENTE = {
            CODIGO_PAGAMENTO_CLIENTE,
            PAGAMENTO_CLIENTE,
            PAGAMENTO_PRAZO_CLIENTE,
            CLIENTE_PAGAMENTO,
            USUARIO
    };

    //
    private FormasPagamentoClienteDomain cursorToFormasPagamentoClienteDomain(Cursor cursor) {
        FormasPagamentoClienteDomain formasPagamentoClienteDomain = new FormasPagamentoClienteDomain(null, null, null, null, null);

        formasPagamentoClienteDomain.setCodigo_pagamento_cliente(cursor.getString(0));
        formasPagamentoClienteDomain.setPagamento_cliente(cursor.getString(1));
        formasPagamentoClienteDomain.setPagamento_prazo_cliente(cursor.getString(2));
        formasPagamentoClienteDomain.setCliente_pagamento(cursor.getString(3));
        formasPagamentoClienteDomain.setUsuario(cursor.getString(4));

        return formasPagamentoClienteDomain;
    }

    // Kleilson Teste
    public ArrayList<String> getFormasPagamentoCliente(String codigoCliente) {
        ArrayList<String> list = new ArrayList<>();
        String baixa = getPosBaixaPrazo();
        //
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        /*
        fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria
        FROM formas_pagamento fpg
        WHERE fpg.tipo_forma_pagamento = 'A VISTA'
        UNION ALL
        SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,
        fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria
        FROM formas_pagamento fpg
        INNER JOIN formas_pagamento_cliente fpc ON fpc.pagamento_cliente = fpg.descricao_forma_pagamento
        WHERE fpc.cliente_pagamento = '813'
         */
        //
        String selectQuery = "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                "FROM formas_pagamento fpg\n" +
                "WHERE fpg.tipo_forma_pagamento = 'A VISTA' AND fpg.ativo\n" +
                "UNION ALL\n" +
                "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                "FROM formas_pagamento fpg\n" +
                "INNER JOIN formas_pagamento_cliente fpc ON fpc.pagamento_cliente = fpg.descricao_forma_pagamento\n" +
                "WHERE fpc.cliente_pagamento = '" + codigoCliente + "' AND fpg.ativo";


        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("descricao_forma_pagamento"));
                    String tipo_pagamento = cursor.getString(cursor.getColumnIndexOrThrow("tipo_forma_pagamento"));
                    list.add(
                            pagamento_cliente + " _ " +
                                    tipo_pagamento + " _ " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("auto_num_pagamento")) + " _ " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("baixa_forma_pagamento"))
                    );
                    //list.add(descricao_produto);
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*//
        String selectQuery = "Select * From " + TABELA_FORMAS_PAGAMENTO +
                " WHERE tipo_forma_pagamento = 'A VISTA'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("descricao_forma_pagamento"));
                    list.add(pagamento_cliente + " _ " + "A VISTA");
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.endTransaction();
            //db.close();
        }

        //
        String selectQueryFPC = "Select * From " + TABELA_FORMAS_PAGAMENTO_CLIENTE +
                " WHERE cliente_pagamento = '" + codigoCliente + "'";
        Cursor cursorFPC = db.rawQuery(selectQueryFPC, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursorFPC.getCount() > 0) {
                while (cursorFPC.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursorFPC.getString(cursorFPC.getColumnIndexOrThrow("pagamento_cliente"));
                    list.add(pagamento_cliente + " _ " + "A PRAZO");
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }*/

        /*db.endTransaction();
        db.close();*/
        // Logando o conteúdo da lista
        Log.d("FormasPagamentoCliente", "Lista de formas de pagamento: " + list.toString());

        return list;
    }

    // Kleilson Teste
    public ArrayList<String> getFormasPagamentoClienteBaixa(String codigoCliente) {
        ArrayList<String> list = new ArrayList<>();
        String baixa = getPosBaixaPrazo();
        //
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        /*
        fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria
        FROM formas_pagamento fpg
        WHERE fpg.tipo_forma_pagamento = 'A VISTA'
        UNION ALL
        SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,
        fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria
        FROM formas_pagamento fpg
        INNER JOIN formas_pagamento_cliente fpc ON fpc.pagamento_cliente = fpg.descricao_forma_pagamento
        WHERE fpc.cliente_pagamento = '813'
         */
        //
        String selectQuery = "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                "FROM formas_pagamento fpg\n" +
                "WHERE fpg.tipo_forma_pagamento = 'A VISTA' AND fpg.ativo";

        //String baixa = this.getPosBaixaPrazo();
        if (baixa.equalsIgnoreCase("1")) {
            selectQuery += "\n" +
                    "UNION ALL\n" +
                    "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                    "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                    "FROM formas_pagamento fpg\n" +
                    "INNER JOIN formas_pagamento_cliente fpc ON fpc.pagamento_cliente = fpg.descricao_forma_pagamento\n" +
                    "WHERE fpg.baixa_forma_pagamento = '2' AND fpc.cliente_pagamento = '" + codigoCliente + "' AND fpg.ativo";
        }


        Cursor cursor = db.rawQuery(selectQuery, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("descricao_forma_pagamento"));
                    String tipo_pagamento = cursor.getString(cursor.getColumnIndexOrThrow("tipo_forma_pagamento"));
                    list.add(
                            pagamento_cliente + " _ " +
                                    tipo_pagamento + " _ " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("auto_num_pagamento")) + " _ " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("baixa_forma_pagamento"))
                    );
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.endTransaction();
            //db.close();
        }

        /*//
        String selectQuery = "Select * From " + TABELA_FORMAS_PAGAMENTO +
                " WHERE tipo_forma_pagamento = 'A VISTA'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("descricao_forma_pagamento"));
                    list.add(pagamento_cliente + " _ " + "A VISTA");
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.endTransaction();
            //db.close();
        }

        //
        String selectQueryFPC = "Select * From " + TABELA_FORMAS_PAGAMENTO_CLIENTE +
                " WHERE cliente_pagamento = '" + codigoCliente + "'";
        Cursor cursorFPC = db.rawQuery(selectQueryFPC, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursorFPC.getCount() > 0) {
                while (cursorFPC.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursorFPC.getString(cursorFPC.getColumnIndexOrThrow("pagamento_cliente"));
                    list.add(pagamento_cliente + " _ " + "A PRAZO");
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }*/

        db.endTransaction();
        db.close();
        return list;
    }

    //************ TABELA FINANCEIRO **************

    //CONSTANTES FINANCEIRO
    private static final String TABELA_FINANCEIRO = "financeiro";
    private static final String CODIGO_FINANCEIRO = "codigo_financeiro";
    private static final String UNIDADE_FINANCEIRO = "unidade_financeiro";
    private static final String DATA_FINANCEIRO = "data_financeiro";
    private static final String CODIGO_CLIENTE_FINANCEIRO = "codigo_cliente_financeiro";
    private static final String FPAGAMENTO_FINANCEIRO = "fpagamento_financeiro";
    private static final String DOCUMENTO_FINANCEIRO = "documento_financeiro";
    private static final String VENCIMENTO_FINANCEIRO = "vencimento_financeiro";
    private static final String VALOR_FINANCEIRO = "valor_financeiro";
    private static final String STATUS_AUTORIZACAO = "status_autorizacao";
    private static final String PAGO = "pago";
    private static final String VASILHAME_REF = "vasilhame_ref";
    private static final String USUARIO_ATUAL_FINANCEIRO = "usuario_atual";
    private static final String DATA_INCLUSAO = "data_inclusao";
    private static final String NOSSO_NUMERO_FINANCEIRO = "nosso_numero_financeiro";
    private static final String ID_VENDEDOR_FINANCEIRO = "id_vendedor_financeiro";
    private static final String ID_FINANCEIRO_APP = "id_financeiro_app";
    private static final String NOTA_FISCAL = "nota_fiscal";
    private static final String CODIGO_ALIQUOTA = "codigo_aliquota";

    private static final String[] COLUNAS_FINANCEIRO = {
            CODIGO_FINANCEIRO,
            UNIDADE_FINANCEIRO,
            DATA_FINANCEIRO,
            CODIGO_CLIENTE_FINANCEIRO,
            FPAGAMENTO_FINANCEIRO,
            DOCUMENTO_FINANCEIRO,
            VENCIMENTO_FINANCEIRO,
            VALOR_FINANCEIRO,
            STATUS_AUTORIZACAO,
            PAGO,
            VASILHAME_REF,
            USUARIO_ATUAL_FINANCEIRO,
            DATA_INCLUSAO,
            NOSSO_NUMERO_FINANCEIRO,
            ID_VENDEDOR_FINANCEIRO,
            ID_FINANCEIRO_APP
    };

    //
    private FinanceiroVendasDomain cursorToFinanceiroVendasDomain(Cursor cursor) {
        FinanceiroVendasDomain financeiroVendasDomain = new FinanceiroVendasDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        financeiroVendasDomain.setCodigo_financeiro(cursor.getString(0));
        financeiroVendasDomain.setUnidade_financeiro(cursor.getString(1));
        financeiroVendasDomain.setData_financeiro(cursor.getString(2));
        financeiroVendasDomain.setCodigo_cliente_financeiro(cursor.getString(3));
        financeiroVendasDomain.setFpagamento_financeiro(cursor.getString(4));
        financeiroVendasDomain.setDocumento_financeiro(cursor.getString(5));
        financeiroVendasDomain.setVencimento_financeiro(cursor.getString(6));
        financeiroVendasDomain.setValor_financeiro(cursor.getString(7));
        financeiroVendasDomain.setStatus_autorizacao(cursor.getString(8));
        financeiroVendasDomain.setPago(cursor.getString(9));
        financeiroVendasDomain.setVasilhame_ref(cursor.getString(10));
        financeiroVendasDomain.setUsuario_atual(cursor.getString(11));
        financeiroVendasDomain.setData_inclusao(cursor.getString(12));
        financeiroVendasDomain.setNosso_numero_financeiro(cursor.getString(13));
        financeiroVendasDomain.setId_vendedor_financeiro(cursor.getString(14));
        financeiroVendasDomain.setId_financeiro_app(cursor.getString(15));
        financeiroVendasDomain.setNota_fiscal(cursor.getString(16));
        financeiroVendasDomain.setCodigo_aliquota(cursor.getString(17));

        return financeiroVendasDomain;
    }

    //
    private FinanceiroReceberDomain cursorToFinanceiroReceberDomain(Cursor cursor) {
        FinanceiroReceberDomain financeiroReceberDomain = new FinanceiroReceberDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        financeiroReceberDomain.setCodigo_financeiro(cursor.getString(0));
        financeiroReceberDomain.setUnidade_financeiro(cursor.getString(1));
        financeiroReceberDomain.setData_financeiro(cursor.getString(2));
        financeiroReceberDomain.setCodigo_cliente_financeiro(cursor.getString(3));
        financeiroReceberDomain.setFpagamento_financeiro(cursor.getString(4));
        financeiroReceberDomain.setDocumento_financeiro(cursor.getString(5));
        financeiroReceberDomain.setVencimento_financeiro(cursor.getString(6));
        financeiroReceberDomain.setValor_financeiro(cursor.getString(7));
        financeiroReceberDomain.setStatus_autorizacao(cursor.getString(8));
        financeiroReceberDomain.setPago(cursor.getString(9));
        financeiroReceberDomain.setVasilhame_ref(cursor.getString(10));
        financeiroReceberDomain.setUsuario_atual(cursor.getString(11));
        financeiroReceberDomain.setData_inclusao(cursor.getString(12));
        financeiroReceberDomain.setNosso_numero_financeiro(cursor.getString(13));
        financeiroReceberDomain.setId_vendedor_financeiro(cursor.getString(14));
        financeiroReceberDomain.setId_financeiro_app(cursor.getString(15));

        return financeiroReceberDomain;
    }

    //
    private FormasPagamentoReceberTemp cursorToFormasPagamentoReceberTemp(Cursor cursor) {
        FormasPagamentoReceberTemp formasPagamentoReceberTemp = new FormasPagamentoReceberTemp(null, null, null, null);

        formasPagamentoReceberTemp.setId(cursor.getString(0));
        formasPagamentoReceberTemp.setId_cliente(cursor.getString(1));
        formasPagamentoReceberTemp.setId_forma_pagamento(cursor.getString(2));
        formasPagamentoReceberTemp.setValor(cursor.getString(3));

        return formasPagamentoReceberTemp;
    }

    //
    public void addFinanceiro(FinanceiroVendasDomain financeiroVendasDomain) {
        myDataBase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_FINANCEIRO, financeiroVendasDomain.getCodigo_financeiro());
        values.put(UNIDADE_FINANCEIRO, financeiroVendasDomain.getUnidade_financeiro());
        values.put(DATA_FINANCEIRO, financeiroVendasDomain.getData_financeiro());
        values.put(CODIGO_CLIENTE_FINANCEIRO, financeiroVendasDomain.getCodigo_cliente_financeiro());
        values.put(FPAGAMENTO_FINANCEIRO, financeiroVendasDomain.getFpagamento_financeiro());
        values.put(DOCUMENTO_FINANCEIRO, financeiroVendasDomain.getDocumento_financeiro());
        values.put(VENCIMENTO_FINANCEIRO, financeiroVendasDomain.getVencimento_financeiro());
        values.put(VALOR_FINANCEIRO, financeiroVendasDomain.getValor_financeiro());
        values.put(STATUS_AUTORIZACAO, financeiroVendasDomain.getStatus_autorizacao());
        values.put(PAGO, financeiroVendasDomain.getPago());
        values.put(VASILHAME_REF, financeiroVendasDomain.getVasilhame_ref());
        values.put(USUARIO_ATUAL_FINANCEIRO, financeiroVendasDomain.getUsuario_atual());
        values.put(DATA_INCLUSAO, financeiroVendasDomain.getData_inclusao());
        values.put(NOSSO_NUMERO_FINANCEIRO, financeiroVendasDomain.getNosso_numero_financeiro());
        values.put(ID_VENDEDOR_FINANCEIRO, financeiroVendasDomain.getId_vendedor_financeiro());
        values.put(ID_FINANCEIRO_APP, financeiroVendasDomain.getId_financeiro_app());
        values.put(NOTA_FISCAL, financeiroVendasDomain.getNota_fiscal());
        values.put(CODIGO_ALIQUOTA, financeiroVendasDomain.getCodigo_aliquota());
        myDataBase.insert(TABELA_FINANCEIRO, null, values);
        myDataBase.close();
    }

    // add financeiro recebidos
    public void addFinanceiroRecebidos(FinanceiroVendasDomain financeiroVendasDomain) {
        myDataBase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_FINANCEIRO, financeiroVendasDomain.getCodigo_financeiro());
        values.put(UNIDADE_FINANCEIRO, financeiroVendasDomain.getUnidade_financeiro());
        values.put(DATA_FINANCEIRO, financeiroVendasDomain.getData_financeiro());
        values.put(CODIGO_CLIENTE_FINANCEIRO, financeiroVendasDomain.getCodigo_cliente_financeiro());
        values.put(FPAGAMENTO_FINANCEIRO, financeiroVendasDomain.getFpagamento_financeiro());
        values.put(DOCUMENTO_FINANCEIRO, financeiroVendasDomain.getDocumento_financeiro());
        values.put(VENCIMENTO_FINANCEIRO, financeiroVendasDomain.getVencimento_financeiro());
        values.put(VALOR_FINANCEIRO, financeiroVendasDomain.getValor_financeiro());
        values.put(STATUS_AUTORIZACAO, financeiroVendasDomain.getStatus_autorizacao());
        values.put(PAGO, financeiroVendasDomain.getPago());
        values.put(VASILHAME_REF, financeiroVendasDomain.getVasilhame_ref());
        values.put(USUARIO_ATUAL_FINANCEIRO, financeiroVendasDomain.getUsuario_atual());
        values.put(DATA_INCLUSAO, financeiroVendasDomain.getData_inclusao());
        values.put(NOSSO_NUMERO_FINANCEIRO, financeiroVendasDomain.getNosso_numero_financeiro());
        values.put(ID_VENDEDOR_FINANCEIRO, financeiroVendasDomain.getId_vendedor_financeiro());
        values.put(ID_FINANCEIRO_APP, financeiroVendasDomain.getId_financeiro_app());

        Log.e("SQL", "addFinanceiroRecebidos: " + values);
        Log.e("ErrorCR", "addFinanceiroRecebidos: " + values);

        myDataBase.insert("recebidos", null, values);
    }

    //
    public void addValorFinReceber(String id_cliente, String id_forma_pagamento, String valor) {
        myDataBase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_cliente", id_cliente);
        values.put("id_forma_pagamento", id_forma_pagamento);
        values.put("valor", valor);
        Log.e("SQL", "addValorFinReceber: " + values);
        myDataBase.insert("formas_pagamento_receber", null, values);
    }

    // VERIFICA SE A FORMA DE PAGAMENTO ESCOLHIDA JÁ EXISTE EM RECEBIDOS
    public String[] verForPagRecTemp(String fpagamento_financeiro, String codigo_financeiro_app) {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String query = "SELECT id, valor " +
                "FROM formas_pagamento_receber " +
                "WHERE id_forma_pagamento = '" + fpagamento_financeiro + "'";
        Log.e("SQL", "verForPagRecTemp - " + query);

        Cursor cursor = myDataBase.rawQuery(query, null);

        String[] codigo_financeiro = new String[]{"0", ""};
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                codigo_financeiro = new String[]{cursor.getString(0), cursor.getString(1)};
            }
            //myDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return codigo_financeiro;
    }

    // ATUALIZA OS VALORES DAS BAIXAS RECEBIDAS
    public int updateFinRecTemp(String id, String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("valor", valor);

        int i = myDataBase.update(
                "formas_pagamento_receber",
                values,
                "id" + " = ?",
                new String[]{id}
        );

        return i;
    }


    // ATUALIZA OS VALORES DAS BAIXAS RECEBIDAS
    public int updateFinanceiroRecebidos(String codigo_financeiro, String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("valor_financeiro", valor);

        int i = myDataBase.update(
                "recebidos",
                values,
                "codigo_financeiro" + " = ?",
                new String[]{codigo_financeiro}
        );

        return i;
    }

    // ATUALIZA OS VALORES DAS BAIXAS RECEBIDAS
    public int updateUltimoBoleto(String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ultboleto", valor);

        int i = myDataBase.update(
                "pos",
                values,
                null,
                null
        );
        /* , "id" + " = ?", new String[]{id}*/
        return i;
    }

    /******************** ATUALIZAR CAMPO **************/
    public int updateFormaPagamentoFinanceiro(int id_financeiro_app, String novaFormaPagamento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Atualiza apenas o campo da forma de pagamento (fpagamento_financeiro)
        values.put("fpagamento_financeiro", novaFormaPagamento);

        // Atualiza o registro da tabela financeiro, onde id_financeiro_app corresponde ao da venda
        int result = db.update(
                TABELA_FINANCEIRO, // Nome da tabela
                values, // Os valores que serão atualizados
                "id_financeiro_app = ?", // Condição para encontrar o registro correto
                new String[]{String.valueOf(id_financeiro_app)} // Argumentos da condição
        );
        db.close(); // Fecha a conexão com o banco de dados
        return result; // Retorna o número de linhas atualizadas
    }



    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FinanceiroVendasDomain> getFinanceiroCliente(int id_financeiro_app) {
        ArrayList<FinanceiroVendasDomain> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_FINANCEIRO + " WHERE id_financeiro_app = '" + id_financeiro_app + "'";
        Log.e("SQL", "getFinanceiroCliente - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroVendasDomain financeiro = cursorToFinanceiroVendasDomain(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }


    //LISTAR AS VALORES INSERIDOS AO FINANCEIRO A RECEBER DO CLIENTE
    public ArrayList<FormasPagamentoReceberTemp> getFinanceiroClienteRecebidos(int id_cliente) {
        //SQLiteDatabase
        myDataBase = this.getReadableDatabase();
        ArrayList<FormasPagamentoReceberTemp> listaFormasPagamentoReceber = new ArrayList<>();

        String query = "SELECT * " +
                "FROM formas_pagamento_receber " +
                "WHERE id_cliente = '" + id_cliente + "' ";
        Log.e("ErrorCR", query);

        //Log.e("SQL", "getFinanceiroClienteRecebidos - " + query);
        Cursor cursor = myDataBase.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    FormasPagamentoReceberTemp temp = cursorToFormasPagamentoReceberTemp(cursor);
                    listaFormasPagamentoReceber.add(temp);

                    //Log.e("SQL", "getFinanceiroClienteRecebidos - " + temp.getValor());
                    //Log.e("SQL", "getFinanceiroClienteRecebidos - " + temp.getId_forma_pagamento());
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {

            Log.e("SQL", "erro = " + e.getMessage());
        }

        //db.close();
        return listaFormasPagamentoReceber;
    }
    /*public ArrayList<FinanceiroVendasDomain> getFinanceiroClienteRecebidos(int id_financeiro_app) {
        //SQLiteDatabase
        myDataBase = this.getReadableDatabase();
        ArrayList<FinanceiroVendasDomain> listaFinanceiroVendas = new ArrayList<>();

        //
        StringBuilder filtro = new StringBuilder();
        for (int i = 0; i < IdsCR.size(); i++) {
            if (i > 0) {
                filtro.append(" OR");
            }
            filtro.append(" codigo_financeiro = '").append(IdsCR.get(i)).append("'");
        }

        //id_financeiro_app = '" + id_financeiro_app + "'" filtro.toString() + " " +
        String query = "SELECT codigo_financeiro, unidade_financeiro, data_financeiro, " +
                "codigo_cliente_financeiro, fpagamento_financeiro, documento_financeiro, " +
                "vencimento_financeiro, SUM(valor_financeiro) valor_financeiro, status_autorizacao, " +
                "pago, vasilhame_ref, usuario_atual, data_inclusao, nosso_numero_financeiro, " +
                "id_vendedor_financeiro, id_financeiro_app " +
                "FROM recebidos " +
                "WHERE" + filtro.toString() + " " +
                "GROUP BY fpagamento_financeiro";

        Log.e("SQL", "getFinanceiroClienteRecebidos - " + query);
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroVendasDomain financeiro = cursorToFinanceiroVendasDomain(cursor);
                listaFinanceiroVendas.add(financeiro);

                Log.e("SQL", "getFinanceiroClienteRecebidos - " + financeiro.getCodigo_financeiro());
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }
*/

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FinanceiroReceberClientes> getContasReceberCliente(String id_cliente) {
        ArrayList<FinanceiroReceberClientes> listaFinanceiroVendas = new ArrayList<>();

        /*String query = "SELECT * " +
                "FROM financeiro_receber " +
                "WHERE codigo_cliente = '" + id_cliente + "' AND status_app = '1'" +
                " AND baixa_finalizada_app = '0'";*/
        //String query = "SELECT * FROM financeiro_receber WHERE codigo_cliente = '" + id_cliente + "' AND valor_financeiro != total_pago";

        String query = "SELECT * FROM financeiro_receber AS fir WHERE fir.codigo_cliente = '" + id_cliente + "' AND " +
                "fir.valor_financeiro > ( SELECT (CASE WHEN Sum(rec.pago) IS NOT NULL THEN Sum(rec.pago) ELSE 0 END) AS pago FROM recebidos rec WHERE rec.codigo_financeiro = fir.codigo_financeiro ) \n" +
                "ORDER BY fir.vencimento_financeiro";

        /*"SELECT *" +
                "  FROM financeiro_receber AS fir" +
                " WHERE fir.codigo_cliente = '" + id_cliente + "' AND " +
                "       fir.valor_financeiro > (" +
                "                                  SELECT (CASE WHEN Sum(rec.pago) IS NOT NULL THEN Sum(rec.pago) ELSE 0 END) AS pago" +
                "                                    FROM recebidos rec" +
                "                                   WHERE rec.codigo_financeiro = fir.codigo_financeiro" +
                "                              )*/

        Log.e("SQL", "getContasReceberCliente - " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberClientes financeiro = cursorToContasReceberCliente(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }

    //RETORNA AS CONTAS A RECEBER QUE ESTÃO PENDENTE
    public ArrayList<FinanceiroReceberClientes> getListFormContasReceberCliente(String id_cliente) {
        ArrayList<FinanceiroReceberClientes> listaFinanceiroVendas = new ArrayList<>();

        // AND valor_financeiro != total_pago
        String query = "SELECT * FROM financeiro_receber WHERE codigo_cliente = '" + id_cliente + "' ORDER BY data_financeiro, codigo_financeiro";
        Log.e("SQL", "getListFormContasReceberCliente - " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberClientes financeiro = cursorToContasReceberCliente(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }
        return listaFinanceiroVendas;
    }

    // Método para obter o saldo de um cliente
    public BigDecimal getSaldoCliente(String codigoCliente) {
        SQLiteDatabase db = this.getReadableDatabase();
        BigDecimal saldo = BigDecimal.ZERO;

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABELA_CLIENTES,
                    new String[]{SALDO_CLIENTE},
                    CODIGO_CLIENTE + " = ?",
                    new String[]{codigoCliente},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                saldo = new BigDecimal(cursor.getString(cursor.getColumnIndexOrThrow(SALDO_CLIENTE)));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Erro ao consultar saldo: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return saldo;
    }



    /*//LISTAR TODOS OS CLIENTES
    public ArrayList<Clientes> getAllClientesContasReceber() {
        ArrayList<Clientes> listaClientes = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_CLIENTES + " " +
                "INNER JOIN financeiro_receber ON " +
                "financeiro_receber.codigo_cliente_financeiro = " + TABELA_CLIENTES + "." + CODIGO_CLIENTE +
                " GROUP BY " + TABELA_CLIENTES + "." + NOME_CLIENTE +
                " ORDER BY " + TABELA_CLIENTES + "." + NOME_CLIENTE;
        Log.e("SQL", query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Clientes clientes = cursorToCliente(cursor);
                listaClientes.add(clientes);
            } while (cursor.moveToNext());
        }

        return listaClientes;
    }*/

    //
    private FinanceiroReceberClientes cursorToContasReceberCliente(Cursor cursor) {
        FinanceiroReceberClientes clientes = new FinanceiroReceberClientes(null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        clientes.setCodigo_financeiro(cursor.getString(0));
        clientes.setNosso_numero_financeiro(cursor.getString(1));
        clientes.setData_financeiro(cursor.getString(2));
        clientes.setCodigo_cliente(cursor.getString(3));
        clientes.setNome_cliente(cursor.getString(4));
        clientes.setDocumento_financeiro(cursor.getString(5));
        clientes.setFpagamento_financeiro(cursor.getString(6));
        clientes.setVencimento_financeiro(cursor.getString(7));
        clientes.setValor_financeiro(cursor.getString(8));
        clientes.setTotal_pago(cursor.getString(9));
        clientes.setCodigo_pagamento(cursor.getString(10));
        clientes.setStatus_app(cursor.getString(11));
        clientes.setBaixa_finalizada_app(cursor.getString(12));
        return clientes;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<ClientesContasReceber> getAllClientesContasReceber() {
        ArrayList<ClientesContasReceber> listaClientes = new ArrayList<>();

        //
        /*String query = "SELECT * FROM financeiro_receber " +
                "INNER JOIN " + TABELA_CLIENTES + " ON " +
                TABELA_CLIENTES + "." + CODIGO_CLIENTE + " = financeiro_receber.codigo_cliente" +
                " WHERE status_app = '1'" +
                " GROUP BY " + TABELA_CLIENTES + "." + CODIGO_CLIENTE +
                " ORDER BY " + TABELA_CLIENTES + "." + NOME_CLIENTE;*/

        /*String query = "SELECT * " +
                "FROM financeiro_receber " +
                "INNER JOIN clientes ON clientes.codigo_cliente = financeiro_receber.codigo_cliente " +
                "WHERE status_app = '0' " +
                "GROUP BY clientes.codigo_cliente ORDER BY clientes.nome_cliente";*/

        /*String query = "SELECT * " +
                "FROM financeiro_receber " +
                "INNER JOIN clientes ON clientes.codigo_cliente = financeiro_receber.codigo_cliente " +
                "GROUP BY clientes.codigo_cliente ORDER BY clientes.nome_cliente";*/
        String query = "SELECT *" +
                "  FROM financeiro_receber fir" +
                "       INNER JOIN" +
                "       clientes cli ON cli.codigo_cliente = fir.codigo_cliente" +
                " WHERE fir.valor_financeiro > (" +
                "                                  SELECT (CASE WHEN Sum(rec.pago) IS NOT NULL THEN Sum(rec.pago) ELSE 0 END) AS pago" +
                "                                    FROM recebidos rec" +
                "                                   WHERE rec.codigo_financeiro = fir.codigo_financeiro" +
                "                              )" +
                " GROUP BY cli.codigo_cliente" +
                " ORDER BY cli.nome_cliente";

        Log.e("SQL", "getAllClientesContasReceber - " + query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        //
        if (cursor.moveToFirst()) {
            do {
                ClientesContasReceber clientes = new ClientesContasReceber();// cursorToContasReceberCliente(cursor);

                clientes.codigo_financeiro = cursor.getString(0);
                clientes.nosso_numero_financeiro = cursor.getString(1);
                clientes.data_financeiro = cursor.getString(2);
                clientes.codigo_cliente = cursor.getString(3);
                clientes.nome_cliente = cursor.getString(4);
                clientes.documento_financeiro = cursor.getString(5);
                clientes.fpagamento_financeiro = cursor.getString(6);
                clientes.vencimento_financeiro = cursor.getString(7);
                clientes.valor_financeiro = cursor.getString(8);
                clientes.total_pago = cursor.getString(9);
                clientes.codigo_pagamento = cursor.getString(10);
                clientes.status_app = cursor.getString(11);
                clientes.baixa_finalizada_app = cursor.getString(12);
                clientes.endereco = cursor.getString(20);
                clientes.apelido_cliente = cursor.getString(21);

                listaClientes.add(clientes);
            } while (cursor.moveToNext());
        }

        return listaClientes;
    }


    //ALTERAR CLIENTE
    public int updateFinanceiroReceber(String codigo_financeiro, String status, int id_baixa_app) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status_app", status);
        values.put("id_baixa_app", String.valueOf(id_baixa_app));

        int i = myDataBase.update(
                "financeiro_receber",
                values,
                "codigo_financeiro" + " = ?",
                new String[]{String.valueOf(codigo_financeiro)}
        );
        //db.close();
        return i;
    }


    //ALTERAR CLIENTE
    public int updateFinalizarVenda(String codigo_venda_app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VENDA_FINALIZADA_APP, "1");

        int i = db.update(
                TABELA_VENDAS,
                values,
                CODIGO_VENDA_APP + " = ?",
                new String[]{String.valueOf(codigo_venda_app)}
        );
        db.close();
        return i;
    }

    //ALTERAR CLIENTE
    public int updatePosApp(String val) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ultpromissoria", val);

        int i = myDataBase.update(
                "pos",
                values,
                null,
                null
        );
        myDataBase.close();
        return i;
    }

    //ALTERAR CLIENTE
    public int updatePosAppUltimoBoleto(String val) {
        Log.e("BOLETO SQL", val);
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ultboleto", val);

        int i = myDataBase.update(
                "pos",
                values,
                null,
                null
        );
        //myDataBase.close();
        return i;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getValorTotalFinanceiro(String codigo_financeiro_app) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_financeiro) FROM " + TABELA_FINANCEIRO + " WHERE " + ID_FINANCEIRO_APP + " = '" + codigo_financeiro_app + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO A RECEBER
    public String SomaValTotFinReceber(String id_cliente) {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String selectQuery = "SELECT SUM(valor) FROM " + "formas_pagamento_receber" + " WHERE id_cliente = '" + id_cliente + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }/* finally {
            db.endTransaction();
            db.close();
        }*/

        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO A RECEBER
    public String getValorTotalFinanceiroReceber(String codigo_financeiro_app) {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_financeiro) FROM " + "recebidos" + " WHERE " + ID_FINANCEIRO_APP + " = '" + codigo_financeiro_app + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }/* finally {
            db.endTransaction();
            db.close();
        }*/

        return total;
    }

    // VERIFICA SE A FORMA DE PAGAMENTO ESCOLHIDA JÁ EXISTE EM RECEBIDOS
    public String[] verFormaPagamentoRecebidos(String fpagamento_financeiro, String codigo_financeiro_app) {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String query = "SELECT codigo_financeiro, valor_financeiro " +
                "FROM recebidos " +
                "WHERE fpagamento_financeiro = '" + fpagamento_financeiro + "' AND id_financeiro_app = '" + codigo_financeiro_app + "'";
        Log.e("SQL", "verFormaPagamentoRecebidos - " + query);

        Cursor cursor = myDataBase.rawQuery(query, null);

        String[] codigo_financeiro = new String[]{"0", ""};
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                codigo_financeiro = new String[]{cursor.getString(0), cursor.getString(1)};
            }
            //myDataBase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return codigo_financeiro;
    }

    //
    public int deleteFinanceiroRecebidos(int id_baixa_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "recebidos",
                ID_FINANCEIRO_APP + " = ?",
                new String[]{String.valueOf(id_baixa_app)}
        );

        //
        updateFinanceiroReceber(id_baixa_app);

        //
        db.close();
        return i;
    }

    //
    private int updateFinanceiroReceber(int id_baixa_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        //
        ContentValues values = new ContentValues();
        values.put("status_app", "1");
        values.put("id_baixa_app", "0");
        int a = 0;

        try {
            a = db.update(
                    "financeiro_receber",
                    values,
                    "id_baixa_app" + " = ?",
                    new String[]{String.valueOf(id_baixa_app)}
            );
        } catch (Exception e) {

        }

        //
        db.close();
        return a;
    }

    //
    public int deleteItemFinanceiro(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_FINANCEIRO,
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }

    //
    public int deleteItemFinanceiroReceberTemp(FormasPagamentoReceberTemp temp) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "formas_pagamento_receber",
                "id = ?",
                new String[]{String.valueOf(temp.getId())}
        );
        db.close();
        return i;
    }

    //
    public int deleteFinanceiroReceberTemp() {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "formas_pagamento_receber",
                null,
                null
        );
        db.close();
        return i;
    }

    //
    public int deleteItemFinanceiroReceber(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "recebidos",
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }

    /*################### GERENCIAR VENDAS #####################*/
    //
    public int apagarVendasNaoFinalizadas(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }



    /*################### MARGEN CLIENTES #####################*/
    //
    public String getMargemCliente(String produto, String id) {
        //BANCO DE DADOS
        SQLiteDatabase db = this.getReadableDatabase();

        //
        ClassAuxiliar cAux = new ClassAuxiliar();

        /*//
        String preco_unidade = null;
        String margem_cliente = null;
        String preco;

        try {

            //
            Cursor cursor;
            String query = "" +
                    "SELECT unp.preco_unidade, mac.margem_cliente " +
                    "FROM unidades_precos unp " +
                    "INNER JOIN margens_clientes mac ON mac.produto_margem_cliente = unp.produto_preco " +
                    "WHERE unp.produto_preco = '" + produto + "' AND mac.codigo_cliente_margem_cliente = '" + id + "'" +
                    "";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {

                    preco_unidade = cursor.getString(cursor.getColumnIndexOrThrow("preco_unidade"));
                    margem_cliente = cursor.getString(cursor.getColumnIndexOrThrow("margem_cliente"));

                } while (cursor.moveToNext());
            }

            //
            String[] sub = new String[]{preco_unidade, margem_cliente};

            preco = cAux.maskMoney(new BigDecimal(String.valueOf(cAux.subitrair(sub))));

        } catch (Exception e) {
            preco = "0,00";
        }

        Log.e("MARGEN ", preco);

        return preco;*/
        //
        String preco_unidade = null;
        String preco_rota = null;
        String margem_cliente = null;
        String preco;
        String rota = this.getRotaCliente(id);
        isPrecoFixo = false; // Inicializa como falso


        // PREÇO PO UNIDADE
        try {

            //
            Cursor cursor;
            /* +
                    "" +
                    "SELECT unp.preco_unidade, mac.margem_cliente " +
                    "FROM unidades_precos unp " +
                    "INNER JOIN margens_clientes mac ON mac.produto_margem_cliente = unp.produto_preco " +
                    "WHERE unp.produto_preco = '" + produto + "' AND mac.codigo_cliente_margem_cliente = '" + id + "'" +
                    ""*/
            String query = "SELECT (unp.preco_unidade - ( " + "           SELECT IFNULL( ( " +
                    "               SELECT mar.margem_cliente " +
                    "               FROM margens_clientes mar " +
                    "                  WHERE mar.produto_margem_cliente = unp.produto_preco AND " +
                    "                        mar.codigo_cliente_margem_cliente = '" + id + "' " +
                    "               ), 0) " +
                    "                            ) " +
                    "       ) AS margem_cliente " +
                    "  FROM unidades_precos unp " +
                    " WHERE unp.produto_preco = '" + produto + "' AND unp.preco_unidade != '0.00'";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    preco_unidade = cursor.getString(cursor.getColumnIndexOrThrow("margem_cliente"));
                    isPrecoFixo = true; //PREÇO FIXO ENCONTRADO RECEBE TRUE

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            preco_unidade = null;
            e.printStackTrace();
        }

        // PREÇO POR ROTA
        try {
            //myDataBaseSiac = this.getReadableDatabase();
            //
            Cursor cursor;
            String query = "SELECT (unp.preco_rota - ( " +
                    "           SELECT IFNULL( ( " +
                    "               SELECT mar.margem_cliente " +
                    "               FROM margens_clientes mar " +
                    "                  WHERE mar.produto_margem_cliente = unp.produto_preco_rota AND " +
                    "                        mar.codigo_cliente_margem_cliente = '" + id + "' " +
                    "               ), 0) " +
                    "                            ) " +
                    "       ) AS margem_cliente " +
                    "  FROM rotas_precos unp " +
                    " WHERE unp.rota_preco_rota = '" + rota + "' AND unp.preco_rota != '0.00' AND unp.produto_preco_rota = '" + produto + "'";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    preco_rota = cursor.getString(cursor.getColumnIndexOrThrow("margem_cliente"));
                    isPrecoFixo = true;

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            preco_rota = null;
            e.printStackTrace();
        }

        //
        //String[] sub = new String[]{preco_unidade, margem_cliente};
        //preco = cAux.maskMoney(new BigDecimal(String.valueOf(cAux.subitrair(sub))));

        if (preco_rota != null) {
            preco = cAux.maskMoney(new BigDecimal(preco_rota));
        } else if (preco_unidade != null) {
            preco = cAux.maskMoney(new BigDecimal(preco_unidade));
        } else {
            preco = "0,00";
        }

        Log.e("MARGEN ", preco);

        return preco;
    }

    /************* METODO PARA VERIFICAR PREÇO FIXO ****************/

    public boolean isPrecoFixo() {
        return isPrecoFixo;
    }

    public String getRotaCliente(String codigo_cliente) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT rota_cliente FROM rotas_clientes WHERE codigo_cliente = '" + codigo_cliente + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String rota = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                rota = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rota;
    }

    //
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return myDataBase.query(table, null, null, null, null, null, null);
    }

    // ** Enviar dados
    public String IdProduto(String produto) {

        String query = "SELECT pro.codigo_produto " +
                "FROM " + TABELA_PRODUTOS + " pro " +
                "WHERE pro.descricao_produto = '" + produto + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        StringBuilder str = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                str.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_produto")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();
        return str.toString();

    }

    // ** Enviar dados
    public String IdFormaPagamento(String fpg) {

        String query = "SELECT tfp.codigo_pagamento " +
                "FROM " + TABELA_FORMAS_PAGAMENTO + " tfp " +
                "WHERE tfp.descricao_forma_pagamento = '" + fpg + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        StringBuilder str = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                str.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();
        return str.toString();

    }
    /**************** ENVIAR DADOS MODIFICADO ******************/

    public String montarJson(String dataMovimento) {
        SQLiteDatabase db = null;
        Cursor cursorVendas = null;

        List<Map<String, Object>> pedidos = new ArrayList<>();

        try {
            db = this.getReadableDatabase(); // Abre o banco antes de começar
            String queryVendas = "SELECT * FROM vendas_app WHERE venda_finalizada_app = '1'";
            cursorVendas = db.rawQuery(queryVendas, null);

            if (cursorVendas.moveToFirst()) {
                do {
                    String codigoVenda = cursorVendas.getString(cursorVendas.getColumnIndexOrThrow("codigo_venda"));
                    String clienteId = cursorVendas.getString(cursorVendas.getColumnIndexOrThrow("codigo_cliente"));

                    if (db == null || !db.isOpen()) {
                        db = this.getReadableDatabase();
                    }

                    String queryProdutos = "SELECT pv.codigo_venda_app, p.codigo_produto, pv.quantidade, pv.preco_unitario, pv.entrega_futura " +
                            "FROM produtos_vendas_app pv " +
                            "INNER JOIN produtos p ON pv.produto = p.descricao_produto " +
                            "WHERE pv.codigo_venda_app = ?";
                    Cursor cursorProdutos = db.rawQuery(queryProdutos, new String[]{codigoVenda});

                    List<Map<String, Object>> vendas = new ArrayList<>();
                    if (cursorProdutos.moveToFirst()) {
                        do {
                            Map<String, Object> venda = new LinkedHashMap<>();

                            venda.put("PRODUTO", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("codigo_produto")));
                            venda.put("QUANTIDADE", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("quantidade")));
                            venda.put("VALOR_UNITARIO", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("preco_unitario")));
                            venda.put("ENTREGA_FUTURA", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("entrega_futura")));
                            vendas.add(venda);
                        } while (cursorProdutos.moveToNext());
                    }
                    cursorProdutos.close();

                    String queryFinanceiros = "SELECT * FROM " + TABELA_FINANCEIRO + " WHERE id_financeiro_app = ?";
                    Cursor cursorFinanceiros = db.rawQuery(queryFinanceiros, new String[]{codigoVenda});

                    List<Map<String, Object>> financeiros = new ArrayList<>();
                    if (cursorFinanceiros.moveToFirst()) {
                        do {
                            Map<String, Object> financeiro = new LinkedHashMap<>();
                            financeiro.put("FORMA_PAGAMENTO", IdFormaPagamento(cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("fpagamento_financeiro"))));
                            financeiro.put("VALOR", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("valor_financeiro")));
                            financeiro.put("VENCIMENTO", aux.exibirData(cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("vencimento_financeiro"))));
                            financeiro.put("DOCUMENTO", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("documento_financeiro")));
                            financeiro.put("NOTA_FISCAL", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("nota_fiscal")));
                            financeiro.put("COD_ALIQUOTA", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("codigo_aliquota")));
                            financeiros.add(financeiro);
                        } while (cursorFinanceiros.moveToNext());
                    }
                    cursorFinanceiros.close();

                    Map<String, Object> pedido = new LinkedHashMap<>();
                    pedido.put("CODIGO", codigoVenda);
                    pedido.put("CLIENTE", clienteId);
                    pedido.put("DATA", dataMovimento); // A data agora é recebida diretamente
                    pedido.put("VENDAS", vendas);
                    pedido.put("FINANCEIROS", financeiros);

                    pedidos.add(pedido);
                } while (cursorVendas.moveToNext());
            }
        } catch (Exception e) {
            Log.e("montarJson", "Erro ao montar JSON", e);
        } finally {
            if (cursorVendas != null && !cursorVendas.isClosed()) {
                cursorVendas.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        Map<String, Object> jsonFinal = new LinkedHashMap<>();
        jsonFinal.put("SERIAL", "005000002");
        jsonFinal.put("PEDIDOS", pedidos);

        return new Gson().toJson(jsonFinal);
    }


//    public String montarJson() {
//        SQLiteDatabase db = null;
//        Cursor cursorVendas = null;
//
//        List<Map<String, Object>> pedidos = new ArrayList<>();
//
//        try {
//            db = this.getReadableDatabase(); // Abre o banco antes de começar
//            // Consulta vendas finalizadas
//            String queryVendas = "SELECT * FROM " + TABELA_VENDAS + " WHERE venda_finalizada_app = '1'";
//            cursorVendas = db.rawQuery(queryVendas, null);
//
//            if (cursorVendas.moveToFirst()) {
//                do {
//                    // Captura os detalhes básicos da venda
//                    String codigoVenda = cursorVendas.getString(cursorVendas.getColumnIndexOrThrow("codigo_venda"));
//                    String clienteId = cursorVendas.getString(cursorVendas.getColumnIndexOrThrow("codigo_cliente"));
//                    String dataMovimento = cursorVendas.getString(cursorVendas.getColumnIndexOrThrow("data_movimento"));
//
//                    // Garante que o banco esteja aberto antes da consulta
//                    if (db == null || !db.isOpen()) {
//                        db = this.getReadableDatabase();
//                    }
//
//                    // Captura produtos associados à venda
//                    String queryProdutos = "SELECT * FROM produtos_vendas_app WHERE codigo_venda_app = ?";
//                    Cursor cursorProdutos = db.rawQuery(queryProdutos, new String[]{codigoVenda});
//
//                    List<Map<String, Object>> vendas = new ArrayList<>();
//                    if (cursorProdutos.moveToFirst()) {
//                        do {
//                            Map<String, Object> venda = new HashMap<>();
//                            venda.put("PRODUTO", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("produto")));
//                            venda.put("QUANTIDADE", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("quantidade")));
//                            venda.put("VALOR_UNITARIO", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("preco_unitario")));
//                            venda.put("ENTREGA_FUTURA", cursorProdutos.getString(cursorProdutos.getColumnIndexOrThrow("entrega_futura")));
//                            vendas.add(venda);
//                        } while (cursorProdutos.moveToNext());
//                    }
//                    cursorProdutos.close();
//
//                    // Captura financeiros associados à venda
//                    String queryFinanceiros = "SELECT * FROM " + TABELA_FINANCEIRO + " WHERE id_financeiro_app = ?";
//                    Cursor cursorFinanceiros = db.rawQuery(queryFinanceiros, new String[]{codigoVenda});
//
//                    List<Map<String, Object>> financeiros = new ArrayList<>();
//                    if (cursorFinanceiros.moveToFirst()) {
//                        do {
//                            Map<String, Object> financeiro = new HashMap<>();
//                            financeiro.put("FORMA_PAGAMENTO", IdFormaPagamento(cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("fpagamento_financeiro"))));
//                            financeiro.put("VALOR", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("valor_financeiro")));
//                            financeiro.put("VENCIMENTO", aux.exibirData(cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("vencimento_financeiro"))));
//                            financeiro.put("DOCUMENTO", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("documento_financeiro")));
//                            financeiro.put("NOTA_FISCAL", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("nota_fiscal")));
//                            financeiro.put("COD_ALIQUOTA", cursorFinanceiros.getString(cursorFinanceiros.getColumnIndexOrThrow("codigo_aliquota")));
//                            financeiros.add(financeiro);
//                        } while (cursorFinanceiros.moveToNext());
//                    }
//                    cursorFinanceiros.close();
//
//                    // Monta o pedido
//                    Map<String, Object> pedido = new HashMap<>();
//                    pedido.put("CODIGO", codigoVenda);
//                    pedido.put("CLIENTE", clienteId);
//                    pedido.put("DATA", dataMovimento);
//                    pedido.put("VENDAS", vendas);
//                    pedido.put("FINANCEIROS", financeiros);
//
//                    pedidos.add(pedido);
//                } while (cursorVendas.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.e("montarJson", "Erro ao montar JSON", e);
//        } finally {
//            // Fecha os recursos abertos
//            if (cursorVendas != null && !cursorVendas.isClosed()) {
//                cursorVendas.close();
//            }
//            if (db != null && db.isOpen()) {
//                db.close();
//            }
//        }
//
//        // Monta o JSON final
//        Map<String, Object> jsonFinal = new HashMap<>();
//        jsonFinal.put("SERIAL", "005000002");
//        jsonFinal.put("PEDIDOS", pedidos);
//
//        return new Gson().toJson(jsonFinal); // Converte o mapa para JSON
//    }

    /*************** ENVIAR DADOS ORIGINAL ****************/
    // ** Enviar dados VENDAS
    public String[] EnviarDados(String dataMovimento) {

        // **
        StringBuilder VENDAS = new StringBuilder();
        StringBuilder CLIENTES = new StringBuilder();
        StringBuilder PRODUTOS = new StringBuilder();
        StringBuilder QUANTIDADES = new StringBuilder();
        StringBuilder DATAS = new StringBuilder();
        StringBuilder VALORES = new StringBuilder();

        String query = "SELECT *, (ven.preco_unitario * 100) as valPreVen " +
                "FROM " + TABELA_VENDAS + " ven " +
                "WHERE ven.venda_finalizada_app = '1'";

        //Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                // **
                VENDAS.append(",");
                VENDAS.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda")));

                // **
                CLIENTES.append(",");
                CLIENTES.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente")));

                // **
                PRODUTOS.append(",");
                PRODUTOS.append(IdProduto(cursor.getString(cursor.getColumnIndexOrThrow("produto_venda"))));

                // **
                QUANTIDADES.append(",");
                QUANTIDADES.append(cursor.getString(cursor.getColumnIndexOrThrow("quantidade_venda")));

                // ** aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("data_movimento")))
                DATAS.append(",");
                DATAS.append(dataMovimento);

                // **
                VALORES.append(",");
                /*String pre_unit = "";
                //aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario")));
                String[] valMlt = {aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario"))), "100"};
                String valUnit = String.valueOf(aux.multiplicar(valMlt));*/
                /*if (valUnit.length() < 4) {
                    pre_unit = aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario"))) + "00";
                } else {
                    pre_unit = aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("preco_unitario")));
                }*/
                VALORES.append(cursor.getString(cursor.getColumnIndexOrThrow("valPreVen")));
                Log.i(TAG, " Peço unit." + cursor.getString(cursor.getColumnIndexOrThrow("valPreVen")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();

        String[] ret = {
                VENDAS.toString(),
                CLIENTES.toString(),
                PRODUTOS.toString(),
                QUANTIDADES.toString(),
                DATAS.toString(),
                VALORES.toString()
        };

        return ret;
    }

    // ** Enviar dados VENDAS
    public String[] EnviarDadosFinanceiro() {
        // **
        /*StringBuilder VENDAS = new StringBuilder();
        StringBuilder CLIENTES = new StringBuilder();
        StringBuilder PRODUTOS = new StringBuilder();
        StringBuilder QUANTIDADES = new StringBuilder();
        StringBuilder DATAS = new StringBuilder();
        StringBuilder VALORES = new StringBuilder();*/
        StringBuilder FINANCEIROS = new StringBuilder();
        StringBuilder FINVEN = new StringBuilder();
        StringBuilder VENCIMENTOS = new StringBuilder();
        StringBuilder VALORESFIN = new StringBuilder();
        StringBuilder FPAGAMENTOS = new StringBuilder();
        StringBuilder DOCUMENTOS = new StringBuilder();
        StringBuilder NOTASFISCAIS = new StringBuilder();
        StringBuilder CODALIQUOTAS = new StringBuilder();

        String query = "SELECT *, (fin.valor_financeiro * 100) as valFin " +
                "FROM " + TABELA_VENDAS + " ven " +
                "INNER JOIN " + TABELA_FINANCEIRO + " fin ON fin.id_financeiro_app = ven.codigo_venda_app " +
                "WHERE ven.venda_finalizada_app = '1'";

        //Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                // ---------------------------------- ** FINANCEIRO

                // **
                FINANCEIROS.append(",");
                FINANCEIROS.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_financeiro")));

                // **
                FINVEN.append(",");
                FINVEN.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda")));

                // **
                VENCIMENTOS.append(",");
                VENCIMENTOS.append(aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("vencimento_financeiro"))));

                // **
                VALORESFIN.append(",");
                /*String val_fin = "";
                if (aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"))).length() < 4) {
                    val_fin = aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"))) + "00";
                } else {
                    val_fin = aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro")));
                }*/
                //{aux.soNumeros()
                //String[] valMlt = {"100", cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"))};
                //String val_fin = String.valueOf(aux.multiplicar(valMlt));

                VALORESFIN.append(cursor.getString(cursor.getColumnIndexOrThrow("valFin")));
                Log.i(TAG, " Valor Fin." + cursor.getString(cursor.getColumnIndexOrThrow("valFin")));
                //VALORESFIN.append(aux.soNumeros(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro"))));

                // **
                FPAGAMENTOS.append(",");
                FPAGAMENTOS.append(IdFormaPagamento(cursor.getString(cursor.getColumnIndexOrThrow("fpagamento_financeiro"))));

                // **
                DOCUMENTOS.append(",");
                DOCUMENTOS.append(cursor.getString(cursor.getColumnIndexOrThrow("documento_financeiro")));

                // **
                NOTASFISCAIS.append(",");
                NOTASFISCAIS.append(cursor.getString(cursor.getColumnIndexOrThrow("nota_fiscal")));

                // **
                CODALIQUOTAS.append(",");
                CODALIQUOTAS.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_aliquota")));

            } while (cursor.moveToNext());
        }

        //myDataBase.close();

        /*String[] ret = {
                VENDAS.toString(),
                CLIENTES.toString(),
                PRODUTOS.toString(),
                QUANTIDADES.toString(),
                DATAS.toString(),
                VALORES.toString(),
                FINANCEIROS.toString(),
                FINVEN.toString(),
                VENCIMENTOS.toString(),
                VALORESFIN.toString(),
                FPAGAMENTOS.toString(),
                DOCUMENTOS.toString()
        };*/

        String[] ret = {
                FINANCEIROS.toString(),
                FINVEN.toString(),
                VENCIMENTOS.toString(),
                VALORESFIN.toString(),
                FPAGAMENTOS.toString(),
                DOCUMENTOS.toString(),
                NOTASFISCAIS.toString(),
                CODALIQUOTAS.toString()
        };
        for (String s : ret) {
            Log.i("EnviarDadosFinanceiro", s);
        }


        return ret;
    }

    /**************ENVIAR DADOS ALTERAÇOES **********************/
    public String[] EnviarDadosProdutos() {


        StringBuilder PRODUTOS = new StringBuilder();
        StringBuilder QUANTIDADES = new StringBuilder();
        StringBuilder ID_DA_VENDA = new StringBuilder();

        String query = "SELECT pva.produto, pva.quantidade, pva.codigo_venda_app " +
                "FROM " + TABELA_VENDAS + " ven " +
                "INNER JOIN produtos_vendas_app pva   ON pva.codigo_venda_app = ven.codigo_venda_app " +
                "WHERE ven.venda_finalizada_app = '1'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PRODUTOS.append(",");    PRODUTOS.append(cursor.getString(cursor.getColumnIndexOrThrow("produto")));

                QUANTIDADES.append(",");   QUANTIDADES.append(cursor.getString(cursor.getColumnIndexOrThrow("quantidade")));

                ID_DA_VENDA.append(",");    ID_DA_VENDA.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda_app")));
            } while (cursor.moveToNext());
        }

        cursor.close();
       // db.close();

        String[] ret = {

                PRODUTOS.toString(),
                QUANTIDADES.toString(),
                ID_DA_VENDA.toString()

        };

        for (String s : ret) {
            Log.i("EnviarDadosPRODUTOS", s);
        }

        return ret;
    }


    // Método para consultar o limite de crédito de um cliente específico
    public double getLimiteCreditoCliente(int clienteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + LIMITE_CREDITO_CLIENTE + " FROM " + TABELA_CLIENTES + " WHERE " + CODIGO_CLIENTE + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(clienteId)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            cursor.moveToFirst();
            @SuppressLint("Range") double limiteCredito = cursor.getDouble(cursor.getColumnIndex(LIMITE_CREDITO_CLIENTE));
            cursor.close();
            return limiteCredito;
        } else {
            return 0.0;
        }
    }


    // ** Só para atualizar sem dar problemas, retiar rdepois pra não encher o código
    public String[] EnviarDadosFinanceiroTemp() {

        // **
        StringBuilder FINANCEIROS = new StringBuilder();
        StringBuilder FINVEN = new StringBuilder();
        StringBuilder VENCIMENTOS = new StringBuilder();
        StringBuilder VALORESFIN = new StringBuilder();
        StringBuilder FPAGAMENTOS = new StringBuilder();
        StringBuilder DOCUMENTOS = new StringBuilder();
        StringBuilder NOTASFISCAIS = new StringBuilder();
        StringBuilder CODALIQUOTAS = new StringBuilder();

        String query = "SELECT *, (fin.valor_financeiro * 100) as valFin " +
                "FROM " + TABELA_VENDAS + " ven " +
                "INNER JOIN " + TABELA_FINANCEIRO + " fin ON fin.id_financeiro_app = ven.codigo_venda_app " +
                "WHERE ven.venda_finalizada_app = '1'";

        //Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                // ---------------------------------- ** FINANCEIRO

                // **
                FINANCEIROS.append(",");
                FINANCEIROS.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_financeiro")));

                // **
                FINVEN.append(",");
                FINVEN.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_venda")));

                // **
                VENCIMENTOS.append(",");
                VENCIMENTOS.append(aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("vencimento_financeiro"))));

                // **
                VALORESFIN.append(",");

                VALORESFIN.append(cursor.getString(cursor.getColumnIndexOrThrow("valFin")));
                Log.i(TAG, " Valor Fin." + cursor.getString(cursor.getColumnIndexOrThrow("valFin")));

                // **
                FPAGAMENTOS.append(",");
                FPAGAMENTOS.append(IdFormaPagamento(cursor.getString(cursor.getColumnIndexOrThrow("fpagamento_financeiro"))));

                // **
                DOCUMENTOS.append(",");
                DOCUMENTOS.append(cursor.getString(cursor.getColumnIndexOrThrow("documento_financeiro")));

                // **
                NOTASFISCAIS.append(",");
                NOTASFISCAIS.append(cursor.getString(cursor.getColumnIndexOrThrow("nota_fiscal")));

                // **
                CODALIQUOTAS.append(",");
                CODALIQUOTAS.append("");

            } while (cursor.moveToNext());
        }

        String[] ret = {
                FINANCEIROS.toString(),
                FINVEN.toString(),
                VENCIMENTOS.toString(),
                VALORESFIN.toString(),
                FPAGAMENTOS.toString(),
                DOCUMENTOS.toString(),
                NOTASFISCAIS.toString(),
                CODALIQUOTAS.toString()
        };
        for (String s : ret) {
            Log.i("EnviarDadosFinanceiro", s);
        }


        return ret;
    }

    // ** Enviar dados CONTAS A RECEBER
    public String[] EnviarDadosContasReceber() {
        // **
        StringBuilder codigo_financeiro = new StringBuilder();
        StringBuilder unidade_financeiro = new StringBuilder();
        StringBuilder data_financeiro = new StringBuilder();
        StringBuilder codigo_cliente_financeiro = new StringBuilder();
        StringBuilder fpagamento_financeiro = new StringBuilder();
        StringBuilder documento_financeiro = new StringBuilder();
        StringBuilder vencimento_financeiro = new StringBuilder();
        StringBuilder valor_financeiro = new StringBuilder();
        StringBuilder status_autorizacao = new StringBuilder();
        StringBuilder pago = new StringBuilder();
        StringBuilder vasilhame_ref = new StringBuilder();
        StringBuilder usuario_atual = new StringBuilder();
        StringBuilder data_inclusao = new StringBuilder();
        StringBuilder nosso_numero_financeiro = new StringBuilder();
        StringBuilder id_vendedor_financeiro = new StringBuilder();

        String query = "SELECT *, (pago * 100) as valPago  FROM recebidos";

        //Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                // **
                codigo_financeiro.append(",");
                codigo_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_financeiro")));
                // **
                unidade_financeiro.append(",");
                unidade_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("unidade_financeiro")));
                // **
                data_financeiro.append(",");
                data_financeiro.append(aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("data_financeiro"))));
                // **
                codigo_cliente_financeiro.append(",");
                codigo_cliente_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente_financeiro")));
                // **
                fpagamento_financeiro.append(",");
                fpagamento_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("fpagamento_financeiro")));
                // **
                documento_financeiro.append(",");
                documento_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("documento_financeiro")));
                // **
                vencimento_financeiro.append(",");
                vencimento_financeiro.append(aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("vencimento_financeiro"))));
                // **
                valor_financeiro.append(",");
                valor_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("valor_financeiro")));
                // **
                status_autorizacao.append(",");
                status_autorizacao.append(cursor.getString(cursor.getColumnIndexOrThrow("status_autorizacao")));
                // **
                pago.append(",");
                pago.append(cursor.getString(cursor.getColumnIndexOrThrow("valPago")));
                // **
                vasilhame_ref.append(",");
                vasilhame_ref.append(cursor.getString(cursor.getColumnIndexOrThrow("vasilhame_ref")));
                // **
                usuario_atual.append(",");
                usuario_atual.append(cursor.getString(cursor.getColumnIndexOrThrow("usuario_atual")));
                // **
                data_inclusao.append(",");
                data_inclusao.append(aux.exibirData(cursor.getString(cursor.getColumnIndexOrThrow("data_inclusao"))));
                // **
                nosso_numero_financeiro.append(",");
                nosso_numero_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("nosso_numero_financeiro")));
                // **
                id_vendedor_financeiro.append(",");
                id_vendedor_financeiro.append(cursor.getString(cursor.getColumnIndexOrThrow("id_vendedor_financeiro")));


                //Log.i(TAG + " Peço unit.", cursor.getString(cursor.getColumnIndexOrThrow("valPreVen")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();

        String[] ret = {
                codigo_financeiro.toString(),
                unidade_financeiro.toString(),
                data_financeiro.toString(),
                codigo_cliente_financeiro.toString(),
                fpagamento_financeiro.toString(),
                documento_financeiro.toString(),
                vencimento_financeiro.toString(),
                valor_financeiro.toString(),
                status_autorizacao.toString(),
                pago.toString(),
                vasilhame_ref.toString(),
                usuario_atual.toString(),
                data_inclusao.toString(),
                nosso_numero_financeiro.toString(),
                id_vendedor_financeiro.toString()
        };

        return ret;
    }

    // ** Enviar dados VALES
    public String[] EnviarDadosVales(String dataMov) {

        // **
        StringBuilder CODVALES = new StringBuilder();
        StringBuilder UNIDADES = new StringBuilder();
        StringBuilder NUMEROS = new StringBuilder();
        StringBuilder DATAMOV = new StringBuilder();
        StringBuilder CLIVALES = new StringBuilder();

        String query = "SELECT * " +
                "FROM  vale  val " +
                "WHERE val.situacao_vale = 'UTILIZADO'";

        //Log.e("SQL = ", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                // **
                CODVALES.append(",");
                CODVALES.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_vale")));

                // **
                UNIDADES.append(",");
                UNIDADES.append(cursor.getString(cursor.getColumnIndexOrThrow("unidade_vale")));

                // **
                NUMEROS.append(",");
                NUMEROS.append(cursor.getString(cursor.getColumnIndexOrThrow("numero_vale")));

                // **
                DATAMOV.append(",");
                DATAMOV.append(dataMov);

                // **
                CLIVALES.append(",");
                CLIVALES.append(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente_vale")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();

        String[] ret = {
                CODVALES.toString(),
                UNIDADES.toString(),
                NUMEROS.toString(),
                DATAMOV.toString(),
                CLIVALES.toString()
        };

        return ret;
    }

    // ** Enviar dados
    public int DiasPrazoCliente(String fpg, String cod) {
        int result = 0;

        String query = "SELECT fpc.pagamento_prazo_cliente FROM formas_pagamento_cliente fpc WHERE fpc.pagamento_cliente = '" + fpg + "' AND fpc.cliente_pagamento = '" + cod + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                result = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("pagamento_prazo_cliente")));
            } while (cursor.moveToNext());
        }

        myDataBase.close();
        return result;
    }

    //CURSOR POS
    private PosApp cursorToPos(Cursor cursor) {
        PosApp posApp = new PosApp(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        //
        posApp.setCodigo(cursor.getString(0));
        posApp.setSerial(cursor.getString(1));
        posApp.setUnidade(cursor.getString(2));
        posApp.setSerie(cursor.getString(3));
        posApp.setUltnfce(cursor.getString(4));
        posApp.setUltboleto(cursor.getString(5));
        posApp.setNota_remessa(cursor.getString(6));
        posApp.setSerie_remessa(cursor.getString(7));
        posApp.setLimite_credito(cursor.getString(8));
        posApp.setUltpromissoria(cursor.getString(9));
        posApp.setAutovencimento(cursor.getString(10));
        posApp.setModulo_pedidos(cursor.getString(11));
        posApp.setBaixa_a_prazo(cursor.getString(12));
        posApp.setSerie_boleto(cursor.getString(13));
        try {
            posApp.setEscolher_cliente_vale(cursor.getString(14));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posApp;
    }

    //LISTAR TODAS AS UNIDADES
    public PosApp getPos() {
        PosApp posApp = new PosApp(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        String query = "SELECT * FROM pos LIMIT 1";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                posApp = cursorToPos(cursor);
            } while (cursor.moveToNext());
        }

        myDataBase.close();
        return posApp;
    }

    private ContasBancarias cursorContasBancarias(Cursor cursor) {
        ContasBancarias conta = new ContasBancarias(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        conta.setCodigo(cursor.getString(0));
        conta.setBanco_conta(cursor.getString(1));
        conta.setAgencia(cursor.getString(2));
        conta.setConta(cursor.getString(3));
        conta.setDv_conta(cursor.getString(4));
        conta.setConvenio(cursor.getString(5));
        conta.setContrato(cursor.getString(6));
        conta.setCarteira(cursor.getString(7));
        conta.setVariacao(cursor.getString(8));
        conta.setConta_cedente(cursor.getString(9));
        conta.setDv_conta_cedente(cursor.getString(10));
        conta.setCedente(cursor.getString(11));
        conta.setCpf_cnpj(cursor.getString(12));
        conta.setEndereco(cursor.getString(13));
        conta.setCidade_uf(cursor.getString(14));
        conta.setInstrucoes(cursor.getString(15));
        conta.setInicio_nosso_numero(cursor.getString(16));
        conta.setDv_agencia(cursor.getString(17));
        conta.setTaxa_boleto(cursor.getString(18));

        return conta;
    }

    // LISTA TODAS AS CONTAS BANCARIA
    public ContasBancarias contasBancarias() {

        ContasBancarias contasBancarias = null;
        String query = "SELECT * FROM contas_bancarias ORDER BY codigo DESC LIMIT 1";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                contasBancarias = cursorContasBancarias(cursor);
            } while (cursor.moveToNext());
        }

        return contasBancarias;
    }

    //
    public ContasBancarias ContaBancaria(String formaPG) {

        ContasBancarias contasBancarias = null;
        String query = "SELECT * FROM contas_bancarias cob WHERE cob.codigo = '" + formaPG + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                contasBancarias = cursorContasBancarias(cursor);
            } while (cursor.moveToNext());
        }

        return contasBancarias;
    }

    //CONSULTAR CLIENTE
    public Clientes cliente(String codigo) {
        Clientes cliente = null;
        String query = "SELECT * FROM clientes WHERE codigo_cliente = " + codigo + " LIMIT 1";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                cliente = cursorToCliente(cursor);
            } while (cursor.moveToNext());
        }

        return cliente;
    }

    //
    public String getContaBancariaFormaPagamento(String descFormaPagamento) {

        myDataBase = this.getReadableDatabase();

        String selectQuery = "SELECT conta_bancaria FROM formas_pagamento WHERE descricao_forma_pagamento = '" + descFormaPagamento + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String str = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                str = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    //
    public String getCodContaBancaria(String contaBancFormaPagamento) {

        myDataBase = this.getReadableDatabase();

        String selectQuery = "SELECT banco_conta FROM contas_bancarias WHERE codigo = '" + contaBancFormaPagamento + "' LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String str = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                str = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }


    //CURSOR PEDIDOS
    private UnidadesDomain cursorToUnidade(Cursor cursor) {
        UnidadesDomain unidades = new UnidadesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        unidades.setId_unidade(cursor.getString(0));
        unidades.setDescricao_unidade(cursor.getString(1));
        unidades.setRazao_social(cursor.getString(2));
        unidades.setCnpj(cursor.getString(3));
        unidades.setEndereco(cursor.getString(4));
        unidades.setNumero(cursor.getString(5));
        unidades.setBairro(cursor.getString(6));
        unidades.setCep(cursor.getString(7));
        unidades.setTelefone(cursor.getString(8));
        unidades.setIe(cursor.getString(9));
        unidades.setCidade(cursor.getString(10));
        unidades.setUf(cursor.getString(11));
        unidades.setCodigo_ibge(cursor.getString(12));
        unidades.setUrl_consulta(cursor.getString(13));
        return unidades;
    }

    //LISTAR TODAS AS UNIDADES
    public ArrayList<UnidadesDomain> getUnidades() {
        ArrayList<UnidadesDomain> listaUnidades = new ArrayList<>();

        String query = "SELECT * FROM unidades";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                UnidadesDomain unidades = cursorToUnidade(cursor);
                listaUnidades.add(unidades);
            } while (cursor.moveToNext());
        }

        return listaUnidades;
    }

    //
    private ValesDomain cursorToValesDomain(Cursor cursor) {
        ValesDomain valesDomain = new ValesDomain(null, null, null, null, null, null, null);

        valesDomain.setCodigo_vale(cursor.getString(0));
        valesDomain.setUnidade_vale(cursor.getString(1));
        valesDomain.setCodigo_cliente_vale(cursor.getString(2));
        valesDomain.setNumero_vale(cursor.getString(3));
        valesDomain.setValor_vale(cursor.getString(4));
        valesDomain.setSituacao_vale(cursor.getString(5));
        valesDomain.setProduto_vale(cursor.getString(6));

        return valesDomain;
    }

    // CONSULTA VALE PRODUTO
    public ValesDomain ConsVale(String codigo_vale) {
        ValesDomain vale = null;

        String query = "SELECT * " +
                "FROM vale v " +
                "WHERE v.numero_vale = '" + codigo_vale + "' " +
                "LIMIT 1";

        Log.e("SQL", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                vale = cursorToValesDomain(cursor);
            } while (cursor.moveToNext());
        }

        return vale;
    }

    public int UsarVale(String nVale, String codCli) {
        myDataBase = this.getWritableDatabase();

        //
        ContentValues values = new ContentValues();
        values.put("situacao_vale", "UTILIZADO");
        values.put("codigo_cliente_vale", codCli);
        int a = 0;

        try {
            a = myDataBase.update(
                    "vale",
                    values,
                    "numero_vale" + " = ?",
                    new String[]{String.valueOf(nVale)}
            );
        } catch (Exception ignored) {

        }
        return a;
    }

    public String getIdFPG(String fpg) {
        String id = "";
        myDataBase = this.getReadableDatabase();

        //
        String selectQuery = "SELECT fpg.codigo_pagamento " +
                "FROM formas_pagamento fpg " +
                "WHERE fpg.descricao_forma_pagamento = '" + fpg + "' " +
                "LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    id = cursor.getString(cursor.getColumnIndexOrThrow("codigo_pagamento"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public ArrayList<String> getBandeiraFPg(String fpg) {
        ArrayList<String> list = new ArrayList<>();
        String codFPG = this.getIdFPG(fpg);
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT caa.bandeira " +
                "FROM cartoes_aliquotas caa " +
                "WHERE caa.codigo_forma_pagamento = '" + codFPG + "' " +
                "GROUP BY caa.bandeira";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String bandeira = aux.getBandeira(cursor.getString(cursor.getColumnIndexOrThrow("bandeira")));
                    list.add(bandeira);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getPrazoFPg(String bandeira, String fpg) {
        ArrayList<String> list = new ArrayList<>();
        String codFPG = this.getIdFPG(fpg);
        String codband = aux.getIdBandeira(bandeira); // this.getIdPrazoFPG(fpg);
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT caa.parcela " +
                "FROM cartoes_aliquotas caa " +
                "WHERE caa.bandeira = '" + codband + "' AND caa.codigo_forma_pagamento = '" + codFPG + "'";

        Log.e("PrazosFPG", selectQuery);
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String prazo = cursor.getString(cursor.getColumnIndexOrThrow("parcela"));
                    list.add(prazo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getIdAliquota(String id_fpg, String bandeira, String parcela) {
        String id = "";
        myDataBase = this.getReadableDatabase();

        //
        String selectQuery = "SELECT caa.codigo " +
                "FROM cartoes_aliquotas caa " +
                "WHERE caa.codigo_forma_pagamento = '" + id_fpg + "' AND caa.bandeira = '" + bandeira + "' and caa.parcela = '" + parcela + "' LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    id = cursor.getString(cursor.getColumnIndexOrThrow("codigo"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public String getCartaoTrue(String fpg) {
        String id = "";
        myDataBase = this.getReadableDatabase();
        try {
            //
            String selectQuery = "SELECT fpg.cartao " +
                    "FROM formas_pagamento fpg " +
                    "WHERE fpg.descricao_forma_pagamento = '" + fpg + "' LIMIT 1";
            Cursor cursor = myDataBase.rawQuery(selectQuery, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    id = cursor.getString(cursor.getColumnIndexOrThrow("cartao"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Financeiro", Objects.requireNonNull(e.getMessage()));
            Intent i = new Intent(context, AtualizacaoApp.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
            //context.startActivity(new Intent(context, AtualizacaoApp.class));
        }
        return id;
    }

    // INSERTS DADOS TABELAS
    public void insetDataBase(String sql) {
        myDataBase = this.getWritableDatabase();
        try {
            myDataBase.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertClientes(Clientes cli) {
        ContentValues cv = new ContentValues();
        cv.put("codigo_cliente", cli.getCodigo_cliente());
        cv.put("nome_cliente", cli.getNome_cliente());
        cv.put("latitude_cliente", cli.getLatitude_cliente());
        cv.put("longitude_cliente", cli.getLongitude_cliente());
        cv.put("saldo", cli.getSaldo());
        cv.put("cpfcnpj", cli.getCpfcnpj());
        cv.put("endereco", cli.getEndereco());
        cv.put("apelido_cliente", cli.getApelido_cliente());

        myDataBase = this.getWritableDatabase();
        myDataBase.insertOrThrow("clientes", null, cv);
    }

    //
    public void LimparDadosBanco() {
        myDataBase = this.getWritableDatabase();
        myDataBase.delete("vendas_app", null, null);
        myDataBase.delete("recebidos", null, null);
        myDataBase.delete("financeiro", null, null);
    }

    public void FecharConexao() {
        myDataBase.close();
        SQLiteDatabase db = this.getReadableDatabase();
        db.close();
    }
}
