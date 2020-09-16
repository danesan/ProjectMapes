package br.com.projectmapes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.projectmapes.dao.NotificacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Funcoes;
import br.com.projectmapes.suporte.ItemNotificacaoViewHolder;
import br.com.projectmapes.suporte.NotificacaoRecyclerViewAdapter;
import br.com.projectmapes.suporte.UsuarioAlunoMenuDropDownAdapter;


public class TelaNotificacoesResponsavelFragment extends Fragment {

    private TextInputLayout usuarioAlunoMenuDropDownTextInputLayout;
    private AutoCompleteTextView usuarioAlunoAutoCompleteTextView;
    private TextInputLayout dataInicialTextInputLayout;
    private TextInputLayout dataFinalTextInputLayout;
    private Button dataInicialDatePickerButton;
    private Button dataFinalDatePickerButton;
    private Button consultarButton;
    private TextView erroTodosCamposObrigatoriosTextView;
    private Usuario usuarioResponsavel;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView nenhumRegistroEditText;
    private ProgressBar progressBar;

    private VinculoDAO vinculoDAO;
    private UsuarioDAO usuarioDAO;

    private ArrayList<Usuario> usuariosAluno;
    private Usuario usuarioAlunoSelecionado;
    private UsuarioAlunoMenuDropDownAdapter adapter;

    private int quantVinculos = 0 ;
    private int quantUsuarios = 0;

    public TelaNotificacoesResponsavelFragment() {
        // Required empty public constructor
    }

    public static TelaNotificacoesResponsavelFragment novaInstancia() {
        TelaNotificacoesResponsavelFragment fragment = new TelaNotificacoesResponsavelFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tela_notificacoes_responsavel, container, false);

        SharedPreferencesDAO sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
        usuarioResponsavel = sharedPreferencesDAO.getUsuarioLogado();

        usuarioAlunoMenuDropDownTextInputLayout = (TextInputLayout) layout.findViewById(R.id.usuario_aluno_menu_dropdown_tela_notificacoes);
        usuarioAlunoAutoCompleteTextView = (AutoCompleteTextView) layout.findViewById(R.id.login_aluno_autocompletetextview_tela_notificacoes);
        dataInicialTextInputLayout = (TextInputLayout) layout.findViewById(R.id.data_inicial_text_view_tela_notificacoes_usuario_responsavel);
        dataFinalTextInputLayout = (TextInputLayout) layout.findViewById(R.id.data_final_text_view_tela_notificacoes_usuario_responsavel);
        dataInicialDatePickerButton = (Button) layout.findViewById(R.id.data_inicial_date_picker_tela_notificacoes_usuario_responsavel);
        dataFinalDatePickerButton = (Button) layout.findViewById(R.id.data_final_date_picker_tela_notificacoes_usuario_responsavel);
        consultarButton = (Button) layout.findViewById(R.id.consultar_button_tela_notificacoes_usuario_responsavel);
        erroTodosCamposObrigatoriosTextView = (TextView) layout.findViewById(R.id.erro_todos_campos_obrigatorios_text_view_tela_notificacoes);
        progressBar = (ProgressBar) layout.findViewById(R.id.carregando_notificacoes_tela_notificacoes_progress_bar);
        //swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.lista_notificacoes_swipe_refresh);
        recyclerView = (RecyclerView) layout.findViewById(R.id.notificacoes_recycler_view);
        nenhumRegistroEditText = (TextView) layout.findViewById(R.id.sem_registro_encontrado_edit_text);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        vinculoDAO = new VinculoDAO();
        usuarioDAO = new UsuarioDAO();
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

        dataInicialDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePickerFragment = new DatePickerFragment(dataInicialTextInputLayout);
                datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });
        dataFinalDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePickerFragment = new DatePickerFragment(dataFinalTextInputLayout);
                datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        consultarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean temErro = false;

                if(usuarioAlunoSelecionado == null){
                    temErro = true;
                    usuarioAlunoMenuDropDownTextInputLayout.setError(" ");
                } else {
                    usuarioAlunoMenuDropDownTextInputLayout.setError(null);
                }
                if(dataInicialTextInputLayout.getEditText().getText().toString().isEmpty()){
                    temErro = true;
                    dataInicialTextInputLayout.setError(" ");
                } else {
                    dataInicialTextInputLayout.setError(null);
                }
                if(dataFinalTextInputLayout.getEditText().getText().toString().isEmpty()){
                    temErro = true;
                    dataFinalTextInputLayout.setError(" ");
                } else {
                    dataFinalTextInputLayout.setError(null);
                }

                if (temErro) {
                    erroTodosCamposObrigatoriosTextView.setVisibility(View.VISIBLE);
                } else {
                    erroTodosCamposObrigatoriosTextView.setVisibility(View.GONE);
                    nenhumRegistroEditText.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    pesquisarNotificacoes(dataInicialTextInputLayout.getEditText().getText().toString(),
                            dataFinalTextInputLayout.getEditText().getText().toString());
                }
            }
        });

        //swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        //    @Override
        //    public void onRefresh() {
        //        Log.d("Refresh", "swipe");
        //        swipeRefreshLayout.setRefreshing(false);
        //    }
        //});
        //nenhumRegistroEditText.setVisibility(View.GONE);

        return layout;
    }

    private void pesquisarNotificacoes(final String dataInicial, final String dataFinal){
        VinculoDAO vinculoDAO = new VinculoDAO();
        final UsuarioDAO usuarioDAO = new UsuarioDAO();
        final NotificacaoDAO notificacaoDAO = new NotificacaoDAO();
        final Funcoes funcoes = new Funcoes();
        final List<ItemNotificacaoViewHolder> itensNotificacaoViewHolder = new ArrayList<>();

        //vinculoDAO.getVinculosCollectionReference()
        //.whereEqualTo("uidUsuarioResponsavel", usuarioResponsavel.getUid())
        //.get()
        //.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        //@Override
        //public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

        //if(queryDocumentSnapshots.getDocuments().isEmpty()){
        //    nenhumRegistroEditText.setVisibility(View.VISIBLE);
        //    swipeRefreshLayout.setRefreshing(false);
        //} else {
        //for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
        //Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);

        //usuarioDAO.getUsuarioCollectionReference()
        //.document(vinculo.getUidUsuarioAluno())
        //.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        //@Override
        //public void onSuccess(DocumentSnapshot documentSnapshot) {
        //final Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);

        notificacaoDAO.getMovimentacoesGeofenceCollectionReference()
                //.document(usuarioAluno.getUid())
                .document(usuarioAlunoSelecionado.getUid())
                .collection("movimentacoes_salvas")
                .whereGreaterThanOrEqualTo("dataHora", funcoes.converterDataStringEmMilisegundos(dataInicial))
                .whereLessThanOrEqualTo("dataHora", funcoes.converterDataStringEmMilisegundos(dataFinal))
                .orderBy("dataHora", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressBar.setVisibility(View.GONE);

                if(queryDocumentSnapshots.getDocuments().isEmpty()){
                    nenhumRegistroEditText.setVisibility(View.VISIBLE);
                } else {
                    nenhumRegistroEditText.setVisibility(View.GONE);

                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                        MovimentacaoGeofence movimentacaoGeofence = documentSnapshot.toObject(MovimentacaoGeofence.class);
                        Log.d("movim_geofence", movimentacaoGeofence.toString());
                        ItemNotificacaoViewHolder itemNotificacaoViewHolder =
                                new ItemNotificacaoViewHolder(usuarioAlunoSelecionado.getFotoURL(),
                                        usuarioAlunoSelecionado.getLogin(),
                                        movimentacaoGeofence.getDataHora(),
                                        movimentacaoGeofence.getAcao());

                        itensNotificacaoViewHolder.add(itemNotificacaoViewHolder);
                    }

                    NotificacaoRecyclerViewAdapter adapter =
                            new NotificacaoRecyclerViewAdapter(getContext(),
                                    itensNotificacaoViewHolder);
                    //adapter.setOnClickListener(this);
                    recyclerView.setAdapter(adapter);
                }
                //swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
//});
    //}
    //}
    //}
    //});
//}


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

            if(dia < 10) {
                diaString = "0" + diaString;
            }
            if(mes < 10) {
                mesString = "0" + mesString;
            }
            dataTextInputLayout.getEditText().setText(diaString + "/" + mesString +"/" + ano);
        }
    }

}
