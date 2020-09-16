package br.com.projectmapes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.QuerySnapshot;

import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;
import br.com.projectmapes.suporte.Funcoes;

public class ConfirmarVinculoDialogFragment extends DialogFragment {

    private Usuario usuarioResponsavel;
    private Usuario usuarioLogado;
    private String senha;

    private TextView loginUsuarioResponsavelTextView;
    private TextInputLayout senhaTextInputLayout;
    private Button confirmarButton;
    private Button cancelarButton;
    private SolicitacaoVinculoDAO solicitacaoVinculoDAO;
    private SolicitacaoVinculo solicitacaoVinculo;
    private Funcoes funcoes;
    private ConfirmarVinculoDialogFragment.AoConfirmarVinculo listener;

    private static final String DIALOG_TAG = "ConfirmarvinculoDialogFragment";

    public ConfirmarVinculoDialogFragment(Usuario usuarioResponsavel, Usuario usuarioLogado) {
        this.usuarioResponsavel = usuarioResponsavel;
        this.usuarioLogado = usuarioLogado;
        funcoes = new Funcoes();
        solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();

        solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                .whereEqualTo("uidUsuarioAluno", usuarioLogado.getUid())
                .whereEqualTo("uidUsuarioResponsavel", usuarioResponsavel.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        solicitacaoVinculo = queryDocumentSnapshots
                                .getDocuments().get(0).toObject(SolicitacaoVinculo.class);
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            senha = savedInstanceState.getString("SENHA");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        saveInstanceState.putString("SENHA", senhaTextInputLayout.getEditText().getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_dialog_confirmarvinculo, container, false);

        loginUsuarioResponsavelTextView = (TextView) layout.findViewById(R.id.login_confirmar_vinculo_dialog);
        loginUsuarioResponsavelTextView.setText(usuarioResponsavel.getLogin());
        senhaTextInputLayout = (TextInputLayout) layout.findViewById(R.id.senha_confirmar_vinculo_dialog);
        senhaTextInputLayout.getEditText().setText(senha);
        cancelarButton = (Button) layout.findViewById(R.id.cancelar_button_confirmar_vinculo_dialog);
        cancelarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        confirmarButton = (Button) layout.findViewById(R.id.enviar_button_canelar_vinculo_dialog);
        confirmarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String senha = senhaTextInputLayout.getEditText().getText().toString();

                senhaTextInputLayout.setError(null);
                if(senha.isEmpty()){
                    funcoes.setEditTextOnChangeFocus(senhaTextInputLayout, getResources().getString(R.string.erro_senha_cadastro_vazio));
                } else {
                    senha = funcoes.criptografarSenha(senhaTextInputLayout.getEditText().getText().toString());
                    if(!solicitacaoVinculo.getSenha().equals(senha)){
                        senhaTextInputLayout.setError(getString(R.string.erro_senha));
                    } else {
                        solicitacaoVinculoDAO.removerSolicitacaoVinculo(solicitacaoVinculo);
                        Vinculo vinculo = new Vinculo(usuarioResponsavel.getUid(), usuarioLogado.getUid());
                        VinculoDAO vinculoDAO = new VinculoDAO();
                        vinculoDAO.adicionarVinculo(vinculo);

                        listener.confirmarVinculo(usuarioResponsavel);
                        dismiss();
                    }
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

    public void setListener(AoConfirmarVinculo listener) {
        this.listener = listener;
    }

    public interface AoConfirmarVinculo{
        void confirmarVinculo(Usuario Usuario);
    }
}
