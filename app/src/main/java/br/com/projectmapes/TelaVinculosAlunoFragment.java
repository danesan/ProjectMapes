package br.com.projectmapes;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.VinculosUsuarioAlunoRecyclerViewAdapter;

public class TelaVinculosAlunoFragment extends Fragment{

    private Toolbar toolbar;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private Usuario usuarioAlunoLogado;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private MenuLateralNavigationView menuLateralNavigationView;
    private int opcaoSelecionada;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private VinculosUsuarioAlunoRecyclerViewAdapter adapter;
    private TextView resultadoVinculos;

    private SolicitacaoVinculoDAO solicitacaoVinculoDAO;
    private List<Usuario> usuariosComAlgumTipoVinculo;
    private VinculoDAO vinculoDAO;
    private UsuarioDAO usuarioDAO;

    public static TelaVinculosAlunoFragment novaInstancia(Usuario usuarioAlunoLogado){
        TelaVinculosAlunoFragment telaVinculosAlunoFragment = new TelaVinculosAlunoFragment(usuarioAlunoLogado);
        return telaVinculosAlunoFragment;
    }

    private TelaVinculosAlunoFragment(Usuario usuarioAlunoLogado){
        this.usuarioAlunoLogado = usuarioAlunoLogado;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_tela_vinculos_aluno,
                container, false);

        sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
        usuarioAlunoLogado = sharedPreferencesDAO.getUsuarioLogado();

        usuarioDAO = new UsuarioDAO();
        vinculoDAO = new VinculoDAO();

        usuariosComAlgumTipoVinculo = new ArrayList<>();

