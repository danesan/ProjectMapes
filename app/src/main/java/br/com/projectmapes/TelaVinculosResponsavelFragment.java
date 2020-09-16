package br.com.projectmapes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.UsuarioRecyclerViewAdapter;

public class TelaVinculosResponsavelFragment extends Fragment {
    private SharedPreferencesDAO sharedPreferencesDAO;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private UsuarioRecyclerViewAdapter adapter;

    private SolicitacaoVinculoDAO solicitacaoVinculoDAO;
    private VinculoDAO vinculoDAO;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioResponsavelLogado;
    private List<Usuario> usuariosVinculos;
    private List<Usuario> usuariosComSolicitacoesVinculoEnviadas;
    private UsuarioRecyclerViewAdapter.OnItemClickListener onItemClickListener;
    private TextView nenhumVinculoTelaVinculosTextView;

    private FecharSearchView listener;

    public static TelaVinculosResponsavelFragment novaInstancia(Usuario usuarioResponsavelLogado){
        //Bundle params = new Bundle();
        //params.putString(Constantes.EXTRA_TIPO,tipo);
        TelaVinculosResponsavelFragment telaVinculosResponsavelFragment = new TelaVinculosResponsavelFragment(usuarioResponsavelLogado);
        //telaVinculosAlunoFragment.setArguments(params);
        return telaVinculosResponsavelFragment;
    }

    private TelaVinculosResponsavelFragment(Usuario usuarioResponsavelLogado){
        this.usuarioResponsavelLogado = usuarioResponsavelLogado;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tela_vinculos_responsavel,
                container, false);

        usuarioDAO = new UsuarioDAO();
        adapter = new UsuarioRecyclerViewAdapter(getContext());
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        nenhumVinculoTelaVinculosTextView = (TextView) layout.findViewById(R.id.nenhum_vinculo_adicionado_tela_vinculos);

        recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view_usuarios_tela_vinculos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);

        onItemClickListener = new UsuarioRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int posicao, Usuario usuarioAluno) {
                AdicionarVinculoDialogFragment adicionarVinculoDialogFragment =
                        new AdicionarVinculoDialogFragment(usuarioResponsavelLogado, usuarioAluno);
                adicionarVinculoDialogFragment.abrir(getActivity().getSupportFragmentManager());
                adicionarVinculoDialogFragment.setAdicionarVinculoListener(new AdicionarVinculoDialogFragment.AoAdicionarVinculo() {
                    @Override
                    public void adicionarVinculo(Usuario usuario) {
                        adapter.adicionarUsuarioSolicitacaVinculoEnviada(usuario);
                        adapter.notifyDataSetChanged();
                        listener.fechar();
                    }
                });
            }
        };

        listarUsuariosAlunoComQuemTemVinculoESolicitacaoVinculo(usuarioResponsavelLogado);

        SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
        solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuarioResponsavelLogado.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots.getDocumentChanges() != null){
                            for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                                Log.d("EVENTO", documentChange.getType()+ " ");
                                if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                    SolicitacaoVinculo solicitacaoVinculo = documentChange
                                            .getDocument().toObject(SolicitacaoVinculo.class);
                                    Log.d("Solicitacao_vinculo", solicitacaoVinculo.toString());
                                    removerSolicitacaoVinculo(solicitacaoVinculo);
                                }
                            }
                        }
                    }
                });
        return layout;
    }

    public void pesquisarUsuarioAluno(String pesquisa) throws ExecutionException, InterruptedException {
        //String primeiraLetraMaiuscula = pesquisa.substring(0,1).toUpperCase();
        pesquisa = pesquisa.toLowerCase();

        Task<QuerySnapshot> query = usuarioDAO.getUsuarioCollectionReference()
                //.whereEqualTo("perfil", Constantes.USUARIO_ALUNO)
                .orderBy("nome")
                .startAt(pesquisa)
                .endAt(pesquisa + "\uf8ff")
                .get();
        query.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Usuario> usuariosEncontrados = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                    if (!usuario.getLogin().equals(usuarioResponsavelLogado.getLogin())
                            && usuario.getPerfil().equalsIgnoreCase(Constantes.USUARIO_ALUNO)) {
                        usuariosEncontrados.add(usuario);
                    }
                }
                atualizarListaPesaquisaUsuariosAluno(usuariosEncontrados);
            }
        });
    }

    private void listarUsuariosAlunoComQuemTemVinculoESolicitacaoVinculo(Usuario usuarioResponsavel) {
        usuariosVinculos = new ArrayList<>();
        usuariosComSolicitacoesVinculoEnviadas = new ArrayList<>();

        SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
        solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuarioResponsavel.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            SolicitacaoVinculo solicitacaoVinculo = documentSnapshot.toObject(SolicitacaoVinculo.class);

                            usuarioDAO.getUsuarioCollectionReference()
                                    .document(solicitacaoVinculo.getUidUsuarioAluno()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);
                                            usuariosComSolicitacoesVinculoEnviadas.add(usuarioAluno);
                                            adicionarNaListaUsuariosComAlgumTipoSolicitacao(usuarioAluno,
                                                    Constantes.FLAG_USUARIOS_SOLICITACAO_VINCULO);
                                            nenhumVinculoTelaVinculosTextView.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                });

        VinculoDAO vinculoDAO = new VinculoDAO();
        vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuarioResponsavel.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);

                            usuarioDAO.getUsuarioCollectionReference()
                                    .document(vinculo.getUidUsuarioAluno()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);
                                            usuariosVinculos.add(usuarioAluno);
                                            adicionarNaListaUsuariosComAlgumTipoSolicitacao(usuarioAluno,
                                                    Constantes.FLAG_USUARIOS_VINCULO);
                                            nenhumVinculoTelaVinculosTextView.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                });
    }

    public void removerSolicitacaoVinculo(SolicitacaoVinculo solicitacaoVinculo) {
        usuarioDAO.getUsuarioCollectionReference()
                .document(solicitacaoVinculo.getUidUsuarioAluno())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                adapter.removerSolicitacaoVinculo(usuario);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void adicionarNaListaUsuariosComAlgumTipoSolicitacao(Usuario usuario, int tipo){
        if(tipo == Constantes.FLAG_USUARIOS_VINCULO){
            adapter.adicionarVinculo(usuario);
        } else {
            adapter.adicionarNaListaSolicitacaoVinculoEnviada(usuario);
        }
        atualizarAdapter();
    }

    private void atualizarListaPesaquisaUsuariosAluno(List<Usuario> usuariosAluno){
        adapter.adicionarUsuariosPesquisa(usuariosAluno);
        atualizarAdapter();
    }

    public void resetarPesquisa(){
        adapter.resetarListaUsuarios();
        atualizarAdapter();
    }

    private void atualizarAdapter(){
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public void setFecharSearchViewListener(FecharSearchView listener){
        this.listener = listener;
    }

    public interface FecharSearchView{
        void fechar();
    }
}
