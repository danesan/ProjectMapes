package br.com.projectmapes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import br.com.projectmapes.dao.LocalizacaoDAO;
import br.com.projectmapes.dao.NotificacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.Localizacao;
import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.ItemNotificacaoViewHolder;
import br.com.projectmapes.suporte.NotificacaoRecyclerViewAdapter;

public class TelaInicioResponsavelFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment mapaFragmentContainerView;
    private FragmentContainerView notificacoesFragmentContainerView;

    private GoogleMap mGoogleMap;

    private VinculoDAO vinculoDAO;
    private NotificacaoDAO notificacaoDAO;
    private LocalizacaoDAO localizacaoDAO;
    private UsuarioDAO usuarioDAO;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private Usuario usuario;
    private Localizacao localizacaoAtualUsuarioAluno;

    private RecyclerView recyclerView;
    private List<ItemNotificacaoViewHolder> itensNotificacaoViewHolder;

    private Funcoes funcoes;

    public TelaInicioResponsavelFragment() {
        // Required empty public constructor
    }

    public static TelaInicioResponsavelFragment novaInstancia() {
        TelaInicioResponsavelFragment fragment = new TelaInicioResponsavelFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.fragment_tela_inicio_responsavel, container, false);

        mapaFragmentContainerView = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragment_view_tela_principal_usuario_responsavel);
        notificacoesFragmentContainerView = layout.findViewById(R.id.notificacoes_lista_fragment);
        recyclerView = layout.findViewById(R.id.notificacoes_recyclerview_tela_inicio_usuario_responsavel);

        mapaFragmentContainerView.getMapAsync(this);

        sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
        vinculoDAO = new VinculoDAO();
        notificacaoDAO = new NotificacaoDAO();
        localizacaoDAO = new LocalizacaoDAO();
        usuarioDAO = new UsuarioDAO();
        itensNotificacaoViewHolder = new ArrayList<ItemNotificacaoViewHolder>();

        funcoes = new Funcoes();

        usuario = sharedPreferencesDAO.getUsuarioLogado();

/*        vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuario.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);

                            usuarioDAO.getUsuarioCollectionReference()
                                    .document(vinculo.getUidUsuarioAluno())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);

                                    notificacaoDAO.getMovimentacoesGeofenceCollectionReference()
                                            .document(usuarioAluno.getUid())
                                            .collection("movimentacoes_salvas")
                                            .orderBy("dataHora", Query.Direction.DESCENDING)
                                            .limit(1)
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            MovimentacaoGeofence movimentacaoGeofence = queryDocumentSnapshots.getDocuments().get(0)
                                                    .toObject(MovimentacaoGeofence.class);
                                            Log.d("movim_geofence", movimentacaoGeofence.toString());
                                            ItemNotificacaoViewHolder itemNotificacaoViewHolder =
                                                    new ItemNotificacaoViewHolder(usuarioAluno.getFotoURL(),
                                                            usuarioAluno.getLogin(),
                                                            movimentacaoGeofence.getDataHora(),
                                                            movimentacaoGeofence.getAcao());

                                            itensNotificacaoViewHolder.add(itemNotificacaoViewHolder);

                                            NotificacaoRecyclerViewAdapter adapter =
                                                    new NotificacaoRecyclerViewAdapter(getContext(),
                                                            itensNotificacaoViewHolder);
                                            //adapter.setOnClickListener(this);
                                            recyclerView.setAdapter(adapter);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });*/

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
                                    localizacaoAtualUsuarioAluno = documentSnapshot.toObject(Localizacao.class);

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(localizacaoAtualUsuarioAluno.getLatitude(),
                                                    localizacaoAtualUsuarioAluno.getLongitude()))
                                            .zoom(17)
                                            .build();
                                    mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                                    mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
                                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    mGoogleMap
                                            .animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    mGoogleMap.clear();
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromBitmap(funcoes.getBitmapFromVector(getContext(), R.drawable.ic_marcador)))
                                            .position(new LatLng(localizacaoAtualUsuarioAluno.getLatitude(),
                                                    localizacaoAtualUsuarioAluno.getLongitude())));
                                }
                            });
                }
            }
        });
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

}