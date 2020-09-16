package br.com.projectmapes.suporte;

import android.app.ActivityManager;
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

public class FinalizaRastreamentoBroadcast extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("entrou", "onHandleIntent");

        //Intent rastreamentoAlunointent =
        //        intent.getParcelableExtra("rastreamentoAlunointent");
        Log.d("FinalizaRast", intent.toString());

        Intent rastreamentoAlunointent = new Intent(context, RastreamentoAlunoIntentService.class);
        context.stopService(rastreamentoAlunointent);

        //configurarAlarmeParaIniciarRastreamento(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configurarAlarmeParaIniciarRastreamento(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        long horarioInicial = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_INICIAL, 0);

        Intent agendamentoHorarioInicialRastreamentointent = new Intent(context, IniciaRastreamentoBroadcast.class);
        PendingIntent agendamentoHorarioInicial = PendingIntent.getBroadcast(context,
                0, agendamentoHorarioInicialRastreamentointent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Funcoes funcoes = new Funcoes();
        Log.d("HORARIO_INICIAL", funcoes.converterHorarioMilisegundosEmString(horarioInicial));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, horarioInicial, agendamentoHorarioInicial);
    }

}
