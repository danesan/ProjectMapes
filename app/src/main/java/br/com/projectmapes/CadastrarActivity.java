package br.com.projectmapes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import javax.annotation.Nullable;

import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;
import br.com.projectmapes.suporte.Funcoes;


public class CadastrarActivity extends AppCompatActivity {

    private DatabaseReference usuarioDatabaseReference;

    //private GoogleApiClient googleApiClient;
    private Button cadastrarGoogleButton;
    private Button cadastrarFacebookButton2;
    private GoogleSignInOptions googleAPISignIn;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 1;

    private CadastroImagemFragment cadastrarImagemFragment;
    private TextInputLayout loginEditText;
    private TextInputLayout emailEditText;
    private TextInputLayout senhaEditText;
    private TextInputLayout confirmaSenhaEditText;
    private AutoCompleteTextView perfilDropDownMenu;
    private ImageView imageCadastroImageView;
    private Button imagemCadastroButton;
    private TextInputLayout nomeCompletoEditText;
    private Button salvarButton;
    private ProgressBar uploadProgressBar;
    private ScrollView layoutTelaCadastrar;

    private UsuarioDAO usuarioDAO;
    private Activity activity;
    private Funcoes funcoes;
    private FirebaseUser firebaseUser;
    private SharedPreferences sharedPreferences;

    private boolean cadastroRealizado = false;

    private String TAG = "CadastrarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);
        activity = this;
        firebaseAuth = FirebaseAuth.getInstance();

        String[] PERFILS_USUARIOS = new String[]{"Aluno", "Responasável"};
        ArrayAdapter<String> perfilAdapater = new ArrayAdapter<>(getBaseContext(),
                R.layout.dropdown_menu_popup_perfil_item, PERFILS_USUARIOS);

        //instanciar as Views
        loginEditText = (TextInputLayout) findViewById(R.id.cadastrarLoginEditText);
        emailEditText = (TextInputLayout) findViewById(R.id.cadastrarEmailEditText);
        senhaEditText = (TextInputLayout) findViewById(R.id.cadastrarSenhaEditText);
        confirmaSenhaEditText = (TextInputLayout) findViewById(R.id.cadastrarConfirmarSenhaEditText);
        nomeCompletoEditText = (TextInputLayout) findViewById(R.id.cadastrarNomeCompletoEditText);
        perfilDropDownMenu = findViewById(R.id.perfil_exposed_dropdown);
        perfilDropDownMenu.setAdapter(perfilAdapater);
        uploadProgressBar = (ProgressBar) findViewById(R.id.upload_imagemperfil_progress_bar);
        layoutTelaCadastrar = (ScrollView) findViewById(R.id.layout_tela_cadastrar);

        //instanciar e configurar o botão para cadastro com Google
        configurarGoogleApiLogin();
        funcoes = new Funcoes();

        loginEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    funcoes.setEditTextOnChangeFocus(loginEditText,
                            getResources().getString(R.string.erro_login_cadastro_vazio));
                }
            }
        });

        emailEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    funcoes.setValidacaoEmailOnChangeFocus(emailEditText,
                            getResources().getString(R.string.erro_login_email));
                }
            }
        });

        senhaEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    funcoes.setEditTextOnChangeFocus(senhaEditText,
                            getResources().getString(R.string.erro_senha_cadastro_vazio));
                }
            }
        });

        confirmaSenhaEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    funcoes = new Funcoes();
                    funcoes.setValidacaoSenhaOnChangeFocus(senhaEditText, confirmaSenhaEditText,
                            getResources().getString(R.string.erro_senha_confirma_Senha));
                }
            }
        });

        nomeCompletoEditText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    funcoes.setEditTextOnChangeFocus(nomeCompletoEditText,
                            getResources().getString(R.string.erro_nome_cadastro_vazio));
                }
            }
        });

        final ArrayAdapter<CharSequence> perfilSpinnerAdapter =
                ArrayAdapter.createFromResource(this, R.array.perfis_array,
                        android.R.layout.simple_spinner_item);
        perfilSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinnerPerfil.setAdapter(perfilSpinnerAdapter);
        //spinnerPerfil.setFocusable(true);
        /*spinnerPerfil.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                loginEditText.clearFocus();
                emailEditText.clearFocus();
                senhaEditText.clearFocus();
                confirmaSenhaEditText.clearFocus();
                nomeCompletoEditText.clearFocus();
                return false;
            }
        });*/
        salvarButton = (Button) findViewById(R.id.salvarButton);
        salvarButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();

        cadastrarImagemFragment = (CadastroImagemFragment)
                getSupportFragmentManager().findFragmentById(R.id.containerCadastroImagemFragment);
        imageCadastroImageView = cadastrarImagemFragment.getImagemCadastroImageView();
        imagemCadastroButton = cadastrarImagemFragment.getImagemCadastroButton();
        cadastrarGoogleButton = cadastrarImagemFragment.getCadastrarGoogleButton();
        cadastrarFacebookButton2 = cadastrarImagemFragment.getCadastrarFacebookButton();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAPISignIn != null) {
            googleSignInClient = GoogleSignIn.getClient(CadastrarActivity.this, googleAPISignIn);
        }

        cadastrarGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //instanciar e configurar o botão para cadastro com Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        try {
                                            String nome = object.getString("name");
                                            String email = object.getString("email");
                                            String login = email.substring(0,email.indexOf("@"));
                                            String stringFoto = "";
                                            String uriJson = (String) object.getJSONObject("picture").getJSONObject("data").get("url");

                                            Bundle params = new Bundle();
                                            params.putBoolean("redirect", false);
                                            params.putInt("height", 200);
                                            params.putInt("width", 200);
                                            params.putString("type", "normal");

                                            Uri foto = Uri.parse(uriJson);
                                            Usuario usuario = new Usuario(email, null,
                                                    foto.toString(), login, nome, null, null);
                                            atualizarActivity(usuario);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,picture,photos,albums");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("ERRO", exception.toString());
                    }
                });

        cadastrarFacebookButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(CadastrarActivity.this,
                        Arrays.asList("public_profile, user_photos, email"));

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                signInResultGoogle(task);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStop(){
        super.onStop();
        if(googleAPISignIn != null && googleSignInClient != null) {
        }
    }

    private void configurarGoogleApiLogin(){
        googleAPISignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(CadastrarActivity.this, googleAPISignIn);
    }

    private void signInResultGoogle(Task<GoogleSignInAccount> completedTask) {
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                int indexArroba = email.indexOf('@');
                String login = email.substring(0,indexArroba);
                Uri foto = account.getPhotoUrl();
                String nome = account.getDisplayName();
                //String perfil = spinnerPerfil.getSelectedItem().toString();

                Usuario usuario = new Usuario(email, null,
                        foto.toString(), login, nome, null, null);

                atualizarActivity(usuario);
            } else {
                Toast.makeText(getBaseContext(), "Sem conexão com a internet", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            Log.w("TAG", "signInResult: failed code=" + e.getStatusCode() + " " + e.toString());
            atualizarActivity(null);
        }
    }

    private void atualizarActivity(Usuario conta){
        loginEditText.getEditText().setText(conta.getLogin());
        emailEditText.getEditText().setText(conta.getEmail());
        if(conta.getFotoURL() != null){
            Uri fotoUri = Uri.parse(conta.getFotoURL());
            Picasso.with(getBaseContext()).load(fotoUri).into(imageCadastroImageView);
        }
        nomeCompletoEditText.getEditText().setText(conta.getNome());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void cadastrarUsuario(){
        final String login = loginEditText.getEditText().getText().toString();
        final String email = emailEditText.getEditText().getText().toString();
        final String senha = senhaEditText.getEditText().getText().toString();
        final String confirmaSenha = confirmaSenhaEditText.getEditText().getText().toString();
        final String nomeCompleto = nomeCompletoEditText.getEditText().getText()
                .toString().toLowerCase();
        final String perfil = perfilDropDownMenu.getText().toString();

        if(dadosEstaoValidos(login, email, senha, confirmaSenha, nomeCompleto, perfil)) {
            uploadProgressBar.setVisibility(View.VISIBLE);
            layoutTelaCadastrar.setForeground(getResources().getDrawable(R.color.cor_background_carregando, null));
            salvarButton.setEnabled(false);

            final Usuario usuarioQueSeraCadastrado = new Usuario(email, null, null,
                    login, nomeCompleto, perfil, null);

            final Task<InstanceIdResult> firebaseInstanceId = FirebaseInstanceId.getInstance()
                    .getInstanceId();
            firebaseInstanceId.addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }
                    final String firebaseToken = task.getResult().getToken();
                    usuarioQueSeraCadastrado.setFirebaseToken(firebaseToken);

                    verificarSeExisteLoginCadastradoECriarUsuarioFirebase(usuarioQueSeraCadastrado, senha);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean dadosEstaoValidos(String login, String email, String senha,
                                      String confirmaSenha, String nomeCompleto, String perfil){

        boolean erro = false;
        if(login.length() == 0){
            loginEditText.setError(getResources().getString(R.string.erro_login_cadastro_vazio));
            erro = true;
        } else {
            loginEditText.setError(null);
        }
        if(email.length() == 0 && !funcoes.emailEValido(email)) {
            emailEditText.setError(getResources().getString(R.string.erro_login_email));
            erro = true;
        } else {
            emailEditText.setError(null);
        }
        if(senha.length() < 6) {
            senhaEditText.setError(getResources().getString(R.string.erro_senha_cadastro_quant_minima));
            erro = true;
        } else {
            senhaEditText.setError(null);
        }
        if (!senha.equals(confirmaSenha)) {
            confirmaSenhaEditText.setError(getResources().getString(R.string.erro_senha_confirma_Senha));
            confirmaSenhaEditText.getEditText().setText(null);
            erro = true;
        } else {
            confirmaSenhaEditText.setError(null);
        }
        if (nomeCompleto.length() == 0) {
            nomeCompletoEditText.setError(getResources().getString(R.string.erro_nome_cadastro_vazio));
            nomeCompletoEditText.getEditText().setText(null);
            erro = true;
        } else {
            nomeCompletoEditText.setError(null);
        }

        if(perfil.equals("Escolha")){
            Toast.makeText(getBaseContext(), "Escolha um perfil", Toast.LENGTH_SHORT).show();
            erro = true;
        }
        return !erro;
    }

    private void verificarSeExisteLoginCadastradoECriarUsuarioFirebase(final Usuario usuarioQueSeraCadastrado, final String senha){
        usuarioDAO = new UsuarioDAO();

        usuarioDAO.getUsuarioCollectionReference().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                boolean jaExisteLoginCadastrado = false;

                for (DocumentSnapshot documentSnapshotSnapshot : queryDocumentSnapshots.getDocuments()){
                    Usuario usuario = documentSnapshotSnapshot.toObject(Usuario.class);
                    if(usuario.getLogin().equalsIgnoreCase(usuarioQueSeraCadastrado.getLogin())){
                        jaExisteLoginCadastrado = true;
                        break;
                    }
                }

                if(!jaExisteLoginCadastrado) {
                    criarUsuarioFirebase(usuarioQueSeraCadastrado, senha);
                } else {
                    if(!cadastroRealizado) {
                        uploadProgressBar.setVisibility(View.GONE);
                        layoutTelaCadastrar.setForeground(null);
                        salvarButton.setEnabled(true);
                        Toast.makeText(activity, R.string.erro_login_ja_existente, Toast.LENGTH_LONG).show();
                        loginEditText.requestFocus();
                    }
                }
            }
        });



    /*final Query query = usuarioDAO.getUsuarioDatabaseReference().orderByChild("login");
    query.addValueEventListener(new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            boolean jaExisteLoginCadastrado = false;
            for (DataSnapshot objetoSnapshot : dataSnapshot.getChildren()){
                Usuario usuario = objetoSnapshot.getValue(Usuario.class);
                if(usuario.getLogin().equalsIgnoreCase(usuarioQueSeraCadastrado.getLogin())){
                    jaExisteLoginCadastrado = true;
                    break;
                }
            }
           if(!jaExisteLoginCadastrado) {*/
        //criarUsuarioFirebase(usuarioQueSeraCadastrado);
        //} else {
        //uploadProgressBar.setVisibility(View.GONE);
        //layoutTelaCadastrar.setForeground(null);
        //salvarButton.setEnabled(true);
        //Toast.makeText(activity, R.string.erro_login_ja_existente, Toast.LENGTH_LONG).show();
        //loginEditText.requestFocus();
        //}
        //query.removeEventListener(this);
        //}

        //@Override
        //public void onCancelled(@NonNull DatabaseError databaseError) {
        //}
        //});
    }

    private void criarUsuarioFirebase(final Usuario usuarioQuerSeraCadastrado, final String senha){
        firebaseAuth.createUserWithEmailAndPassword(usuarioQuerSeraCadastrado.getEmail(), senha)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            final StorageReference imagemPerfilUsuarioStrageReference = usuarioDAO
                                    .getFotosPerfilUsuarioStorageReference(usuarioQuerSeraCadastrado.getLogin());
                            imagemPerfilUsuarioStrageReference
                                    .putBytes(cadastrarImagemFragment.getFotoPerfilDoImageView())
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            imagemPerfilUsuarioStrageReference.getDownloadUrl()
                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            usuarioQuerSeraCadastrado.setFotoURL(uri.toString());
                                                            usuarioQuerSeraCadastrado.setUid(firebaseUser.getUid());

                                                            usuarioDAO.cadastrarUsuario(usuarioQuerSeraCadastrado, firebaseUser);

                                                            Toast.makeText(getApplicationContext(),
                                                                    "Usuário cadastrado com sucesso!",Toast.LENGTH_LONG)
                                                                    .show();

                                                            cadastroRealizado = true;
                                                            sharedPreferences = getBaseContext()
                                                                    .getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                                                                            MODE_PRIVATE);

                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.remove(Constantes.KEY_USUARIO_FIREBASE_LOGADO_EMAIL);
                                                            editor.remove(Constantes.KEY_USUARIO_FIREBASE_LOGADO_SENHA);
                                                            editor.remove(Constantes.KEY_USUARIO_FIREBASE_JA_LOGADO);
                                                            editor.commit();

                                                            Intent loginIntent = new Intent(getBaseContext(),
                                                                    LoginActivity.class);
                                                            loginIntent.putExtra("usuarioRecemCadastrado", usuarioQuerSeraCadastrado);
                                                            startActivity(loginIntent);
                                                            finish();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Teste", e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String mensagemErro = e.getLocalizedMessage();
                        switch (mensagemErro){
                            case "The email address is already in use by another account." :
                                uploadProgressBar.setVisibility(View.GONE);
                                layoutTelaCadastrar.setForeground(null);
                                salvarButton.setEnabled(true);
                                emailEditText.requestFocus();
                                Toast.makeText(getApplicationContext(),"E-mail já está em uso por outra conta", Toast.LENGTH_LONG)
                                        .show();
                                break;
                        }
                    }
                });

        //byte[] imagemPerfil = cadastrarImagemFragment.getFotoPerfilDoImageView();
        //UploadTask salvarImagemCloudStorageTask = usuarioDAO
        //        .getFotosPerfilUsuarioStorageReference(usuarioQuerSeraCadastrado.getLogin())
        //        .putBytes(imagemPerfil);
