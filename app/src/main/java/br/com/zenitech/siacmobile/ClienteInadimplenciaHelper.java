package br.com.zenitech.siacmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import br.com.zenitech.siacmobile.domains.FinanceiroReceberClientes;

public class ClienteInadimplenciaHelper {

    private SharedPreferences prefs;

    public ClienteInadimplenciaHelper(Context context) {
        prefs = context.getSharedPreferences("InadimplenciaClientes", Context.MODE_PRIVATE);
    }

    // Salvar o status de inadimplência
    public void salvarStatusInadimplencia(String idCliente, boolean isInadimplente) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(idCliente, isInadimplente);
        editor.apply();
        Log.d("SALVANDO INADINPLENTE", "Status de inadimplência atualizado para o cliente " + idCliente + ": " + isInadimplente);
    }

    // Verificar se o cliente está inadimplente
    public boolean verificarInadimplencia(String idCliente) {
        return prefs.getBoolean(idCliente, false);
    }
}


























