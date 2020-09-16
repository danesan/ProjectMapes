package br.com.projectmapes.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.w3c.dom.Text;

import java.util.Locale;

import br.com.projectmapes.R;
import br.com.projectmapes.suporte.Constantes;

public class SeekbarPreference extends Preference {

    private SeekBar seekBar;
    private TextView tituloSeekBar;
    private TextView descricaoSeekBar;
    private TextView valorSeekBar;
    private int valorInteiroSeekBar;
    private Button diminuirValorButton;
    private Button aumentarValorButton;

    private SharedPreferences sharedPreferences;

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.seekbarpreference);
        Log.d("CONSTRUTOR", "OK");
        sharedPreferences = context.
                getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        Log.d("VIEWHOLDER", "BINDSEEKBAR");
        tituloSeekBar = (TextView) holder.findViewById(R.id.titulo_seekbarpreference);
        descricaoSeekBar = (TextView) holder.findViewById(R.id.descricao_seekbarpreference);
        valorSeekBar = (TextView) holder.findViewById(R.id.valor_seekbarpreference);
        diminuirValorButton = (Button) holder.findViewById(R.id.diminuir_seekbarpreference_button);
        aumentarValorButton = (Button) holder.findViewById(R.id.aumentar_seekbarpreference_button);
        seekBar = (SeekBar) holder.findViewById(R.id.seekbarpreference);

        int valorSeekbar = Integer.parseInt(sharedPreferences.getLong(
                Constantes.KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO,
                Constantes.VALOR_PADRAO_SEEKBAR) / 1000 + "");
        seekBar.setProgress(valorSeekbar);
        valorSeekBar.setText("A cada " + valorSeekbar + " segundos");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int diferenca = progress % Constantes.VALOR_INCREMENTO_SEEKBAR;
                if(diferenca < (Constantes.VALOR_INCREMENTO_SEEKBAR/2 + 1)){
                    progress -= diferenca;
                } else {
                    progress += (Constantes.VALOR_INCREMENTO_SEEKBAR - diferenca);
                }
                seekBar.setProgress(progress);
                valorSeekBar.setText("A cada " + progress + " segundos");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(Constantes.KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO, progress * 1000);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        aumentarValorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valorInteiroSeekBar = seekBar.getProgress();
                int novoValorSeekBar = valorInteiroSeekBar + Constantes.VALOR_INCREMENTO_SEEKBAR;
                if(novoValorSeekBar > Constantes.VALOR_MAXIMO_SEEKBAR){
                    novoValorSeekBar -= Constantes.VALOR_INCREMENTO_SEEKBAR;
                }
                seekBar.setProgress(novoValorSeekBar);
                valorSeekBar.setText("A cada " + novoValorSeekBar + " segundos");
            }
        });

        diminuirValorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valorInteiroSeekBar = seekBar.getProgress();
                int novoValorSeekBar = valorInteiroSeekBar - Constantes.VALOR_INCREMENTO_SEEKBAR;
                if(novoValorSeekBar < Constantes.VALOR_MINIMO_SEEKBAR){
                    novoValorSeekBar += Constantes.VALOR_INCREMENTO_SEEKBAR;
                }
                seekBar.setProgress(novoValorSeekBar);
                valorSeekBar.setText("A cada " + novoValorSeekBar + " segundos");
            }
        });
        super.onBindViewHolder(holder);
    }

    public void setValor(int progress){
        seekBar.setProgress(progress);
    }

    public int getValor(){
        return seekBar.getProgress();
    }

    public int getValorPadrao(){
        return Constantes.VALOR_PADRAO_SEEKBAR;
    }


}
