package br.com.projectmapes;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;

import java.util.Calendar;

import br.com.projectmapes.dao.LocalizacaoDAO;
import br.com.projectmapes.modelo.Localizacao;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.Utils;


public class RastreamentoAlunoService extends Service {

    private static final String PACKAGE_NAME =
            "br.com.projectmapes.TesteRastreamentoAlunoService";
    private static final String TAG = RastreamentoAlunoService.class.getSimpleName();
    private static final String CHANNEL_ID = "my_channel_01";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";
    private static final int NOTIFICATION_ID = 1234;

    private SharedPreferences sharedPreferences;
    private static long HORARIO_INICIAL_EM_MILISEGUNDOS;
    private static long HORARIO_FINAL_EM_MILISEGUNDOS;
    private static long INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private final IBinder mBinder = new LocalBinder();
    private boolean mChangingConfiguration = false;
    private NotificationManager notificationManager;
    private Handler serviceHandler;

    private Usuario usuarioAluno;

    private CollectionReference localizacaoRef;
    private Funcoes funcoes;

    @Override
    public void onCreate() {
        Log.d("ON_CREATE_SERVICE", "OK");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES, MODE_PRIVATE);

        LocalizacaoDAO localizacaoDAO = new LocalizacaoDAO();
        localizacaoRef = localizacaoDAO.getLocalizacoesCollectionReference();

        funcoes = new Funcoes();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name,
                            NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    private void atualizarLocalizacao(LocationResult locationResult){
        Log.d(TAG, "Nova localização: " + locationResult.getLastLocation());

        HORARIO_INICIAL_EM_MILISEGUNDOS = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_INICIAL, 0);
        HORARIO_FINAL_EM_MILISEGUNDOS = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_FINAL, 0);

        Calendar horarioInicial = Calendar.getInstance();
        horarioInicial.setTimeInMillis(HORARIO_INICIAL_EM_MILISEGUNDOS);

        Calendar horarioFinal = Calendar.getInstance();
        horarioFinal.setTimeInMillis(HORARIO_FINAL_EM_MILISEGUNDOS);

        Location location = locationResult.getLastLocation();
        Calendar horarioLocation = Calendar.getInstance();
        horarioLocation.setTimeInMillis(location.getTime());

        Log.d("HORARIO_INICAL",funcoes.converterHorarioMilisegundosEmString(horarioInicial.getTimeInMillis()));
        Log.d("HORARIO_FINAL",funcoes.converterHorarioMilisegundosEmString(horarioFinal.getTimeInMillis()));
        Log.d("HORARIO_ATUAL",funcoes.converterHorarioMilisegundosEmString(horarioLocation.getTimeInMillis()));

        if(funcoes.compararHorarios(horarioInicial, horarioFinal) == 1){
            if((funcoes.compararHorarios(horarioLocation, horarioInicial) <= 0) &&
                    (funcoes.compararHorarios(horarioLocation, horarioFinal) >= 0)){
                salvarLocalizacao(location);
            }
        } else if((funcoes.compararHorarios(horarioLocation, horarioInicial) <= 0) ||
                (funcoes.compararHorarios(horarioLocation, horarioFinal) >= 0)){
            salvarLocalizacao(location);
        }
            //verifica se o serviço está rodando em foreground(primeiro plano)
        if (servicoEstaRodandoEmPrimeiroPlano(this)) {
            //notificationManager.notify(NOTIFICATION_ID, getNotificacao());
        }
    }

    private void salvarLocalizacao(Location location) {
        Localizacao localizacao = new Localizacao(
                location.getTime(),
                location.getLatitude(),
                location.getLongitude());
        localizacaoRef.document(usuarioAluno.getUid()).collection("localizacoes_salvas").add(localizacao);
        localizacaoRef.document(usuarioAluno.getUid()).set(localizacao);
    }


    public boolean servicoEstaRodandoEmPrimeiroPlano(Context context){
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service
                : manager.getRunningServices(Integer.MAX_VALUE)){
            if(getClass().getName().equals(service.service.getClassName())){
                if(service.foreground){
                    return true;
                }
            }
        }
        return false;
    }

    private Notification getNotificacao(){
        Intent intent = new Intent(this, RastreamentoAlunoService.class);
        CharSequence text = Utils.getLocationText(getUltimaLocalizacaoConhecida());
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TelaPrincipalUsuarioAlunoActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .addAction(R.drawable.ic_launcher_background, getString(R.string.launch_Activity),
                    activityPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private LocationRequest getLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS);
        locationRequest.setFastestInterval(INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.d("INTERVAL_REQ_LOC:", INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS + "");
        return locationRequest;
    }

    private Location getUltimaLocalizacaoConhecida() {
        final Location[] ultimaLocalizacao = new Location[1];
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                ultimaLocalizacao[0] = task.getResult();
                                Log.w(TAG, "Localização conhecida");
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
            return ultimaLocalizacao[0];
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("LOCATION_GET_DURATION", "start in service");
        try{
            if(locationCallback != null){
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
            locationRequest = getLocationRequest();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    atualizarLocalizacao(locationResult);
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        } catch (SecurityException unlikely){
            //Utils.setRequestLocationUpdates(this, false);
            Log.e(TAG, "Permissão não localizada" + unlikely);
        }

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            //removeLocationUpdates();
            //stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            //Utils.setRequestLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            //Utils.setRequestLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");
            startForeground(NOTIFICATION_ID, getNotificacao());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    public void requestLocationUpdates(){
        Log.i(TAG, "Requesting location updates");
        boolean horariosConfigurados = true;

        HORARIO_INICIAL_EM_MILISEGUNDOS = sharedPreferences.getLong(
                Constantes.KEY_PREF_HORARIO_INICIAL,0);
        HORARIO_FINAL_EM_MILISEGUNDOS = sharedPreferences.getLong(
                Constantes.KEY_PREF_HORARIO_FINAL,0);
        INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS = sharedPreferences.getLong(
                Constantes.KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO, Constantes.VALOR_PADRAO_SEEKBAR * 1000);

        if(HORARIO_INICIAL_EM_MILISEGUNDOS == 0){
            horariosConfigurados = false;
        }

        if(HORARIO_FINAL_EM_MILISEGUNDOS == 0){
            horariosConfigurados = false;
        }

        if(INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS == 0){
            horariosConfigurados = false;
        }
        Log.d("HOR_INICIAL", HORARIO_INICIAL_EM_MILISEGUNDOS + "");
        Log.d("HOR_FINAL", HORARIO_FINAL_EM_MILISEGUNDOS + "");
        Log.d("INT_REQ", INTERVALO_REQUISICAO_LOCALIZACAO_EM_MILISEGUNDOS + "");

        if(horariosConfigurados){
            Utils.setRequestLocationUpdates(this, true);
            startService(new Intent(getApplicationContext(), RastreamentoAlunoService.class));
        } else {
            Toast.makeText(this, "Configure o horário/período para requisitar a localização " +
                    "do dispositivo em CONFIGURAÇÕES", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //stopLocationUpdates();
        serviceHandler.removeCallbacksAndMessages(null);
    }

    public void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("", "stopLocationUpdates");
                    }
                });
    }

    public class LocalBinder extends Binder{
        RastreamentoAlunoService getService(){
            return RastreamentoAlunoService.this;
        }
    }

    public void setUsuarioAluno(Usuario usuarioAluno) {
        this.usuarioAluno = usuarioAluno;
    }
}
