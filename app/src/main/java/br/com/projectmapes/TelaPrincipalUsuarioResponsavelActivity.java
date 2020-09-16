package br.com.projectmapes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.projectmapes.dao.LocalizacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.GeoFenceInfo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.GeoFenceDB;
import br.com.projectmapes.suporte.MapesApplication;

public class TelaPrincipalUsuarioResponsavelActivity extends AppCompatActivity implements
        //OnMapReadyCallback,
        //OnRequestPermissionsResultCallback,
        SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private static final String TAG = TelaPrincipalUsuarioAlunoActivity.class.getSimpleName();
    //private SupportMapFragment mapaFragment;
    private GoogleMap mGoogleMap;
    private CameraPosition posicaoCamera;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private GeoFenceInfo geoFenceInfo;
    private GeoFenceDB geoFenceDB;
    private ConnectivityManager gerenciadorConexao;
    private NetworkInfo redeAtiva;
    //private ConexaoReceiver conexaoReceiver;
    private int tentativas;
    private List<String> permissoes;
    private Usuario usuario;

    private static final int ZOOM_DEFAULT = 15;
    private static final int REQUEST_PERMISSOES = 1;
    private static final int REQUEST_ERRO_PLAY_SERVICES = 5;
    private static final int REQUEST_CHECK_SETTINGS = 6;
    private static final String KEY_POSICAO_CAMERA = "posicao_camera";
    private static final String KEY_LOCALIZACAO = "localizacao";
    private static final int M_MAX_ENTRIES = 5;
    private boolean mPermissaoLocalizacaoConcedida = false;
    private boolean bound = false;

    private Location ultimaLocalizacao;
    private LocationRequest locationRequest;

    private static Activity activity;
    private Toolbar toolbar;
    private Fragment visualizacaoMapaFragment;
    private FragmentContainerView fragmentContainerView;
    private FragmentContainerView notificacoesFragmentContainerView;
    private DrawerLayout drawerLayout;
    private NavigationView menuLateralNavigationView;
    private ActionBarDrawerToggle drawerToggle;
    private TextView nenhumVinculoTextView;
    private TextView nenhumVinculoTelaVinculosTextView;
    private int opcaoSelecionada;

    private SharedPreferencesDAO sharedPreferencesDAO;
    private LocalizacaoDAO localizacaoDAO;
    private VinculoDAO vinculoDAO;
    private UsuarioDAO usuarioDAO;
    private Funcoes funcoes;

    private TelaInicioResponsavelFragment telaInicioResponsavelFragment;
    private TelaNotificacoesResponsavelFragment telaNotificacoesResponsavelFragment;
    private TelaRotasFragment telaRotasFragment;
    private TelaVinculosResponsavelFragment telaVinculosResponsavelFragment;
    private MenuItem pesquisaMenuItem;
    private SearchView searchView;
    private Menu menuToolbar;
    private FragmentManager fragmentManager;
    private int quantidadeVinculos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TELAPRINCUSRRESPONSAVEL", "ON_CREATE");
        setContentView(R.layout.activity_tela_principal_usuario_responsavel);
        toolbar = (Toolbar) findViewById(R.id.toolbar_tela_principal_usuario_responsavel);
        setSupportActionBar(toolbar);

        MapesApplication mapesApplication = (MapesApplication) getApplication();
        getApplication().registerActivityLifecycleCallbacks(mapesApplication);

        sharedPreferencesDAO = new SharedPreferencesDAO(this);
        localizacaoDAO = new LocalizacaoDAO();
        usuarioDAO = new UsuarioDAO();
        vinculoDAO = new VinculoDAO();
        funcoes = new Funcoes();
        usuario = sharedPreferencesDAO.getUsuarioLogado();
        fragmentManager = getSupportFragmentManager();

        //fragmentContainerView = findViewById(R.id.fragment_view_tela_principal_usuario_responsavel);
        //notificacoesFragmentContainerView = findViewById(R.id.notificacoes_lista_fragment);
        nenhumVinculoTextView = (TextView) findViewById(R.id.nenhum_vinculo_adicionado);
        //
        ajustarFragmentSePossuiVinculos();

        //telaInicioResponsavelFragment = TelaInicioResponsavelFragment.novaInstancia();
        //getSupportFragmentManager().beginTransaction().add(R.id.layout_fragments_tela_principal_usuario_responsavel, telaInicioResponsavelFragment).commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_menu_lateral_tela_principal_usuario_responsavel);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        menuLateralNavigationView = (NavigationView)
                findViewById(R.id.navigation_view_menu_lateral_usuario_responsavel);
        menuLateralNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selecionarOpcaoMenuLateral(menuItem);
                return true;
            }
        });

        if(savedInstanceState != null){
            opcaoSelecionada = savedInstanceState.getInt("menuItem");
            ultimaLocalizacao = savedInstanceState.getParcelable(KEY_LOCALIZACAO);
            posicaoCamera = savedInstanceState.getParcelable(KEY_POSICAO_CAMERA);
        } else {
            opcaoSelecionada = R.id.opcao_inicio_menu_usuario_responsavel;
        }

        if(opcaoSelecionada != 0) {
            selecionarOpcaoMenuLateral(menuLateralNavigationView.getMenu().findItem(opcaoSelecionada));
        }

        activity = this;
        iniciarGooglePlayServices();
        carregarInformacoesMenuLateral();
        recuperarFirebaseToken();
    }

    private void ajustarFragmentSePossuiVinculos(){

        vinculoDAO.getVinculosCollectionReference().whereEqualTo("uidUsuarioResponsavel", usuario.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                quantidadeVinculos = queryDocumentSnapshots.getDocuments().size();
                if(quantidadeVinculos > 0){
                    telaInicioResponsavelFragment = TelaInicioResponsavelFragment.novaInstancia();
                    fragmentManager.beginTransaction().replace(R.id.layout_fragments_tela_principal_usuario_responsavel,
                            telaInicioResponsavelFragment).commit();
                    //fragmentContainerView = findViewById(R.id.fragment_view_tela_principal_usuario_responsavel);
                    //notificacoesFragmentContainerView = findViewById(R.id.notificacoes_lista_fragment);
                    nenhumVinculoTextView.setVisibility(View.GONE);
                } else {
                    //fragmentManager.beginTransaction().detach()
                    removerFragment();
                    nenhumVinculoTextView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void selecionarOpcaoMenuLateral(MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
        nenhumVinculoTextView.setVisibility(View.GONE);

        removerFragment();

        if(menuItem.getItemId() != opcaoSelecionada) {
            Log.d("OPCAO_JA_SELE", opcaoSelecionada + "");
            Log.d("MENU_SELEC", menuItem.getItemId() + "");
            opcaoSelecionada = menuItem.getItemId();
            pesquisaMenuItem.setVisible(false);

            switch (opcaoSelecionada){
                case R.id.opcao_inicio_menu_usuario_responsavel:
                    toolbar.setTitle(getString(R.string.app_name));
                    ajustarFragmentSePossuiVinculos();
                    break;

                case R.id.opcao_notificacoes:
                    toolbar.setTitle(getString(R.string.nome_tela_notificacoes));
                    telaNotificacoesResponsavelFragment = TelaNotificacoesResponsavelFragment.novaInstancia();
                    fragmentManager.beginTransaction().replace(R.id.layout_fragments_tela_principal_usuario_responsavel,
                            telaNotificacoesResponsavelFragment).commit();
                    break;

                case R.id.opcao_rotas:
                    toolbar.setTitle(getString(R.string.nome_tela_rotas));
                    telaRotasFragment = TelaRotasFragment.novaInstancia();
                    fragmentManager.beginTransaction()
                            .replace(R.id.layout_fragments_tela_principal_usuario_responsavel, telaRotasFragment)
                            .commit();
                    break;

                case R.id.opcao_vinculos_menu_usuario_responsavel:
                    toolbar.setTitle(getString(R.string.nome_tela_vinculos));
                    telaVinculosResponsavelFragment = TelaVinculosResponsavelFragment.novaInstancia(usuario);
                    fragmentManager.beginTransaction().replace(R.id.layout_fragments_tela_principal_usuario_responsavel,
                            telaVinculosResponsavelFragment).commit();
                    telaVinculosResponsavelFragment.setFecharSearchViewListener(new TelaVinculosResponsavelFragment.FecharSearchView() {
                        @Override
                        public void fechar() {
                            Log.d("FECHOU", "searchview");
                            searchView.onActionViewCollapsed();
                            pesquisaMenuItem.collapseActionView();
                        }
                    });
                    instaciarSearchView();
                    break;
            }
        }
    }

    private void removerFragment(){
        switch (opcaoSelecionada){
            case R.id.opcao_inicio_menu_usuario_responsavel:
                if(telaInicioResponsavelFragment != null){
                    fragmentManager.beginTransaction().detach(telaInicioResponsavelFragment).commit();
                }
                break;
            case R.id.opcao_notificacoes:
                fragmentManager.beginTransaction().detach(telaNotificacoesResponsavelFragment).commit();
                break;
            case R.id.opcao_rotas:
                fragmentManager.beginTransaction().detach(telaRotasFragment).commit();
                break;
            case R.id.opcao_vinculos_menu_usuario_responsavel:
                fragmentManager.beginTransaction().detach(telaVinculosResponsavelFragment).commit();
                break;
        }

    }

    private void instaciarSearchView(){

        pesquisaMenuItem.setVisible(true);
        pesquisaMenuItem.setOnActionExpandListener(this);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menuToolbar.findItem(R.id.pesquisa_vinculos).getActionView();
        //searchView.onActionViewCollapsed();
        searchView.setQueryHint(getResources().getString(R.string.pesquisa_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onBackPressed() {
        if(opcaoSelecionada != R.id.opcao_inicio_menu_usuario_responsavel){
            selecionarOpcaoMenuLateral(menuLateralNavigationView.getMenu().findItem(R.id.opcao_inicio_menu_usuario_responsavel));
        } else {
            finish();
        }
    }


    @Override
    public void onPostCreate(Bundle savedInstance, PersistableBundle persistableBundle){
        super.onPostCreate(savedInstance, persistableBundle);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        menuToolbar = menu;
        getMenuInflater().inflate(R.menu.menu_toolbar_tela_vinculos, menuToolbar);
        pesquisaMenuItem = menuToolbar.findItem(R.id.pesquisa_vinculos);
        pesquisaMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String pesquisa) {
        Log.d("Pesquisa", pesquisa);
        if(!pesquisa.isEmpty()){
            try {
                telaVinculosResponsavelFragment.pesquisarUsuarioAluno(pesquisa);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            telaVinculosResponsavelFragment.resetarPesquisa();
        }
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        searchView.onActionViewExpanded();
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        //InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchView.onActionViewCollapsed();
        return true;
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
            parametros.putInt("menuItem", opcaoSelecionada);
            parametros.putParcelable(Constantes.KEY_USUARIO_EXTRA, usuario);
            super.onSaveInstanceState(parametros);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle parametros) {
        super.onSaveInstanceState(parametros);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TELAPRINCIPALUSRESPONSAVEL", "ON_RESUME");
        Log.d("PermissaLocalizacao", mPermissaoLocalizacaoConcedida+"");

        //inicializarGoogleMaps();
    }

    /*

    class ConexaoReceiver extends BroadcastReceiver {
        private boolean primeiraVez = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(primeiraVez){
                primeiraVez = false;
                return;
            }
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                animarCameraMapa(ultimaLocalizacao);
            }
        }
    }

    private void inicializarGoogleMaps() {
        mapaFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_view_tela_principal_usuario_responsavel);
        mapaFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        Log.d("GOOGLE_MAP", googleMap.toString());
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuario.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);
                    localizacaoDAO.getLocalizacoesCollectionReference()
                            .document(vinculo.getUidUsuarioAluno())
                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    Localizacao localizacao = documentSnapshot.toObject(Localizacao.class);

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(localizacao.getLatitude(),
                                                    localizacao.getLongitude()))
                                            .zoom(17)
                                            .build();
                                    mGoogleMap
                                            .animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    mGoogleMap.clear();
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromBitmap(funcoes.getBitmapFromVector(getBaseContext(), R.drawable.ic_marcador)))
                                            .position(new LatLng(localizacao.getLatitude(),
                                                    localizacao.getLongitude())));
                                }
                            });
                }
            }
        });
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

    private void criarSolicitacoesDeLocalizacao() {
        Log.d("LOCATION_REQUEST", "Criando location request");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .addLocationRequest(locationRequest);

        final SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //mPermissaoLocalizacaoConcedida = true;
                atualizarLocalizacao();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        Log.d("LOCATION_REQUEST", "GPS DESATIVADO");
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(activity,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        e.printStackTrace();
                        Log.d("LOCATION_REQUEST", "ERRO COM PROBLEMA DE PERMISSÃO");
                    }
                }
            }
        });
    }

    private void atualizarMapa(){
        geoFenceInfo = geoFenceDB.getGeofence("1");
        if(geoFenceInfo != null){
            LatLng posicao = new LatLng(geoFenceInfo.getLatitude(), geoFenceInfo.getLongitude());
            mGoogleMap.addCircle(new CircleOptions()
                    .strokeWidth(2)
                    .fillColor(0x9981D0EB)
                    .center(posicao)
                    .radius(geoFenceInfo.getRaio()));
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
                atualizarLocalizacao();
            } else {
                Log.d("LOCATION_REQUEST", "usuario não aceitou");
                criarSolicitacoesDeLocalizacao();
            }
        }
    }

    private void atualizarLocalizacao(){
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
                Task<Location> taskRecuperarLocalizacao = mFusedLocationProviderClient.getLastLocation();
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

        /*
        Log.d("VERSAO ANDROID", Build.VERSION.SDK_INT + "");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean permissaoConcedidaAcessoLocalizacaoEmSegundoPlano =
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
            Log.d("permissaoConcBG",
                    permissaoConcedidaAcessoLocalizacaoEmSegundoPlano +"");
            if (!permissaoConcedidaAcessoLocalizacaoEmSegundoPlano) {
                permissoes.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
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
        }
    } */


    private void recuperarFirebaseToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        final String firebaseTokenAtualizado = task.getResult().getToken();
                        //usuario.setFirebaseToken(firebaseTokenAtualizado);
                        sharedPreferencesDAO.atualizarFirebaseTokenUsuarioLogado(firebaseTokenAtualizado);
                        Log.d(TAG, firebaseTokenAtualizado);
                    }
                });
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
