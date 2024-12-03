package br.com.zenitech.siacmobile.interfaces;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import br.com.zenitech.siacmobile.Configuracoes;
import br.com.zenitech.siacmobile.CreditoPrefs;
import br.com.zenitech.siacmobile.domains.Conta;
import br.com.zenitech.siacmobile.domains.EnviarDados;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IEnviarDados {

    //LOGIN DA CONTA DO USUÁRIO
    /*
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
        * */
    @FormUrlEncoded
    @POST("index_app_siac.php")
    Call<ArrayList<EnviarDados>> enviarDados(
            @Field("TELA") String TELA,
            @Field("SERIAL") String SERIAL,
            @Field("VENDAS") String VENDAS,
            @Field("CLIENTES") String CLIENTES,
            @Field("PRODUTOS") String PRODUTOS,
            @Field("QUANTIDADES") String QUANTIDADES,
            @Field("DATAS") String DATAS,
            @Field("VALORES") String VALORES,
            @Field("FINANCEIROS") String FINANCEIROS,
            @Field("FINVEN") String FINVEN,
            @Field("VENCIMENTOS") String VENCIMENTOS,
            @Field("VALORESFIN") String VALORESFIN,
            @Field("FPAGAMENTOS") String FPAGAMENTOS,
            @Field("DOCUMENTOS") String DOCUMENTOS,
            @Field("NOTASFISCAIS") String NOTASFISCAIS,
            @Field("CODALIQUOTAS") String CODALIQUOTAS,
            @Field("ENTFUTURA") String ENTFUTURA  // Novo campo adicionado para entrega futura

    );
//
//    @FormUrlEncoded
//    @POST("index_app_siac.php")
//    Call<ArrayList<EnviarDados>> enviarDadosProdutos(
//            @Field("TELA") String TELA,
//            @Field("SERIAL") String SERIAL,
//            @Field("IDDAVENDA") String IDDAVENDA,
//            @Field("PRODUTOS") String PRODUTOS,
//            @Field("QUANTIDADES") String QUANTIDADES
//    );


    @POST("http://191.243.197.5/POSSIACN/index_app_siac_v2.php?TELA=850&SERIAL=005000002")
    Call<ArrayList<EnviarDados>> enviarDadosUnificado(
            @Query("TELA") String TELA, // Passado como query parameter
            @Query("SERIAL") String SERIAL, // Passado como query parameter
            @Body JsonObject PEDIDOS  // Corpo JSON
    );


    @FormUrlEncoded
    @POST("index_app_siac.php")
    Call<ArrayList<EnviarDados>> enviarDadosContasReceber(
            @Field("TELA") String TELA,
            @Field("SERIAL") String SERIAL,
            @Field("codigo_financeiro") String codigo_financeiro,
            @Field("unidade_financeiro") String unidade_financeiro,
            @Field("data_financeiro") String data_financeiro,
            @Field("codigo_cliente_financeiro") String codigo_cliente_financeiro,
            @Field("fpagamento_financeiro") String fpagamento_financeiro,
            @Field("documento_financeiro") String documento_financeiro,
            @Field("vencimento_financeiro") String vencimento_financeiro,
            @Field("valor_financeiro") String valor_financeiro,
            @Field("status_autorizacao") String status_autorizacao,
            @Field("pago") String pago,
            @Field("usuario_atual") String usuario_atual,
            @Field("data_inclusao") String data_inclusao,
            @Field("nosso_numero_financeiro") String nosso_numero_financeiro,
            @Field("id_vendedor_financeiro") String id_vendedor_financeiro
    );

    @FormUrlEncoded
    @POST("index_app_siac.php")
    Call<ArrayList<EnviarDados>> enviarDadosVales(
            @Field("TELA") String TELA,
            @Field("SERIAL") String SERIAL,
            @Field("CODIGOS") String CODIGOS,
            @Field("VALES") String VALES,
            @Field("DATAS") String DATAS,
            @Field("CLIENTES") String CLIENTES
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIACN/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
