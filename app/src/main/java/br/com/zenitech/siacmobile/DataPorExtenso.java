package br.com.zenitech.siacmobile;

import static br.com.zenitech.siacmobile.NumeroPorExtenso.valorPorExtenso;

public class DataPorExtenso {

    public static String dataPorExtenso(String data) {
        String[] dataF;

        // Tenta dividir pelo formato "dd/MM/yyyy"
        if (data.contains("/")) {
            dataF = data.split("/");
        }
        // Caso contrário, assume que está no formato "yyyy-MM-dd"
        else if (data.contains("-")) {
            dataF = data.split("-");
            // Reorganiza para "dd/MM/yyyy"
            dataF = new String[] {dataF[2], dataF[1], dataF[0]}; // "dia", "mes", "ano"
        } else {
            throw new IllegalArgumentException("Formato de data inválido. Use 'dd/MM/yyyy' ou 'yyyy-MM-dd'");
        }

        String[] dia = {"zero", "um", "dois", "tres", "quatro", "cinco", "seis", "sete", "oito", "nove",
                "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito",
                "dezenove", "vinte", "vinte e um", "vinte e dois", "vinte e três", "vinte e quatro",
                "vinte e cinco", "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove", "trinta", "trinta e um"};

        String[] mes = {"", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        //
        String diaF = dia[Integer.parseInt(dataF[0])];
        String mesF = mes[Integer.parseInt(dataF[1])];
        String anoF = valorPorExtenso(Double.parseDouble(dataF[2]));

        return diaF + " de " + mesF + " de " + anoF.replace("real", "").replace("reais", "");
    }
}
