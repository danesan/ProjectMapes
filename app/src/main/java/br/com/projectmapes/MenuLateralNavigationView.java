package br.com.projectmapes;

import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;

public class MenuLateralNavigationView  {

    private int opcaoSelecionada;
    private DrawerLayout drawerLayout;
    private AppCompatActivity activity;
    private NavigationView navigationView;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private int numeroSolicitacoesVinculos = 0;

    public MenuLateralNavigationView(final AppCompatActivity activity, DrawerLayout drawerLayout, Usuario usuario) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;

        if(usuario.getPerfil().equalsIgnoreCase(Constantes.USUARIO_RESPONSAVEL)){
            navigationView = (NavigationView) activity.findViewById(R.id.navigation_view_menu_lateral_usuario_responsavel);
        } else {
            navigationView = (NavigationView) activity.findViewById(R.id.navigation_view_menu_lateral_tela_principal_usuario_aluno);
            SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
            solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                    .whereEqualTo("uidUsuarioAluno", usuario.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                            @Nullable FirebaseFirestoreException e) {

                            if(queryDocumentSnapshots.getDocumentChanges() != null){
                                for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()){
                                    Log.d("EVENTO", documentChange.getType()+ " ");
                                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                                        numeroSolicitacoesVinculos++;
                                    } else if(documentChange.getType() == DocumentChange.Type.REMOVED){
                                        numeroSolicitacoesVinculos--;
                                    }
                                }
                            }
                            Log.d("NUM_SOL_VIN", numeroSolicitacoesVinculos + "");
                            inicializarBadgeSolicitacoesVinculos(numeroSolicitacoesVinculos);
                        }
                    });
        }
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if(item.getItemId() != opcaoSelecionada){
                            selecionarOpcao(item);
                            return false;
                        }
                        return true;
                    }
                });

        Funcoes funcoes = new Funcoes();
        View headerView = (View) navigationView.getHeaderView(0);
        ImageView fotoPerfilImageView = headerView.findViewById(R.id.imageView_imagemUsuario_cabecalho_menuLateral);
        TextView loginUsuarioTextView = headerView.findViewById(R.id.textView_loginUsuario_menu_lateral);
        TextView nomeCompletoTextView = headerView.findViewById(R.id.textView_nomeCompletoUsuario_menu_lateral);

        Picasso.with(activity).load(usuario.getFotoURL()).fit().into(fotoPerfilImageView);
        loginUsuarioTextView.setText(usuario.getLogin());
        nomeCompletoTextView.setText(funcoes.converterLetrasNome(usuario.getNome()));
    }

    private void inicializarBadgeSolicitacoesVinculos(int numeroSolicitacoes){
        TextView badgeNumeroNotificacoesSolicitacoesvinculos = (TextView)
                MenuItemCompat.getActionView(navigationView.getMenu()
                        .findItem(R.id.opcao_vinculos_menu_usuario_aluno));
        if(numeroSolicitacoes <= 0){
            badgeNumeroNotificacoesSolicitacoesvinculos.setText("");
            return;
        }
        badgeNumeroNotificacoesSolicitacoesvinculos.setGravity(Gravity.CENTER_VERTICAL);
        badgeNumeroNotificacoesSolicitacoesvinculos.setTypeface(null, Typeface.BOLD);
        badgeNumeroNotificacoesSolicitacoesvinculos.setTextColor(activity.getResources().getColor(R.color.colorAccent));
        badgeNumeroNotificacoesSolicitacoesvinculos.setText("+" + numeroSolicitacoes);
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void marcarMenuItem(int opcaoSelecionada){
        this.opcaoSelecionada = opcaoSelecionada;
        navigationView.getMenu().findItem(opcaoSelecionada).setChecked(true);
    }

    public void selecionarMenuItem(){
        selecionarOpcao(navigationView.getMenu().findItem(opcaoSelecionada));
    }

    public void selecionarOpcao(MenuItem menuItem){
        try {
            //opcaoSelecionada = menuItem.getItemId();
            //menuItem.setChecked(false);
            Intent intent = null;
            switch (menuItem.getItemId()){
                case  R.id.opcao_vinculos_menu_usuario_responsavel:
                    intent = new Intent(activity, TelaVinculosResponsavelActivity.class);
                    break;
                case R.id.opcao_vinculos_menu_usuario_aluno :
                    intent = new Intent(activity, TelaVinculosAlunoFragment.class);
                    break;
                case R.id.opcao_configuracoes_menu_usuario_aluno :
                    drawerLayout.closeDrawers();
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.principal_fragment_view_tela_principal_usuario_aluno,
                                    new ConfiguracoesFragment()).commit();
                    //intent = new Intent(activity, ConfiguracoesFragment.class);
                    break;
                case  R.id.opcao_sair:
                    sharedPreferencesDAO = new SharedPreferencesDAO(activity);
                    sharedPreferencesDAO.atualizarUsuarioLogout();

                    intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    FirebaseAuth.getInstance().signOut();
                    activity.finish();
                    break;
            }
            if(!activity.getTitle().toString().equalsIgnoreCase(Constantes.NOME_APP)){
                activity.finish();
            }
            intent.putExtra(Constantes.KEY_OPCAO_SLEECIONADA_MENU_LATERAL, menuItem.getItemId());
            activity.startActivity(intent);
            drawerLayout.closeDrawers();
        } catch(NullPointerException ex){
        }

    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }
}
