package br.com.projectmapes;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
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

public class TelaVinculosResponsavelActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        AdicionarVinculoDialogFragment.AoAdicionarVinculo {

    private int opcaoSelecionada;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private MenuLateralNavigationView menuLateralNavigationView;
    private ActionBarDrawerToggle drawerToggle;
    private UsuarioDAO usuarioDAO;
    private List<Usuario> usuariosVinculos;
    private List<Usuario> usuariosComSolicitacoesVinculoEnviadas;
    private Usuario usuarioLogado;

    private RecyclerView recyclerView;
    private UsuarioRecyclerViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private SearchView searchView;
    private Menu menu;
    private Context context;
    private UsuarioRecyclerViewAdapter.OnItemClickListener onItemClickListener;
    //  private MenuLateralNavigationView menuLateralNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_vinculos);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar_tela_principal_usuario_aluno);
        toolbar.setTitle(R.string.titulo_vinculos_activity);
        setSupportActionBar(toolbar);

        adapter = new UsuarioRecyclerViewAdapter(context);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_usuarios_tela_vinculos);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        usuarioDAO = new UsuarioDAO();
        sharedPreferencesDAO = new SharedPreferencesDAO(this);
        usuarioLogado = sharedPreferencesDAO.getUsuarioLogado();

        onItemClickListener = new UsuarioRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int posicao, Usuario usuarioAluno) {
                //AdicionarVinculoDialogFragment adicionarVinculoDialogFragment =
                //        new AdicionarVinculoDialogFragment(usuarioLogado, usuarioAluno, getSupportFragmentManager().findFragmentById());
                //adicionarVinculoDialogFragment.abrir(getSupportFragmentManager());
            }
        };

        listarUsuariosAlunoComQuemTemVinculoESolicitacaoVinculo(usuarioLogado);

        SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
        solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioResponsavel", usuarioLogado.getUid())
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

        opcaoSelecionada = getIntent()
                .getIntExtra(Constantes.KEY_OPCAO_SLEECIONADA_MENU_LATERAL, 0);

        drawerLayout = (DrawerLayout)
                findViewById(R.id.drawerLayout_menuLateral_telaVinculosUsuarioResponsavel);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        menuLateralNavigationView = new MenuLateralNavigationView(this, drawerLayout, usuarioLogado);

        if (savedInstanceState == null) {
            menuLateralNavigationView.marcarMenuItem(opcaoSelecionada);
        } else {
            opcaoSelecionada = savedInstanceState.getInt("menuItem");
            menuLateralNavigationView.marcarMenuItem(opcaoSelecionada);
        }
        menuLateralNavigationView.selecionarMenuItem();
    }

    @Override
    public void onPostCreate(Bundle savedInstance, PersistableBundle persistableBundle) {
        super.onPostCreate(savedInstance, persistableBundle);
        //menuLateralFragment.sincronizarDrawerToogle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_action_bar_tela_vinculos, menu);
        Log.d("TELA_VINCULOS", "ONCREATEOPTIONSMENU");
        this.menu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_acao_pesquisar).getActionView();
        searchView.onActionViewCollapsed();
        /*
        MenuItem searchItem = menu.findItem(R.id.menu_acao_pesquisar);
        SearchView searchView = null;
        if(searchItem != null){
            searchView = (SearchView) searchItem.getActionView();
        }
        if(searchView != null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        }*/
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //menuLateralFragment.exibirDrawerLayout();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String pesquisa) {
        Log.d("PESQUISA", "teste" + pesquisa);
        Log.d("PESQUISA_NULA", "teste" + pesquisa.isEmpty());

        if (!pesquisa.isEmpty()) {
            try {
                pesquisarUsuarioAluno(pesquisa);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            if (adapter != null) {
                adapter.resetarListaUsuarios();
                adapter.notifyDataSetChanged();
            }
        }

        return false;
    }

    private void pesquisarUsuarioAluno(String pesquisa) throws ExecutionException, InterruptedException {
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

                    if (!usuario.getLogin().equals(usuarioLogado.getLogin())
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
                                        }
                                    });
                        }
                    }
                });

    }

    @Override
    public void adicionarVinculo(Usuario usuario) {
        adapter.adicionarUsuarioSolicitacaVinculoEnviada(usuario);
        adapter.notifyDataSetChanged();
        toolbar.collapseActionView();
        onQueryTextChange("");
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

    private void atualizarAdapter(){
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
