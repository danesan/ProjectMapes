package br.com.projectmapes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout loginEditText;
    private TextInputLayout senhaEditText;
    private Button entrarButton;
    private Button cadastrarButton;
    private TextView esqueceuSenhaTextView;
    private ProgressBar loginProgressBar;
    private RelativeLayout layoutTelaLogin;

    private Funcoes funcoes;
    private FirebaseAuth firebaseAuth;
    private UsuarioDAO usuarioDAO;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private FirebaseAuth.AuthStateListener authStateListener;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate","");
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferencesDAO = new SharedPreferencesDAO(this);
        usuarioDAO = new UsuarioDAO();

        //instanciar Views
        loginEditText = (TextInputLayout) findViewById(R.id.editText_loginEmail);
        senhaEditText = (TextInputLayout) findViewById(R.id.editText_senha);
        entrarButton = (Button) findViewById(R.id.button_entrar);
        cadastrarButton = (Button) findViewById(R.id.button_cadastrar);
        esqueceuSenhaTextView = (TextView) findViewById(R.id.textView_esqueceuSenha);
        loginProgressBar = (ProgressBar) findViewById(R.id.progressBar_login);
        layoutTelaLogin = (RelativeLayout) findViewById(R.id.layout_telaLogin);

        verificarSeUsuarioEstaLogado();

        //inicializar click/focusChange listeners
        inicializarViewsListeners();
    }

    private void inicializarViewsListeners(){
        loginEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    funcoes = new Funcoes();
                    funcoes.setValidacaoEmailOnChangeFocus(loginEditText, getString(R.string.erro_login_email));
                }
            }
        });

        entrarButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                funcoes = new Funcoes();
                final String email = loginEditText.getEditText().getText().toString();
                final String senha = senhaEditText.getEditText().getText().toString();

                boolean temErroLogin = false;
                if(email.length() == 0) {
                    loginEditText.setError("Insira um e-mail");
                    temErroLogin = true;
                } else {
                    loginEditText.setError(null);
                }
                if(senha.length() == 0) {
                    senhaEditText.setError("Insira a senha");
                    temErroLogin = true;
                } else {
                    senhaEditText.setError(null);
                }
                if(!temErroLogin){
                    ativarViews(false);

                    final Task<AuthResult> result =  firebaseAuth
                            .signInWithEmailAndPassword(email, senha);
                    result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //SALVANDO INFORMAÇÕES DO USUÁRIO LOGADO EM SHAREDPREFERENCES
                                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                usuarioDAO.getUsuarioCollectionReference()
                                        .whereEqualTo("email", email)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(!task.isSuccessful()){
                                                    Log.e("Erro", task.getException().getMessage());
                                                    return;
                                                }

                                                Usuario usuario = task.getResult().getDocuments().get(0)
                                                        .toObject(Usuario.class);

                                                atualizarTokenUsuario(usuario);
                                                sharedPreferencesDAO.atualizarUsuarioLogin(usuario, senha);
                                                iniciarTelaPrincipal(usuario);
                                            }
                                        });
                                /*
                                usuarioDAO.getUsuarioDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot objetoSnapshot : dataSnapshot.getChildren()) {
                                            if(objetoSnapshot.getKey().equalsIgnoreCase(firebaseUser.getUid())){
                                                Usuario usuario = objetoSnapshot.getValue(Usuario.class);
                                                Log.d("USUARIO", usuario.toString());

                                                sharedPreferencesDAO.atualizarUsuarioLogin(usuario);

                                                if(usuario.getPerfil().equalsIgnoreCase(Constantes.USUARIO_RESPONSAVEL)){
                                                    iniciarTelaPrincipal(usuario);
                                                } else {
                                                    //Intent telaPrincipalIntent = new Intent(getBaseContext(),
                                                    //        TelaPrincipalUsuarioAlunoActivity.class);
                                                    //startActivity(telaPrincipalIntent);
                                                    //finish();
                                                }

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });*/
                            } else {
                                ativarViews(true);

                                String mensagemErro = task.getException().getMessage();
                                Log.d("ERRO: ", task.getException().getMessage().toString());
                                switch (mensagemErro){
                                    case "There is no user record corresponding to this identifier. The user may have been deleted." :
                                        lancarErroExcecao("E-mail não encontrado", 1);
                                        break;
                                    case "The password is invalid or the user does not have a password." :
                                        Log.d("ERRO_SENHA", senha);
                                        lancarErroExcecao("Senha inválida", 2);
                                        break;
                                    case "A network error (such as timeout, interrupted connection or unreachable host) has occurred." :
                                        lancarErroExcecao("Não foi possível se conectar. Verifique sua conexão com a internet",3);
                                }
                            }
                        }
                    });
                }
            }
        });

        cadastrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cadastrarIntent = new Intent(getBaseContext(), CadastrarActivity.class);
                startActivity(cadastrarIntent);
                //finish();
            }
        });

        esqueceuSenhaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EsqueceuASenhaDialogFragment esqueceuASenhaDialogFragment = new EsqueceuASenhaDialogFragment();
                esqueceuASenhaDialogFragment.abrir(getSupportFragmentManager());
            }
        });

    }

    private void atualizarTokenUsuario(final Usuario usuario) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.e("ERRO NO TOKEN", String.valueOf(task.getException()));
                } else {
                    final String tokenAtualizado = task.getResult().getToken();
                    final String tokenAntigo = usuario.getFirebaseToken();

                    Log.d("TOKEN ANTIGO", usuario.getFirebaseToken());
                    Log.d("TOKEN ATUAL", tokenAtualizado);
                    Log.d("USER ID", usuario.getUid());

                    usuarioDAO.getUsuarioCollectionReference()
                            .document(usuario.getUid())
                            .update("firebaseToken", tokenAtualizado);

                    sharedPreferencesDAO.atualizarFirebaseTokenUsuarioLogado(tokenAtualizado);
                }
            }
        });
    }

    private void lancarErroExcecao(String mensagemErro, int erroId){
        if(erroId == 1) {
            loginEditText.setError(mensagemErro);
            senhaEditText.getEditText().setText("");
            loginEditText.getEditText().requestFocus();
        } else {
            senhaEditText.setError(mensagemErro);
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop(){
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    private void preencherInformacoesUsuarioRecemCadastrado(){
        Intent intent = getIntent();
        Usuario usuario = intent.getParcelableExtra("usuarioRecemCadastrado");
        Funcoes funcoes = new Funcoes();
        if(usuario != null){
            loginEditText.getEditText().setText(usuario.getEmail());
        } else {
            Log.d("USER_RECEM_CAD", "Não foi recem cadastrado");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void verificarSeUsuarioEstaLogado() {
        boolean jaLogado = sharedPreferencesDAO.usarioJalogado();
        String email = sharedPreferencesDAO.getEmailUsuarioJaLogadoAlgumaVez();
        String senha = sharedPreferencesDAO.getSenhaUsuarioJaLogadoAlgumaVez();

        Log.d("USER_IS_LOG", jaLogado + "");

        if (jaLogado) {
            Usuario usuario = sharedPreferencesDAO.getUsuarioLogado();
            Log.d("USUARIO_LOGADO", usuario.toString());

            atualizarTokenUsuario(usuario);
            iniciarTelaPrincipal(usuario);
        } else {
            if(email.isEmpty()){
                preencherInformacoesUsuarioRecemCadastrado();
            } else {
                loginEditText.getEditText().setText(email);
            }
        }
    }

    public static class EsqueceuASenhaDialogFragment extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstance){
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View layoutDialog = inflater.inflate(R.layout.fragment_dialog_esqueceu_senha, null);
            final TextInputLayout emailEditText = (TextInputLayout) layoutDialog.findViewById(R.id.emailEsqueceuSenha);
            Button cancelarButton = (Button) layoutDialog.findViewById(R.id.cancelarButtonEsqueceuSenhaDialogFragment);
            Button enviarButton = (Button) layoutDialog.findViewById(R.id.enviarButtonEsqueceuSenhaDialogFragment);
            emailEditText.requestFocus();

            cancelarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            enviarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getEditText().getText().toString();
                    Funcoes funcoes = new Funcoes();

                    if (email.length() == 0) {
                        emailEditText.setError("Insira um e-mail");
                        return;
                    }
                    else {
                        if(funcoes.emailEValido(email)){
                            resetarSenhaUsuario(email, getDialog(), emailEditText);
                        } else {
                            emailEditText.setError("Insira um e-mail válido");
                        }
                    }
                }
            });

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(layoutDialog)
                    .create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            return dialog;
        }

        public void abrir(FragmentManager fragmentManager){
            show(fragmentManager, "DIALOG_ESQUECEU_SENHA");
        }


        private void resetarSenhaUsuario(final String email, final Dialog dialog,
                                         final TextInputLayout emailEditText){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(),
                                        "E-mail para resetar senha foi enviado para " + email,
                                        Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                try {
                                    throw task.getException();
                                } catch (com.google.firebase.auth
                                        .FirebaseAuthInvalidUserException exception) {
                                    Toast.makeText(getContext(), "Nenhum usuário encontrado " +
                                            "para o e-mail informado", Toast.LENGTH_LONG).show();
                                    emailEditText.getEditText().selectAll();
                                    emailEditText.getEditText().requestFocus();
                                } catch (Exception e) {
                                }
                                Log.d("ERRO", task.getException().toString());
                            }
                        }
                    });
        }
    }

    private void iniciarTelaPrincipal(Usuario usuario){
        Intent telaPrincipalIntent;
        if(usuario.getPerfil().equalsIgnoreCase(Constantes.USUARIO_RESPONSAVEL)){
            telaPrincipalIntent = new Intent(getBaseContext(),
                    TelaPrincipalUsuarioResponsavelActivity.class);
        } else {
            telaPrincipalIntent = new Intent(getBaseContext(),
                    TelaPrincipalUsuarioAlunoActivity.class);
        }

        telaPrincipalIntent.putExtra(Constantes.KEY_USUARIO_EXTRA, usuario);
        startActivity(telaPrincipalIntent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ativarViews(boolean acao){
        loginEditText.setEnabled(acao);
        senhaEditText.setEnabled(acao);
        cadastrarButton.setEnabled(acao);
        entrarButton.setEnabled(acao);
        esqueceuSenhaTextView.setEnabled(acao);

        if(!acao){
            loginProgressBar.setVisibility(View.VISIBLE);
            layoutTelaLogin.setForeground(getResources().getDrawable(R.color.cor_background_carregando, null));
        } else {
            loginProgressBar.setVisibility(View.GONE);
            layoutTelaLogin.setForeground(null);
        }
    }

}
