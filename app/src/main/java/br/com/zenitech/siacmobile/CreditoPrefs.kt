package br.com.zenitech.siacmobile

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Classe para armazenar temporariamente informações sobre vendas a prazo,
 * usada para verificação de crédito e controle de cancelamento de vendas a prazo.
 */
class CreditoPrefs(context: Context) {
    private val prefs: SharedPreferences
    private val gson: Gson = Gson() // Inicializando o Gson para serializar e deserializar JSON

    init {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var formaPagamentoPrazo: String?
        // Armazena e obtém a forma de pagamento a prazo
        get() = prefs.getString("formaPagamentoPrazo", "")
        set(formaPagamentoPrazo) {
            prefs.edit().putString("formaPagamentoPrazo", formaPagamentoPrazo).apply()
        }

    var valorAprazo: String?
        // Armazena e obtém o valor total a prazo
        get() = prefs.getString("ValorAprazo", "")
        set(valorAprazo) {
            prefs.edit().putString("ValorAprazo", valorAprazo).apply()
        }

    var idCliente: String?
        // Armazena e obtém o ID do cliente associado à venda a prazo
        get() = prefs.getString("IdCliente", "")
        set(idCliente) {
            prefs.edit().putString("IdCliente", idCliente).apply()
        }


    // Método para salvar o array estadosEntregaFutura
    fun salvarEstadosEntregaFutura(estadosEntregaFutura: ArrayList<Int>) {
        val json = gson.toJson(estadosEntregaFutura) // Converter ArrayList para JSON
        prefs.edit().putString("estadosEntregaFutura", json).apply()
    }

    // Método para recuperar o array estadosEntregaFutura
    fun recuperarEstadosEntregaFutura(): ArrayList<Int> {
        val json = prefs.getString("estadosEntregaFutura", "")
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            gson.fromJson(json, type) // Converter de volta para ArrayList
        } else {
            ArrayList() // Retorna lista vazia se não houver nada salvo
        }
    }



    // Limpa todas as informações armazenadas (quando a venda for cancelada ou concluída)
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        // Nome do arquivo de SharedPreferences
        private const val PREF_NAME = "financeiro_prefs"
    }
}