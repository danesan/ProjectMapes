package br.com.projectmapes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import br.com.projectmapes.dao.LocalizacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.Localizacao;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.MaskEditTextUtil;
import br.com.projectmapes.suporte.UsuarioAlunoMenuDropDownAdapter;

public class TelaRotasFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextInputLayout usuarioAlunoMenuDropDown;
    private AutoCompleteTextView usuarioAlunoAutoCompleteTextView;
    private TextInputLayout dataTextInputLayout;
    private Button dataButton;
    private TextInputLayout horarioInicialTextInputLayout;
    private Button horarioInicialButton;
    private TextInputLayout horarioFinalTextInputLayout;
    private Button horarioFinalButton;
    private Button consultarButton;
    private TextView erroTodosCamposObrigatoriosTextView;

    private LocalizacaoDAO localizacaoDAO;
    private VinculoDAO vinculoDAO;
    private UsuarioDAO usuarioDAO;
    private SharedPreferencesDAO sharedPreferencesDAO;

    private ArrayList<Usuario> usuariosAluno;
    private Usuario usuarioAlunoSelecionado;
    private UsuarioAlunoMenuDropDownAdapter adapter;

    private Funcoes funcoes;

    private int quantVinculos = 0 ;
    private int quantUsuarios = 0;

    public TelaRotasFragment() {
        // Required empty public constructor
    }

    public static TelaRotasFragment novaInstancia() {
        TelaRotasFragment fragment = new TelaRotasFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_tela_rotas, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapa_rota_fragment_view_tela_principal_usuario_responsavel);
        mapFragment.getMapAsync(this);

        usuarioAlunoMenuDropDown = layout.findViewById(R.id.usuario_menu_dropdown);
        usuarioAlunoAutoCompleteTextView = layout.findViewById(R.id.login_aluno_autocompletetextview);
        dataTextInputLayout = (TextInputLayout) layout.findViewById(R.id.data_text_view_tela_rotas_usuario_responsavel);
        dataButton = (Button) layout.findViewById(R.id.data_date_picker_tela_rotas);
        horarioInicialTextInputLayout = (TextInputLayout) layout.findViewById(R.id.horario_inicial_text_view_tela_rotas_usuario_responsavel);
        horarioInicialButton = (Button) layout.findViewById(R.id.horario_inicial_time_picker_tela_rotas_usuario_responsavel);
        horarioFinalTextInputLayout = (TextInputLayout) layout.findViewById(R.id.horario_final_text_view_tela_rotas_usuario_responsavel);
        horarioFinalButton = (Button) layout.findViewById(R.id.horario_final_time_picker_tela_rotas_usuario_responsavel);
        consultarButton = (Button) layout.findViewById(R.id.consultar_button_tela_rotas_usuario_responsavel);
        erroTodosCamposObrigatoriosTextView = (TextView) layout.findViewById(R.id.erro_todos_campos_obrigatorios_text_view);

        localizacaoDAO = new LocalizacaoDAO();
        vinculoDAO = new VinculoDAO();
        usuarioDAO = new UsuarioDAO();
        sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
        funcoes = new Funcoes();

        usuariosAluno = new ArrayList<>();

        vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", sharedPreferencesDAO.getUsuarioLogado().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);
                    Log.d("VINCULO_ENCONTRADO", vinculo.toString());

                    quantVinculos = queryDocumentSnapshots.getDocuments().size();

                    usuarioDAO.getUsuarioCollectionReference()
                            .document(vinculo.getUidUsuarioAluno())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);
                            usuariosAluno.add(usuarioAluno);

                            quantUsuarios++;

                            Log.d("USUARIO_ENCONTRADO", usuarioAluno.toString());

                            if(quantUsuarios == quantVinculos) {
                                adapter = new UsuarioAlunoMenuDropDownAdapter(getContext(), usuariosAluno);
                                usuarioAlunoAutoCompleteTextView.setAdapter(adapter);

                                adapter.setOnItemUsuarioClickListener(new UsuarioAlunoMenuDropDownAdapter.OnItemUsuarioClickListener() {
                                    @Override
                                    public void selecionarUsuario(Usuario usuarioAluno) {
                                        Log.d("CLICOU", "clicuou");
                                        usuarioAlunoAutoCompleteTextView.setText(usuarioAluno.getLogin());
                                        usuarioAlunoAutoCompleteTextView.dismissDropDown();
                                        usuarioAlunoSelecionado = usuarioAluno;
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        /*dataTextInputLayout.getEditText().addTextChangedListener(MaskEditTextUtil.mask(dataTextInputLayout.getEditText(),
                MaskEditTextUtil.FORMAT_DATE));

        horarioInicialTextInputLayout.getEditText().addTextChangedListener(MaskEditTextUtil.mask(horarioInicialTextInputLayout.getEditText(),
                MaskEditTextUtil.FORMAT_HOUR));

        horarioFinalTextInputLayout.getEditText().addTextChangedListener(MaskEditTextUtil.mask(horarioFinalTextInputLayout.getEditText(),
                MaskEditTextUtil.FORMAT_HOUR));
*/
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dataDialogFragment = new DatePickerFragment(dataTextInputLayout);
                dataDialogFragment.show(getActivity().getSupportFragmentManager(), "datPicker");
            }
        });

        horarioInicialButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                DialogFragment horarioDialogFragment = new TimePickerFragment(horarioInicialTextInputLayout,
                        horarioFinalTextInputLayout, horarioFinalButton);
                horarioDialogFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        horarioFinalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment horarioDialogFragment = new TimePickerFragment(horarioFinalTextInputLayout);
                horarioDialogFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        consultarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean temErro = false;

                if (usuarioAlunoSelecionado == null) {
                    temErro = true;
                    usuarioAlunoMenuDropDown.setError(" ");
                } else {
                    usuarioAlunoMenuDropDown.setError(null);
                }
                if (dataTextInputLayout.getEditText().getText().toString().isEmpty()) {
                    temErro = true;
                    dataTextInputLayout.setError(" ");
                } else {
                    dataTextInputLayout.setError(null);
                }
                if (horarioInicialTextInputLayout.getEditText().getText().toString().isEmpty()) {
                    temErro = true;
                    horarioInicialTextInputLayout.setError(" ");
                } else {
                    horarioInicialTextInputLayout.setError(null);
                }
                if (horarioFinalTextInputLayout.getEditText().getText().toString().isEmpty()) {
                    temErro = true;
                    horarioFinalTextInputLayout.setError(" ");
                } else {
                    horarioFinalTextInputLayout.setError(null);
                }

                if (temErro) {
                    erroTodosCamposObrigatoriosTextView.setVisibility(View.VISIBLE);
                } else {
                    erroTodosCamposObrigatoriosTextView.setVisibility(View.GONE);

                    Calendar dataHoraInicial = Calendar.getInstance();
                    Calendar dataHoraFinal = Calendar.getInstance();

                    dataHoraInicial.setTimeInMillis(
                            funcoes.converterDataHorarioStringEmMilisegundos(
                                    dataTextInputLayout.getEditText().getText().toString(),
                                    horarioInicialTextInputLayout.getEditText().getText().toString()));

                    dataHoraFinal.setTimeInMillis(
                            funcoes.converterDataHorarioStringEmMilisegundos(
                                    dataTextInputLayout.getEditText().getText().toString(),
                                    horarioFinalTextInputLayout.getEditText().getText().toString()));

                    //Log.d("Usuario_Selecionado", usuarioAlunoSelecionado.toString());
                    consultarRotas(usuarioAlunoSelecionado, dataHoraInicial.getTimeInMillis(), dataHoraFinal.getTimeInMillis());
                }
            }
        });

        return layout;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng ifam = new LatLng(Constantes.KEY_LATITUDE_IFAM, Constantes.KEY_LONGITUDE_IFAM);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(ifam)
                .zoom(18)
                .build();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void consultarRotas(Usuario usuarioAluno, long dataHoraInicial, long dataHoraFinal){
        Log.d("DATA_HORA_INICIAL", dataHoraInicial + "");
        Log.d("DATA_HORA_FINAL", dataHoraFinal + "");
        localizacaoDAO.getLocalizacoesCollectionReference().document(usuarioAluno.getUid())
                .collection("localizacoes_salvas")
                .whereGreaterThanOrEqualTo("dataHora", dataHoraInicial)
                .whereLessThanOrEqualTo("dataHora", dataHoraFinal)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                PolylineOptions polylineOptions = new PolylineOptions();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    Localizacao localizacao = documentSnapshot.toObject(Localizacao.class);
                    Log.d("Localizacao", funcoes.converterDataMilisegundosEmString(localizacao.getDataHora())
                            + " " + funcoes.converterHorarioMilisegundosEmString(localizacao.getDataHora()));
                    polylineOptions.add(new LatLng(localizacao.getLatitude(), localizacao.getLongitude()));
                }
                mMap.clear();
                Polyline polyline = mMap.addPolyline(polylineOptions);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_seta_inicio_rota);
                polyline.setStartCap(new CustomCap(bitmapDescriptor, 10));
                polyline.setEndCap(new RoundCap());
                polyline.setColor(0xff000000);
                polyline.setJointType(JointType.ROUND);
            }
        });
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private TextInputLayout horarioTextInputLayout;
        private TextInputLayout horarioFinalTextInputLayout;
        private Button horarioFinalButton;
        private boolean habilitarHorarioFim;

        public TimePickerFragment(TextInputLayout horarioTextInputLayout) {
            this.horarioTextInputLayout = horarioTextInputLayout;
        }

        public TimePickerFragment(TextInputLayout horarioTextInputLayout, TextInputLayout horarioFinalTextInputLayout,
                                  Button horarioFinalButton) {
            this.horarioTextInputLayout = horarioTextInputLayout;
            this.horarioFinalTextInputLayout = horarioFinalTextInputLayout;
            this.horarioFinalButton = horarioFinalButton;

            habilitarHorarioFim = true;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar dataHoje = Calendar.getInstance();
            int horas = dataHoje.get(Calendar.HOUR_OF_DAY);
            int minutos = dataHoje.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, horas, minutos, true);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onTimeSet(TimePicker view, int horas, int minutos) {
            String horasString = horas + "";
            String minutosString = minutos + "";

            if(horas < 10){
                horasString = "0" + horas;
            }
            if(minutos < 10){
                minutosString = "0" + minutos;
            }
            horarioTextInputLayout.getEditText().setText(horasString + ":" + minutosString);

            if(habilitarHorarioFim){
                horarioFinalTextInputLayout.setBoxBackgroundColor(0);
                horarioFinalButton.setEnabled(true);
                //horarioFinalButton.setBackgroundTintList(colorStateList);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private TextInputLayout dataTextInputLayout;

        public DatePickerFragment(TextInputLayout dataTextInputLayout) {
            this.dataTextInputLayout = dataTextInputLayout;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar dataHoje = Calendar.getInstance();
            int year = dataHoje.get(Calendar.YEAR);
            int month = dataHoje.get(Calendar.MONTH);
            int day = dataHoje.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int ano, int mes, int dia) {
            mes++;
            String diaString = dia + "";
            String mesString = mes + "";

            if(dia < 10){
                diaString = "0" + dia;
            }
            if(mes < 10){
                mesString = "0" + mes;
            }

            dataTextInputLayout.getEditText().setText(diaString + "/" + mesString +"/" + ano);
        }
    }

}
