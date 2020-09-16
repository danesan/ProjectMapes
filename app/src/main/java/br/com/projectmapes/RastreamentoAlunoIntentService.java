package br.com.projectmapes;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;

import br.com.projectmapes.dao.LocalizacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.modelo.GeoFenceInfo;
import br.com.projectmapes.modelo.Localizacao;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.GeoFenceDB;
import br.com.projectmapes.suporte.GeofenceBroadcastReceiver;

public class RastreamentoAlunoIntentService extends IntentService {

    private static final String CHANNEL_ID = "notificacao_rastreamento_channel";
    private static final String EXTRA_INICIOU_CLICANDO_NOTIFICACAO = "iniciou_clicando_notificacao";
    private static long INTERVALO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = 10000;
    private static long INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = 5000;

    private final IBinder binder = new RastreamentoBinder();
    private NotificationManager notificationManager;
    private int NOTIFICATION_ID = 15;

    private boolean configuracoesAlteradas = false;
    private Intent rastreamentoAlunoIntent;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FusedLocationProviderClient fusedLocationProviderClientTempoReal;
    private LocationCallback locationCallback;
    private LocationCallback locationCallbackTempoReal;
    private Handler serviceHandler;
    private Location location;
    private GoogleMap googleMap;
    private GeofencingClient geofencingClient;
    private GeofencingRequest geofencingRequest;
    private PendingIntent pendingIntent;
    private PendingIntent geofencePendingIntent;
    private GeoFenceInfo geoFenceInfo;
    private GeoFenceDB geoFenceDB;

    private Usuario usuario;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private SharedPreferences sharedPreferences;
    private LocalizacaoDAO localizacaoDAO;
    private Funcoes funcoes;

    public RastreamentoAlunoIntentService() {
        super("RastreamentoAlunoIntentService");
    }

