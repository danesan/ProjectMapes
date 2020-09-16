package br.com.projectmapes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.modelo.GeoFenceInfo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.FinalizaRastreamentoBroadcast;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.IniciaRastreamentoBroadcast;

public class TelaPrincipalUsuarioAlunoActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnRequestPermissionsResultCallback {

    private static final String TAG = TelaPrincipalUsuarioAlunoActivity.class.getSimpleName();
    private SupportMapFragment mapaFragment;
    private GoogleMap mGoogleMap;
    private CameraPosition posicaoCamera;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ConnectivityManager gerenciadorConexao;
    private NetworkInfo redeAtiva;
    private ConexaoReceiver conexaoReceiver;
    private int tentativas;
    private List<String> permissoes;

    private static final int ZOOM_DEFAULT = 15;
    private static final int REQUEST_PERMISSOES = 1;
    private static final int REQUEST_ERRO_PLAY_SERVICES = 5;
    private static final int REQUEST_CHECK_SETTINGS = 6;
    private static final String KEY_POSICAO_CAMERA = "posicao_camera";
    private static final String KEY_LOCALIZACAO = "localizacao";
    private static final int M_MAX_ENTRIES = 5;
    private boolean mPermissaoLocalizacaoConcedida = false;

    private Location ultimaLocalizacao;
    private LocationRequest locationRequest;

    private static Activity activity;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView menuLateralNavigationView;
    private ActionBarDrawerToggle drawerToggle;
    private int opcaoSelecionada;

    private FragmentContainerView fragmentContainerView;
    private TelaVinculosAlunoFragment telaVinculosAlunoFragment;

    private SharedPreferencesDAO sharedPreferencesDAO;
    private UsuarioDAO usuarioDAO;
    private Usuario usuario;

    public RastreamentoAlunoService rastreamentoAlunoService;
    public RastreamentoAlunoIntentService rastreamentoAlunoIntentService;
    private boolean bound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Serviço conectado");
            //RastreamentoAlunoService.LocalBinder binder
            //        = (RastreamentoAlunoService.LocalBinder) service;
            RastreamentoAlunoIntentService.RastreamentoBinder binder = (RastreamentoAlunoIntentService.RastreamentoBinder) service;
            rastreamentoAlunoIntentService = binder.getService();
            rastreamentoAlunoIntentService.setUsuario(usuario);
            rastreamentoAlunoIntentService.setGoogleMap(mGoogleMap);
            //rastreamentoAlunoIntentService.requestLocationUpdates();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Serviço desconectado");
            rastreamentoAlunoService = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ON_CREATE", "ON_CREATE");
        setContentView(R.layout.activity_tela_principal_usuario_aluno);
        toolbar = (Toolbar) findViewById(R.id.toolbar_tela_principal_usuario_aluno);
        setSupportActionBar(toolbar);

        fragmentContainerView = findViewById(R.id.principal_fragment_view_tela_principal_usuario_aluno);

