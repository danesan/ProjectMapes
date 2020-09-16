package br.com.projectmapes.suporte;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import br.com.projectmapes.R;

public class VisualizacaoMapaFragment extends Fragment implements OnMapReadyCallback{

    private static final String TAG = VisualizacaoMapaFragment.class.getSimpleName();

    private SupportMapFragment mapaFragment;
    private GoogleMap mGoogleMap;
    private CameraPosition posicaoCamera;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mLocalizacaoDefault = new LatLng(-3.044653,-60.1071907);
    private static final int ZOOM_DEFAULT = 15;
    private static final int PERMISSAO_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mPermissaoLocalizacaoConcedida;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location ultimaLocalizacao;

    private static final String KEY_POSICAO_CAMERA = "posicao_camera";
    private static final String KEY_LOCALIZACAO = "localizacao";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private List[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    public static VisualizacaoMapaFragment novaInstancia(){
        Bundle parametros = new Bundle();
        VisualizacaoMapaFragment visualizacaoMapaFragment = new VisualizacaoMapaFragment();
        visualizacaoMapaFragment.setArguments(parametros);
        return visualizacaoMapaFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);



    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_visualizacao_mapa, container, false);


        return layout;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

    }



}
