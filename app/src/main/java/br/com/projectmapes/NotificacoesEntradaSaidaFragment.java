package br.com.projectmapes;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.dao.NotificacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.ItemNotificacaoViewHolder;
import br.com.projectmapes.suporte.NotificacaoRecyclerViewAdapter;

public class NotificacoesEntradaSaidaFragment extends Fragment implements NotificacaoRecyclerViewAdapter.OnClickListener {

    private static final String TAG_DETALHE_NOTIFICACAO = "tagDetalheNotificacao";
    private static final String EXTRA_NOTIFICACAO = "Notificacao";

    private NotificacaoMovimentacaoGeofence notificacao;
    private RecyclerView notificacoesRecyclerView;
    private TextView nenhumaNotificacaoTextView;
    private List<ItemNotificacaoViewHolder> notificacoes;
    private NotificacoesDownloadTask notificacoesDownloadTask;
    private ProgressBar carregandoNotificacoesProgressbar;

    private NotificacaoDAO notificacaoDAO;
    private VinculoDAO vinculoDAO;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private UsuarioDAO usuarioDAO;
    private Usuario usuario;

    List<ItemNotificacaoViewHolder> itensNotificacaoViewHolder;

    public NotificacoesEntradaSaidaFragment() {
    }

    public static NotificacoesEntradaSaidaFragment novaInstancia(NotificacaoMovimentacaoGeofence notificacao) {
        NotificacoesEntradaSaidaFragment fragment = new NotificacoesEntradaSaidaFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NOTIFICACAO, notificacao);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
        usuario = sharedPreferencesDAO.getUsuarioLogado();

        if (getArguments() != null) {
            notificacao = (NotificacaoMovimentacaoGeofence) getArguments().getSerializable(EXTRA_NOTIFICACAO);
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout =  inflater.inflate(R.layout.fragment_notificacoes_entrada_saida, container, false);

        notificacoesRecyclerView = (RecyclerView) layout.findViewById(R.id.notificacoes_recyclerview_tela_inicio_usuario_responsavel);
        nenhumaNotificacaoTextView = (TextView) layout.findViewById(R.id.nenhuma_notificacao_recebida_textview);
        carregandoNotificacoesProgressbar = layout.findViewById(R.id.carregando_notificacoes_progress_bar);

        notificacoesRecyclerView.setHasFixedSize(true);
        notificacoesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificacoesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance){
        super.onActivityCreated(savedInstance);

        if(notificacoes == null) {
            if(notificacoesDownloadTask == null) {
                notificacoesDownloadTask = new NotificacoesDownloadTask();
                notificacoesDownloadTask.execute();
            } else if (notificacoesDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
                exibirProgresso();
            }
        } else {
            atualizarLista(notificacoes);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(notificacoesDownloadTask != null) {
            notificacoesDownloadTask.cancel(true);
        }
    }


    private void atualizarLista(List<ItemNotificacaoViewHolder> notificacoes) {
        NotificacaoRecyclerViewAdapter adapter = new NotificacaoRecyclerViewAdapter(getActivity(), notificacoes);
        notificacoesRecyclerView.setAdapter(adapter);
    }

    private void exibirProgresso() {
        carregandoNotificacoesProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void selecionarNotificacao(View view, int posicao, ItemNotificacaoViewHolder notificacao) {

    }

    private class NotificacoesDownloadTask extends AsyncTask<Void, Void, List<ItemNotificacaoViewHolder>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            exibirProgresso();
        }

        @Override
        protected List<ItemNotificacaoViewHolder> doInBackground(Void... params) {
            notificacaoDAO = new NotificacaoDAO();
            vinculoDAO = new VinculoDAO();
            usuarioDAO = new UsuarioDAO();
            itensNotificacaoViewHolder = new ArrayList<ItemNotificacaoViewHolder>();

            vinculoDAO.getVinculosCollectionReference()
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
                                                carregandoNotificacoesProgressbar.setVisibility(View.GONE);
                                                if(queryDocumentSnapshots.getDocuments().size() == 0){
                                                    nenhumaNotificacaoTextView.setVisibility(View.VISIBLE);
                                                } else {
                                                    nenhumaNotificacaoTextView.setVisibility(View.GONE);
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
                                                    notificacoesRecyclerView.setAdapter(adapter);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
            return notificacoes;
        }
    }

}