    @Override
    public void onCreate() {
        sharedPreferencesDAO = new SharedPreferencesDAO(getBaseContext());
        usuario = sharedPreferencesDAO.getUsuarioLogado();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                salvarLocalizacao(locationResult.getLastLocation());
            }
        };

        locationCallbackTempoReal = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //Log.d("LOC_TEMPO_REAL", location.toString());
                    Localizacao localizacao = new Localizacao(System.currentTimeMillis(), location.getLatitude(), location.getLongitude());
                    localizacaoDAO.getLocalizacoesCollectionReference().document(usuario.getUid()).set(localizacao);
                    //atualizarLocalizacaoTempoReal(location);
                }
            }
        };

        //criarLocationRequestParaSalvarRotas();
        //criarLocationRequestEmTempoReal();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClientTempoReal = LocationServices.getFusedLocationProviderClient(this);
        getUltimaLocalizacaoConhecida();

        criarGeofence();

        HandlerThread handlerThread = new HandlerThread("servico_rastreamento");
        handlerThread.start();

        serviceHandler = new Handler(handlerThread.getLooper());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String nomeCanalNotificacao = getString(R.string.nome_canal_notificacao_servico_rastreamento);
            String descricao = getString(R.string.descricao_canal_notificacao_servico_rastreamento);
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    nomeCanalNotificacao, importancia);
            notificationChannel.setDescription(descricao);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        sharedPreferences = getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES, MODE_PRIVATE);
        localizacaoDAO = new LocalizacaoDAO();
        funcoes = new Funcoes();
        Log.d("ENTROU", "ON_CREATE_SERVICE_INTENT");
    }

    @Override
    public void onDestroy() {
        //notificationManager.cancel(NOTIFICATION_ID);
        //super.onDestroy();
        Log.d("CHAMOU", "ON_DESTROY_SERVICE");
        serviceHandler.removeCallbacks(null);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        fusedLocationProviderClientTempoReal.removeLocationUpdates(locationCallbackTempoReal);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configuracoesAlteradas = true;
    }

    private void salvarLocalizacao(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double latitudePosicaoInicial = 0;
        double longitudePosicaoInicial = 0;

        if(this.location != null) {
            latitudePosicaoInicial = this.location.getLatitude();
            longitudePosicaoInicial = this.location.getLongitude();
        }

        boolean alterouPosicao = false;
        long intervaloLocationRequest = sharedPreferencesDAO.getIntervaloLocationRequest();

        if(locationRequest.getFastestInterval() != intervaloLocationRequest) {
            INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = intervaloLocationRequest;
            INTERVALO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = (long) (INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS * 1.1);

            atualizarLocationRequest();
        }

        if(latitude > (latitudePosicaoInicial + 0.000030) || (latitude < (latitudePosicaoInicial - 0.000030))){
            alterouPosicao = true;
        }
        if(longitude > (longitudePosicaoInicial + 0.000030) || (longitude < (longitudePosicaoInicial - 0.000030))){
            alterouPosicao = true;
        }

        if(alterouPosicao){
            this.location = location;

            Localizacao localizacao = new Localizacao(System.currentTimeMillis(), latitude, longitude);

            //localizacaoDAO.getLocalizacoesCollectionReference().document(usuario.getUid()).set(localizacao);

            long HORARIO_INICIAL_EM_MILISEGUNDOS = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_INICIAL, 0);
            long HORARIO_FINAL_EM_MILISEGUNDOS = sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_FINAL, 0);

            Calendar horarioInicial = Calendar.getInstance();
            horarioInicial.setTimeInMillis(HORARIO_INICIAL_EM_MILISEGUNDOS);

            Calendar horarioFinal = Calendar.getInstance();
            horarioFinal.setTimeInMillis(HORARIO_FINAL_EM_MILISEGUNDOS);

            Calendar horarioLocation = Calendar.getInstance();
            horarioLocation.setTimeInMillis(location.getTime());

            if(funcoes.compararHorarios(horarioInicial, horarioFinal) == 1){
                if((funcoes.compararHorarios(horarioLocation, horarioInicial) <= 0) &&
                        (funcoes.compararHorarios(horarioLocation, horarioFinal) >= 0)){
                    localizacaoDAO.getLocalizacoesCollectionReference().document(usuario.getUid()).collection("localizacoes_salvas").add(localizacao);
                }
            } else if((funcoes.compararHorarios(horarioLocation, horarioInicial) <= 0) ||
                    (funcoes.compararHorarios(horarioLocation, horarioFinal) >= 0)){
                localizacaoDAO.getLocalizacoesCollectionReference().document(usuario.getUid()).collection("localizacoes_salvas").add(localizacao);
            }
        } else {
            Log.d("DEBUG", "NÂO ALTEROU POSIÇÃO");
        }
        Log.d("LOCATION", location.toString());
    }



    private Notification getNotificacaoServicoEmExecucao() {
        int flag = Constantes.FLAG_USUARIO_ALUNO;
        String texto = "Serviço de rastreamento em execução";

        Intent intent = new Intent(this, RastreamentoAlunoIntentService.class);
        intent.putExtra(EXTRA_INICIOU_CLICANDO_NOTIFICACAO, true);

        PendingIntent serviceIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TelaPrincipalUsuarioAlunoActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        builder.setSmallIcon(R.drawable.ic_notificacao)
                .setColor(getResources().getColor(R.color.cor_primaria_escura))
                //.setContentTitle(getString(R.string.app_name))
                .setContentText(texto)
                .setTicker("Chegou uma notificação")
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(activityPendingIntent)
                .setLights(Color.BLUE, 1000, 5000)
                .setVibrate(new long[]{100, 500, 200, 800})
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setNumber(flag);

        return builder.build();
    }

    private void criarLocationRequestEmTempoReal(){
        LocationRequest locationRequestEmTempoReal = new LocationRequest();
        locationRequestEmTempoReal.setInterval(5000);
        locationRequestEmTempoReal.setFastestInterval(1000);
        locationRequestEmTempoReal.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClientTempoReal.requestLocationUpdates(locationRequestEmTempoReal,
                locationCallbackTempoReal,
                Looper.getMainLooper());
    }


    private void criarLocationRequestParaSalvarRotas(){
        SharedPreferencesDAO sharedPreferencesDAO = new SharedPreferencesDAO(this);
        INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = sharedPreferencesDAO.getIntervaloLocationRequest();
        INTERVALO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS = (long) (INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS * 1.1);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVALO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS);
        locationRequest.setFastestInterval(INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.myLooper());
    }

    private void atualizarLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVALO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS);
        locationRequest.setFastestInterval(INTERVALO_CURTO_ATUALIZACAO_LOCATION_REQUEST_EM_MILISSEGUNDOS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void getUltimaLocalizacaoConhecida(){
        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful() && task.getResult() != null){
                        location = task.getResult();
                    } else {
                        Log.e("ERRO", "Erro ao recuperar localização!");
                    }
                }
            });
        } catch (SecurityException e){
            Log.e("ERRO", "Sem permissão para acesso à localização " + e);
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setGoogleMap(GoogleMap googleMap){
        this.googleMap = googleMap;
    }

    public void requestLocationUpdates() {
        rastreamentoAlunoIntent = new Intent(getApplicationContext(), RastreamentoAlunoIntentService.class);
        startService(rastreamentoAlunoIntent);
    }

    public void pararServico(){
        if(rastreamentoAlunoIntent != null){
            stopService(rastreamentoAlunoIntent);
        }
        //stopSelf();
    }

    public void atualizarLocalizacaoTempoReal(Location localizacaoAtual){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(localizacaoAtual.getLatitude(),
                        localizacaoAtual.getLongitude()))
                .zoom(17)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void criarGeofence(){
        Log.d("CRIOU", "GEOFENCE");
        final PendingIntent pendingIntent = getGeofencePendingIntent();
        geoFenceInfo = getGeofenceInfo();
        final GeofencingRequest geofencingRequest = getGeofencingRequest(geoFenceInfo);
        geoFenceDB = new GeoFenceDB(getBaseContext());
        geoFenceDB.salvarGeofence("1", geoFenceInfo);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GEOFENCE", "ADICIONADA_COM_SUCESSO");
                        geoFenceInfo = null;
                        //desenharGeofenceMapa();
                    }
                });
    }

    private PendingIntent getGeofencePendingIntent(){
        if(geofencePendingIntent != null){
            return geofencePendingIntent;
        }
        Intent intent = new Intent(getApplicationContext(), GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Intent intent = new Intent(this, RastreamentoAlunoIntentService.class);
        //geofencePendingIntent = PendingIntent.getService(this, 0,
        //        intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public static GeoFenceInfo getGeofenceInfo() {
        //LOCALIZAÇÃO DO IFAM CMC
        return new GeoFenceInfo("1",
                Constantes.KEY_LATITUDE_IFAM,
                Constantes.KEY_LONGITUDE_IFAM,
                Constantes.KEY_RAIO_FENCE_IFAM, Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
    }

    private GeofencingRequest getGeofencingRequest(GeoFenceInfo geoFenceInfo) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geoFenceInfo.getGeofence());
        return builder.build();
    }

    public class RastreamentoBinder extends Binder {
        RastreamentoAlunoIntentService getService() {
            return RastreamentoAlunoIntentService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.d("ENTROU", "ON_BIND");
        stopForeground(true);
        configuracoesAlteradas = false;
        return binder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("ENTROU", "REBIND");
        super.onRebind(intent);
        stopForeground(true);
        configuracoesAlteradas = false;
        //desenharGeofenceMapa();
        //geofencingClient = intent.getParcelableExtra("geofencingclient");
        //setGoogleMap(googleMap);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("ENTROU", "ON_UNBIND");
        startForeground(NOTIFICATION_ID, getNotificacaoServicoEmExecucao());
        //intent.putExtra("geofencingclient", (Parcelable) geofencingClient);
        /*geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //geoFenceDB = new GeoFenceDB(getBaseContext());
                        //geoFenceDB.salvarGeofence("1", geoFenceInfo);
                        //geoFenceInfo = null;
                        //desenharGeofenceMapa();
                    }
                });*/
        return true;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent,flags,startId);
        Log.d("chamou", "onStartCommand");

        criarLocationRequestParaSalvarRotas();
        criarLocationRequestEmTempoReal();
        onUnbind(intent);

        return START_NOT_STICKY;
    }
}
