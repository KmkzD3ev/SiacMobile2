package br.com.zenitech.siacmobile

import android.content.Context
import android.content.SharedPreferences

/**
 * BANCO LOCAL PARA ANALISE DE CREDITO
 *
 * SALVA AS FORMAS DE PAGAMENTO A PRAZO PARA SOMAR OS VALORES E COMPARAR AO LIMITE
 *
 *
 */
class CreditoPrefs
    (context: Context){
     private val prefs: SharedPreferences = context.getSharedPreferences("financeiro_prefs",Context.MODE_PRIVATE)


    var formaPagamentoPrazo: String?
        get() = prefs.getString("formaPagamentoPrazo", "")
        set(value) = prefs.edit().putString("formaPagamentoPrazo", value).apply()


    var ValorAprazo : String?
        get() = prefs.getString("ValorAprazo","")
        set(value)= prefs.edit().putString("ValoresAprazo", value).apply()


    var IdCliente : String?
        get() = prefs.getString("IdCliente", "")
        set(value) = prefs.edit().putString("IdCliente", value).apply()


}