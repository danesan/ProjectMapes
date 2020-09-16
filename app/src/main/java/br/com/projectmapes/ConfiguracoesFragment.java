package br.com.projectmapes;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Calendar;
import java.util.TimeZone;

import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.widgets.SeekbarPreference;

public class ConfiguracoesFragment extends PreferenceFragmentCompat {

    private Preference preferenceHorarioInicial;
    private Preference preferenceHorarioFinal;
    private SeekbarPreference preferencePeriodoRequisicaoLocalizacao;
    private Funcoes funcoes;
    private SharedPreferences sharedPreferences;
    private RastreamentoAlunoIntentService rastreamentoAlunoIntentService;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_configuracoes, rootKey);

        preferenceHorarioInicial = findPreference(Constantes.KEY_PREF_HORARIO_INICIAL);
        preferenceHorarioFinal = findPreference(Constantes.KEY_PREF_HORARIO_FINAL);
        preferencePeriodoRequisicaoLocalizacao = findPreference(
                Constantes.KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO);

        funcoes = new Funcoes();
        sharedPreferences = getActivity().
                getSharedPreferences("configuracoes", Context.MODE_PRIVATE);

        iniciarPreferenciaHorario(preferenceHorarioInicial, Constantes.KEY_PREF_HORARIO_INICIAL);
        iniciarPreferenciaHorario(preferenceHorarioFinal, Constantes.KEY_PREF_HORARIO_FINAL);
        Log.d("ON_CREATE_PREF", "OK");
    }

    private void iniciarPreferenciaHorario(Preference preference, final String chave){
        long horario = sharedPreferences.getLong(chave, 0);
        if(horario == 0){
            preference.setSummary("Não definido");
        } else {
            String horarioString = funcoes
                    .converterHorarioMilisegundosEmString(horario);
            preference.setSummary(horarioString
                    .substring(0,horarioString.length() - 3));
        }
        preference
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //preference.onBindViewHolder(null);
                        exibirTimePickerDialog(preference, chave);
                        return false;
                    }
                });
    }

    private void exibirTimePickerDialog(final Preference preference, final String chave){
        TimePickerDialog.OnTimeSetListener tratador =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar horario = Calendar.getInstance();
                        horario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        horario.set(Calendar.MINUTE, minute);
                        horario.set(Calendar.SECOND, 0);

                        String horarioString = funcoes
                                .converterHorarioMilisegundosEmString(horario.getTimeInMillis());
                        preference.setSummary(horarioString
                                .substring(0,horarioString.length() - 3));

                        salvarPreferencias(chave, horario.getTimeInMillis());
                    }
                };
        Calendar horario = Calendar.getInstance();
        horario.setTimeInMillis(
                funcoes.converterHorarioStringEmMilisegundos(preference
                        .getSummary().toString()));
        horario.setTimeZone(TimeZone.getTimeZone("GMT"));
        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                tratador,
                horario.get(Calendar.HOUR_OF_DAY),
                horario.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void salvarPreferencias(String chave, long valor){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(chave, valor);
        editor.commit();

        TelaPrincipalUsuarioAlunoActivity telaPrincipalUsuarioAlunoActivity = (TelaPrincipalUsuarioAlunoActivity) getActivity();
        telaPrincipalUsuarioAlunoActivity.configurarAlarm(chave, valor);
    }
}
