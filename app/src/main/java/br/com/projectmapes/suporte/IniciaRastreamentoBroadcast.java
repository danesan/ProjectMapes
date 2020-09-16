package br.com.projectmapes.suporte;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import br.com.projectmapes.RastreamentoAlunoIntentService;

public class IniciaRastreamentoBroadcast extends BroadcastReceiver {

    private static final String TAG = IniciaRastreamentoBroadcast.class.getSimpleName();
    private RastreamentoAlunoIntentService rastreamentoAlunoIntentService;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("entrou", "onHandleIntent");

        long valor = intent.getLongExtra("valor_horario_final",0);

        Intent rastreamentoAlunointent = new Intent(context, RastreamentoAlunoIntentService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(rastreamentoAlunointent);
        } else {
            context.startService(rastreamentoAlunointent);
        }

        //configurarAlarmeParaFinalizarRastreamento(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configurarAlarmeParaFinalizarRastreamento(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        long horarioFinal = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_FINAL, 0);

        Intent agendamentoHorarioFinalRastreamentointent = new Intent(context, FinalizaRastreamentoBroadcast.class);
        PendingIntent agendamentoHorarioInicial = PendingIntent.getBroadcast(context,
                0, agendamentoHorarioFinalRastreamentointent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Funcoes funcoes = new Funcoes();
        Log.d("HORARIO_FINAL", funcoes.converterHorarioMilisegundosEmString(horarioFinal));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, horarioFinal, agendamentoHorarioInicial);
    }

}
