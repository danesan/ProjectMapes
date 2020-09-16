package br.com.projectmapes;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;

import br.com.projectmapes.dao.NotificacaoDAO;
import br.com.projectmapes.modelo.NotificacaoSolicitacaoVinculo;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Funcoes;

public class AdicionarVinculoDialogFragment extends DialogFragment {

    private String senha;
    private String confirmaSenha;
    private TextView loginUsuarioTextView;
    private TextInputLayout senhaTextInputLayout;
    private TextInputLayout confirmarSenhaTextInputLayout;
    private Button cancelarButton;
    private Button enviarButton;
    private SolicitacaoVinculo solicitacaoVinculo;

    private static final String DIALOG_TAG = "adicionarVinculoDialog";

    private Usuario usuarioResponsavel;
    private Usuario usuarioAluno;

    private AoAdicionarVinculo listener;

    public AdicionarVinculoDialogFragment(Usuario usuarioResponsavel, Usuario usuarioAluno) {
        this.usuarioResponsavel = usuarioResponsavel;
        this.usuarioAluno = usuarioAluno;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            senha = savedInstanceState.getString("SENHA");
            confirmaSenha = savedInstanceState.getString("CONFIRMA_SENHA");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveInstanceState.putString("SENHA", senhaTextInputLayout.getEditText().getText().toString());
        saveInstanceState.putString("CONFIRMA_SENHA", confirmarSenhaTextInputLayout.getEditText().getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_dialog_adicionarvinculo, container, false);

        loginUsuarioTextView = (TextView) layout.findViewById(R.id.nomeUsuario_DialogAdicionarVinculo);
        loginUsuarioTextView.setText(usuarioAluno.getLogin());
        senhaTextInputLayout = (TextInputLayout) layout.findViewById(R.id.cadastrarSenhaEditTextDialogAdicionarVinculo);
        senhaTextInputLayout.getEditText().setText(senha);
        confirmarSenhaTextInputLayout = (TextInputLayout) layout.findViewById(R.id.cadastrarConfirmarSenhaEditTextDialogAdicionarVinculo);
        confirmarSenhaTextInputLayout.getEditText().setText(confirmaSenha);

        cancelarButton = (Button) layout.findViewById(R.id.cancelarButtonAdicionarVinculoDialogFragment);
        cancelarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        enviarButton = (Button) layout.findViewById(R.id.enviarButtonAdicionarVinculoDialogFragment);
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Funcoes funcoes = new Funcoes();

                String senha = senhaTextInputLayout.getEditText().getText().toString();
                String confirmaSenha = confirmarSenhaTextInputLayout.getEditText().getText().toString();

                if(!senha.equals(confirmaSenha)) {
                    funcoes.setValidacaoSenhaOnChangeFocus(senhaTextInputLayout, confirmarSenhaTextInputLayout,
                            getResources().getString(R.string.erro_senha_confirma_Senha));
                } else {
                    SharedPreferencesDAO sharedPreferencesDAO = new SharedPreferencesDAO(getContext());
                    solicitacaoVinculo =
                            new SolicitacaoVinculo(usuarioResponsavel.getUid(),
                                    usuarioAluno.getUid(),
                                    funcoes.criptografarSenha(senha));

                    Log.d("FIREBASETOKEN", usuarioAluno.getFirebaseToken());
                    SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
                    solicitacaoVinculoDAO.adicionarSolicitacaoVinculo(solicitacaoVinculo,
                            usuarioAluno.getFirebaseToken());


                    //if(!usuarioAluno.isOnline()){

                    /*long dataHorario = System.currentTimeMillis();
                    String uidAluno = usuarioAluno.getUid();
                    String uidResponsavel = usuarioResponsavel.getUid();

                    NotificacaoSolicitacaoVinculo notificacaoSolicitacaoVinculo =
                            new NotificacaoSolicitacaoVinculo(dataHorario, uidAluno,
                                    uidResponsavel, solicitacaoVinculo.getId());

                    NotificacaoDAO notificacaoDAO = new NotificacaoDAO();
                    notificacaoDAO.getNotificacacoesSolicitacoesVinculosReference()
                            .document(usuarioAluno.getFirebaseToken())
                            .set(notificacaoSolicitacaoVinculo);
*/
                    //}
                    //Fragment fragment = getParentFragment();
                    //Log.d("Fragment", fragment.getId() + "");
                    //if (fragment instanceof AoAdicionarVinculo) {
                    //    AoAdicionarVinculo listener = (AoAdicionarVinculo) fragment;
                    listener.adicionarVinculo(usuarioAluno);
                    //}

                    Toast.makeText(getContext(), "Solicitação de vínculo enviado com sucesso", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });

        senhaTextInputLayout.getEditText().requestFocus();

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle(R.string.titulo_dialog_adicionar_vinculo);
        return layout;
    }

    public void abrir(FragmentManager fm){
        if(fm.findFragmentByTag(DIALOG_TAG) == null){
            show(fm, DIALOG_TAG);
        }
    }

    public void setAdicionarVinculoListener(AoAdicionarVinculo listener){
        this.listener = listener;
    }

    public interface AoAdicionarVinculo{
        void adicionarVinculo(Usuario Usuario);
    }
}