        sharedPreferencesDAO = new SharedPreferencesDAO(this);
        usuario = sharedPreferencesDAO.getUsuarioLogado();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_menu_lateral_tela_principal_usuario_aluno);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        menuLateralNavigationView = (NavigationView)
                findViewById(R.id.navigation_view_menu_lateral_tela_principal_usuario_aluno);
        menuLateralNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selecionarOpcaoMenuLateral(menuItem);
                return true;
            }
        });

        if(savedInstanceState != null){
            ultimaLocalizacao = savedInstanceState.getParcelable(KEY_LOCALIZACAO);
            posicaoCamera = savedInstanceState.getParcelable(KEY_POSICAO_CAMERA);
            opcaoSelecionada = savedInstanceState.getInt("menuItem");
        } else {
            opcaoSelecionada = R.id.opcao_inicio_menu_usuario_aluno;
        }
        if(opcaoSelecionada != 0) {
            selecionarOpcaoMenuLateral(menuLateralNavigationView.getMenu().findItem(opcaoSelecionada));
        }

        iniciarGooglePlayServices();
        activity = this;

        if (precisaVerificarPermissoes()) {
            Log.d("VERIFICAR_PERMIS", "Deve solicitar permissoes");
            solicitarPermissoes(permissoes);
        } else {
            mPermissaoLocalizacaoConcedida = true;
            //inicializarGoogleMaps();
        }
        carregarInformacoesMenuLateral();
    }

    @Override
    public void onPostCreate(Bundle savedInstance, PersistableBundle persistableBundle){
        super.onPostCreate(savedInstance, persistableBundle);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(opcaoSelecionada != R.id.opcao_inicio_menu_usuario_aluno){
            selecionarOpcaoMenuLateral(menuLateralNavigationView.getMenu().findItem(R.id.opcao_inicio_menu_usuario_aluno));
        } else {
            finish();
        }
    }

    private void selecionarOpcaoMenuLateral(final MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();

        if(menuItem.getItemId() != opcaoSelecionada){
            Log.d("OPCAO_JA_SELE", opcaoSelecionada + "");
            Log.d("MENU_SELEC", menuItem.getItemId()  + "");
            opcaoSelecionada = menuItem.getItemId();

            FragmentManager fragmentMAnager = getSupportFragmentManager();
            switch (opcaoSelecionada){
                case R.id.opcao_inicio_menu_usuario_aluno :
                    toolbar.setTitle(getString(R.string.app_name));
                    fragmentMAnager.beginTransaction().replace(fragmentContainerView.getId(),
                            mapaFragment).commit();
                    mapaFragment.getMapAsync(this);
                    break;
                case R.id.opcao_vinculos_menu_usuario_aluno :
                    toolbar.setTitle(getString(R.string.nome_tela_vinculos));
                    telaVinculosAlunoFragment = TelaVinculosAlunoFragment.novaInstancia(usuario);
                    fragmentMAnager.beginTransaction().replace(fragmentContainerView.getId(),
                            telaVinculosAlunoFragment).commit();
                    break;
                case R.id.opcao_configuracoes_menu_usuario_aluno :
                    toolbar.setTitle(getString(R.string.nome_tela_configuracoes));
                    //telaVinculosAlunoFragment = TelaVinculosAlunoFragment.novaInstancia(usuario);
                    Intent intent = new Intent(this, ConfiguracoesFragment.class);
                    fragmentMAnager.beginTransaction().replace(fragmentContainerView.getId(),
                            new ConfiguracoesFragment()).commit();
                    break;
                case R.id.opcao_sair :
                    FirebaseAuth.getInstance().signOut();
                    sharedPreferencesDAO.atualizarUsuarioLogout();

                    Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    if(rastreamentoAlunoIntentService != null){
                        rastreamentoAlunoIntentService.pararServico();
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.pesquisa_vinculos:

        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void iniciarGooglePlayServices(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(api.isUserResolvableError(resultCode)){
                Dialog dialog = api.getErrorDialog(this, resultCode, REQUEST_ERRO_PLAY_SERVICES);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                dialog.show();
            } else {
                Toast.makeText(this, R.string.google_play_nao_suportado, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle parametros) {
        super.onSaveInstanceState(parametros);
        if (mGoogleMap != null) {
            parametros.putParcelable(KEY_POSICAO_CAMERA, mGoogleMap.getCameraPosition());
            parametros.putParcelable(KEY_LOCALIZACAO, ultimaLocalizacao);
            super.onSaveInstanceState(parametros);
        }
        parametros.putInt("menuItem", opcaoSelecionada);
    }

    @Override
    public void onRestoreInstanceState(Bundle parametros) {
        super.onSaveInstanceState(parametros);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent rastreamentoAlunointent = new Intent(this, RastreamentoAlunoIntentService.class);
        bindService(rastreamentoAlunointent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(conexaoReceiver);
        Log.d("OSPAUSE_TELAPRINC","CHAMOU");
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    @Override
    protected void onStop(){
        if(bound){
            unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
        Log.d("OSNSTOP_TELAPRINC","CHAMOU");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ON_RESUME", "ON_RESUME");
        Log.d("PermissaLocalizacao", mPermissaoLocalizacaoConcedida+"");

        conexaoReceiver = new ConexaoReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(conexaoReceiver, filter);

        if(mPermissaoLocalizacaoConcedida){
            inicializarGoogleMaps();
        }

        verificarHorariosJaConfigurados();
    }

    class ConexaoReceiver extends BroadcastReceiver {
        private boolean primeiraVez = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(primeiraVez){
                primeiraVez = false;
                return;
            }
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                //animarCameraMapa(ultimaLocalizacao);
            }
        }
    }

    private void inicializarGoogleMaps() {
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        //mPlacesClient = Places.createClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            mapaFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(fragmentContainerView.getId());
            mapaFragment.getMapAsync(this);
        } catch (ClassCastException ex){
        }
        Log.d("ENTROU", "INICIOUGOOGLEMAPS");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        Log.d("GOOGLE_MAP", googleMap.toString());
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        configurarGoogleMap();
        desenharGeofenceMapa();
    }

    public void desenharGeofenceMapa(){
        GeoFenceInfo geoFenceInfo = RastreamentoAlunoIntentService.getGeofenceInfo();
        if(geoFenceInfo != null){
            LatLng posicao = new LatLng(geoFenceInfo.getLatitude(), geoFenceInfo.getLongitude());
            mGoogleMap.addCircle(new CircleOptions()
                    .strokeWidth(2)
                    .fillColor(0x9981D0EB)
                    .center(posicao)
                    .radius(geoFenceInfo.getRaio()));
        } else {
            Log.d("GEOFENCE_INFO", "nulo");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.d("LOCATION_REQUEST", "usuario aceitou");
                tentativas = 0;
                mPermissaoLocalizacaoConcedida = true;
                configurarGoogleMap();
            } else {
                Log.d("LOCATION_REQUEST", "usuario não aceitou");
                //criarSolicitacoesDeLocalizacao();
            }
        }
    }

    private void configurarGoogleMap(){
        if(mGoogleMap == null){
            Log.d("GOOGLEMAP", "NULO");
            return;
        }
        try {
            if(mPermissaoLocalizacaoConcedida){
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Log.d("PERMISSAO", "LOCALIZAÇÃO CONCEDIDA");
                recuperarLocalizacaoDispositivo();
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                ultimaLocalizacao = null;
                Log.d("PERMISSAO", "NÃO CONCEDIDA");
            }
        } catch (Exception e){
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void recuperarLocalizacaoDispositivo(){
        try{
            if(mPermissaoLocalizacaoConcedida){
                Log.d(TAG, "RECUPERANDO LOCALIZAÇÃO DISPOSITIVO.");

                Task<Location> taskRecuperarLocalizacao = fusedLocationProviderClient.getLastLocation();
                taskRecuperarLocalizacao.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "Não foi possível conectar", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Não foi possível conectar");
                    }
                });
                taskRecuperarLocalizacao.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            tentativas = 0;
                            ultimaLocalizacao = task.getResult();

                            if(ultimaLocalizacao != null){
                                Log.d(TAG, "LOCALIZAÇÃO ENCONTRADA.");
                                Log.d(TAG, ultimaLocalizacao.toString());

                                gerenciadorConexao = (ConnectivityManager)
                                        getSystemService(Context.CONNECTIVITY_SERVICE);
                                redeAtiva = gerenciadorConexao.getActiveNetworkInfo();
                                boolean estaConectado = redeAtiva != null &&
                                        redeAtiva.isConnectedOrConnecting();

                                if(estaConectado) {
                                    animarCameraMapa(ultimaLocalizacao);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Você não está conectado à rede." +
                                            "\nVerifique sua conexão com a internet.", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        } else {
                            Log.d(TAG, "Localizacao atual é null. Usando localização default.");
                            Log.d(TAG, "Exception %s", task.getException());
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void animarCameraMapa(Location ultimaLocalizacao){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(ultimaLocalizacao.getLatitude(),
                        ultimaLocalizacao.getLongitude()))
                .zoom(17)
                //.bearing(90)
                //.tilt(45)
                .build();
        mGoogleMap
                .animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private boolean precisaVerificarPermissoes(){
        permissoes = new ArrayList<String>();

        boolean permissaoConcedidaAcessoMemoria =
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
        boolean permissaoConcedidaLocalizacaoPorGPS =
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if(!permissaoConcedidaAcessoMemoria){
            permissoes.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissaoConcedidaLocalizacaoPorGPS){
            permissoes.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        return !permissoes.isEmpty();
    }

    private void solicitarPermissoes(List<String> permissoes){
        if(!permissoes.isEmpty()){
            String[] permissoesArray = new String[permissoes.size()];
            permissoes.toArray(permissoesArray);
            Log.d("PermissoesSoli", permissoesArray.toString());
            ActivityCompat.requestPermissions(this, permissoesArray, REQUEST_PERMISSOES);
        } else {
            mPermissaoLocalizacaoConcedida = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PERMISSAO_REQUEST_CODE", requestCode+"");
        //mPermissaoLocalizacaoConcedida = false;

        switch (requestCode){
            case REQUEST_PERMISSOES :
                Log.d("PERMISSOES_SOLICITADAS", permissions.length+"");
                mPermissaoLocalizacaoConcedida = true;
                for (int i=0; i < permissions.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mPermissaoLocalizacaoConcedida = false;
                        break;
                    }
                }
                if(!mPermissaoLocalizacaoConcedida){
                    Toast.makeText(this, "Você deve conceder as permissões!", Toast.LENGTH_LONG).show();
                    finish();
                }
                //rastreamentoAlunoIntentService.criarGeofence();
        }
    }

    //-3.133836, -60.012759
    //(75m + 97.5) / 2 = 86.25

    public void configurarAlarm(String chave, long valor) {

        if (chave.equals(Constantes.KEY_PREF_HORARIO_INICIAL)) {
            Log.d("Configurando", "alarme " + Constantes.KEY_PREF_HORARIO_INICIAL);

            Intent agendamentoHorarioInicialRastreamentointent = new Intent(this, IniciaRastreamentoBroadcast.class);
            PendingIntent agendamentoHorarioInicial = PendingIntent.getBroadcast(this,
                    0, agendamentoHorarioInicialRastreamentointent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Funcoes funcoes = new Funcoes();
            Log.d("HORARIO_INICIAL", funcoes.converterHorarioMilisegundosEmString(valor));

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, valor, AlarmManager.INTERVAL_DAY, agendamentoHorarioInicial);

        } else if (chave.equals(Constantes.KEY_PREF_HORARIO_FINAL)) {
            Log.d("Configurando", "alarme " + Constantes.KEY_PREF_HORARIO_FINAL);

            Intent agendamentoHorarioFinalRastreamentointent = new Intent(this, FinalizaRastreamentoBroadcast.class);
            PendingIntent agendamentoHorarioFinal = PendingIntent.getBroadcast(this,
                    0, agendamentoHorarioFinalRastreamentointent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Funcoes funcoes = new Funcoes();
            Log.d("HORARIO_FINAL", funcoes.converterHorarioMilisegundosEmString(valor));

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, valor, AlarmManager.INTERVAL_DAY, agendamentoHorarioFinal);
        }
    }

    private void verificarHorariosJaConfigurados(){

        SharedPreferences sharedPreferences = getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        if(sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_INICIAL, 0) == 0 ||
                sharedPreferences.getLong(Constantes.KEY_PREF_HORARIO_FINAL, 0) == 0){
            Snackbar.make(findViewById(R.id.tela_principal_usuario_aluno_coordinator_layout), R.string.configurar_horarios,
                    Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }

    private void carregarInformacoesMenuLateral(){
        Funcoes funcoes = new Funcoes();
        ImageView fotoPerfilUsuarioResponsavelImageView = menuLateralNavigationView.getHeaderView(0)
                .findViewById(R.id.imageView_imagemUsuario_cabecalho_menuLateral);
        TextView loginUsuarioResponsavel = menuLateralNavigationView.getHeaderView(0)
                .findViewById(R.id.textView_loginUsuario_menu_lateral);
        TextView nomeCompletoUsuarioResponsavel = menuLateralNavigationView.getHeaderView(0)
                .findViewById(R.id.textView_nomeCompletoUsuario_menu_lateral);

        Picasso.with(getApplicationContext()).load(usuario.getFotoURL()).into(fotoPerfilUsuarioResponsavelImageView);
        loginUsuarioResponsavel.setText(usuario.getLogin());
        nomeCompletoUsuarioResponsavel.setText(funcoes.converterLetrasNome(usuario.getNome()));
    }

}
