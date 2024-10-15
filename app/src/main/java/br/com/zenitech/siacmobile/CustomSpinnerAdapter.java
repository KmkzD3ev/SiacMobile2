package br.com.zenitech.siacmobile;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    public CustomSpinnerAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Chama o método original para obter o layout do item
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        // Obter o texto completo e formatá-lo
        String fullText = getItem(position);
        String formattedText = formatarFormaPagamento(fullText);

        // Log para verificar a transformação da string
        Log.d("CustomSpinnerAdapter", "Original: " + fullText + " | Formatado: " + formattedText);

        textView.setText(formattedText);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Chama o método original para obter o layout da dropdown
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        // Obter o texto completo e formatá-lo
        String fullText = getItem(position);
        String formattedText = formatarFormaPagamento(fullText);

        // Log para verificar a transformação da string
        Log.d("CustomSpinnerAdapter", "Original: " + fullText + " | Formatado: " + formattedText);

        textView.setText(formattedText);
        return view;
    }

    // Método que formata a string para exibir apenas a primeira parte antes do "_"
    private String formatarFormaPagamento(String fullText) {
        if (fullText != null) {
            // Retorna a primeira parte da string antes do primeiro "_"
            String[] partes = fullText.split(" _ ");
            return partes[0]; // Exibe apenas "DINHEIRO", "PIXAV1", etc.
        }
        return fullText;
    }
}