/*
                    Task<Uri> getDownloadUriTask = salvarImagemCloudStorageTask.continueWithTask(
                            new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if(!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return usuarioDAO
                                            .getFotosPerfilUsuarioStorageReference(usuarioQuerSeraCadastrado.getLogin())
                                            .getDownloadUrl();
                                }
                            }
                    );

                    getDownloadUriTask.addOnCompleteListener(CadastrarActivity.this, new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {
                                Uri fotoUrl = task.getResult();
                                Log.d("FOTO_URL", fotoUrl.toString());

                                usuarioQuerSeraCadastrado.setFotoURL(fotoUrl.toString());
                                Log.d("USUARIO_CADAST", usuarioQuerSeraCadastrado.toString());

                                usuarioDAO.cadastrarUsuario(usuarioQuerSeraCadastrado, firebaseUser);
                                Toast.makeText(getApplicationContext(),
                                        "Usuário cadastrado com sucesso!",Toast.LENGTH_LONG)
                                        .show();

                                sharedPreferences = getBaseContext()
                                        .getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                                                MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove(Constantes.KEY_USUARIO_FIREBASE_LOGADO_EMAIL);
                                editor.remove(Constantes.KEY_USUARIO_FIREBASE_LOGADO_SENHA);
                                editor.remove(Constantes.KEY_USUARIO_FIREBASE_JA_LOGADO);
                                editor.commit();

                                Intent loginIntent = new Intent(getBaseContext(),
                                        LoginActivity.class);
                                loginIntent.putExtra("usuarioRecemCadastrado", usuarioQuerSeraCadastrado);
                                startActivity(loginIntent);
                                finish();
                            }
                        }
                    });*/

    }

}