        resultadoVinculos = (TextView) layout.findViewById(R.id.resultado_vinculos_text_view_tela_vinculos_usuario_aluno);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view_usuarios_tela_vinculos);
        recyclerView.setHasFixedSize(true);

        adapter = new VinculosUsuarioAlunoRecyclerViewAdapter(getContext());
        adapter.setOnConfirmarButtonClickListener(new VinculosUsuarioAlunoRecyclerViewAdapter
                .OnConfirmarButtonClickListener() {
            @Override
            public void onButtonClick(Usuario usuarioResponsavel) {
                ConfirmarVinculoDialogFragment confirmarvinculoDialogFragment =
                        new ConfirmarVinculoDialogFragment(usuarioResponsavel, usuarioAlunoLogado);
                confirmarvinculoDialogFragment.abrir(getActivity().getSupportFragmentManager());
                confirmarvinculoDialogFragment.setListener(new ConfirmarVinculoDialogFragment.AoConfirmarVinculo() {
                    @Override
                    public void confirmarVinculo(Usuario usuario) {
                        Log.d("confirmou", "vínculo");
                        //adapter.confirmarVinculo(usuario);
                        //adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        adapter.setOnExcluirButtonClickListener(new VinculosUsuarioAlunoRecyclerViewAdapter.OnExcluirButtonClickListener() {
            @Override
            public void onButtonClick(Usuario usuarioResponsavel) {
                solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                        .whereEqualTo("uidUsuarioAluno", usuarioAlunoLogado.getUid())
                        .whereEqualTo("uidUsuarioResponsavel", usuarioResponsavel.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots
                                        .getDocuments().get(0);
                                SolicitacaoVinculo solicitacaoVinculo = documentSnapshot.toObject(SolicitacaoVinculo.class);
                                solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                                        .document(solicitacaoVinculo.getId()).delete();
                            }
                        });
            }
        });

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view_usuarios_tela_vinculos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);

        solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
        solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioAluno", usuarioAlunoLogado.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots.getDocumentChanges() != null){
                            for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                                if(documentChange.getType() == DocumentChange.Type.ADDED){
                                    SolicitacaoVinculo solicitacaoVinculo = documentChange
                                            .getDocument().toObject(SolicitacaoVinculo.class);
                                    adicionarSolicitacaoVinculo(solicitacaoVinculo);
                                } else if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                    SolicitacaoVinculo solicitacaoVinculo = documentChange
                                            .getDocument().toObject(SolicitacaoVinculo.class);
                                    excluirSolcitacaoVinculoUsuario(solicitacaoVinculo);
                                }
                                Log.d("MUDANÇAS", documentChange.getType().name());
                                Log.d("SOLICITACAO_VINCDAO", documentChange.getType().name());
                            }
                        }
                    }
                });

        vinculoDAO = new VinculoDAO();
        vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioAluno", usuarioAlunoLogado.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots.getDocumentChanges() != null){
                            for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                                if(documentChange.getType() == DocumentChange.Type.ADDED){
                                    Vinculo vinculo = documentChange
                                            .getDocument().toObject(Vinculo.class);
                                    adicionarVinculoUsuario(vinculo);
                                } else if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                    Log.d("VINCULO", "removido");
                                    Vinculo vinculo = documentChange.getDocument().toObject(Vinculo.class);
                                    excluirVinculoUsuario(vinculo);
                                    //adapter.notifyDataSetChanged();
                                }
                                Log.d("MUDANÇAS", documentChange.getType().name());
                                Log.d("VINCDAO", documentChange.getType().name());
                            }
                        }
                    }
                });

        //listarVinculosESolicitacoesVinculos();

        return layout;
    }

    private void listarVinculosESolicitacoesVinculos() {
        Task<QuerySnapshot> query = solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioAluno", usuarioAlunoLogado.getUid())
                .get();
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    final SolicitacaoVinculo solicitacaoVinculo = documentSnapshot.toObject(SolicitacaoVinculo.class);

                    usuarioDAO.getUsuarioCollectionReference()
                            .document(solicitacaoVinculo.getUidUsuarioResponsavel())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);

                                    if(!usuariosComAlgumTipoVinculo.contains(usuarioResponsavel)){
                                        adapter.adicionarUsuario(usuarioResponsavel,
                                                Constantes.FLAG_USUARIOS_SOLICITACAO_VINCULO);
                                        adapter.notifyDataSetChanged();
                                        resultadoVinculos.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        query = vinculoDAO.getVinculosCollectionReference()
                .whereEqualTo("uidUsuarioAluno", usuarioAlunoLogado.getUid())
                .get();
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);

                    usuarioDAO.getUsuarioCollectionReference()
                            .document(vinculo.getUidUsuarioResponsavel())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.d("CHAMOU", "PESQ_VINC");
                                    Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                                    adapter.adicionarUsuario(usuarioResponsavel,
                                            Constantes.FLAG_USUARIOS_VINCULO);
                                    adapter.notifyDataSetChanged();
                                    resultadoVinculos.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });
    }

    private void adicionarSolicitacaoVinculo(SolicitacaoVinculo solicitacaoVinculo){
        usuarioDAO.getUsuarioCollectionReference()
                .document(solicitacaoVinculo.getUidUsuarioResponsavel())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                        adapter.adicionarUsuario(usuarioResponsavel,
                                Constantes.FLAG_USUARIOS_SOLICITACAO_VINCULO);
                        adapter.notifyDataSetChanged();
                        verificarAdapterVazio();
                    }
                });
    }

    private void excluirSolcitacaoVinculoUsuario(SolicitacaoVinculo solicitacaoVinculo){
        usuarioDAO.getUsuarioCollectionReference()
                .document(solicitacaoVinculo.getUidUsuarioResponsavel())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                        adapter.excluirSolcitacaoVinculoUsuario(usuarioResponsavel);
                        adapter.notifyDataSetChanged();
                        verificarAdapterVazio();
                    }
                });
    }

    private void adicionarVinculoUsuario(Vinculo vinculo){
        usuarioDAO.getUsuarioCollectionReference()
                .document(vinculo.getUidUsuarioResponsavel())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                        adapter.confirmarVinculo(usuarioResponsavel);
                        adapter.notifyDataSetChanged();
                        verificarAdapterVazio();
                    }
                });
    }


    private void excluirVinculoUsuario(Vinculo vinculo){
        usuarioDAO.getUsuarioCollectionReference()
                .document(vinculo.getUidUsuarioResponsavel())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                        adapter.excluirVinculoUsuario(usuarioResponsavel);
                        adapter.notifyDataSetChanged();
                        verificarAdapterVazio();
                    }
                });
    }

    private void verificarAdapterVazio() {
        Log.d("ADPATER_QUANT", adapter.getItemCount() +"");
        if(adapter.getItemCount() != 0){
            resultadoVinculos.setVisibility(View.GONE);
        } else {
            resultadoVinculos.setVisibility(View.VISIBLE);
        }
    }

}