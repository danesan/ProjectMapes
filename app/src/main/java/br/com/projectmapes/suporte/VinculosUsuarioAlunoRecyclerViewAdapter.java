package br.com.projectmapes.suporte;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.R;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;

public class VinculosUsuarioAlunoRecyclerViewAdapter extends
        RecyclerView.Adapter<VinculosUsuarioAlunoRecyclerViewAdapter.VinculosUsuarioAluno> {

    private Context contexto;
    private List<Usuario> usuariosComQuemTemVinculo;
    private List<Usuario> usuariosComSolicitacaoVinculoRecebida;
    private List<Usuario> usuarios;
    private VinculosUsuarioAlunoRecyclerViewAdapter.OnItemClickListener listener;
    private OnConfirmarButtonClickListener confirmarButtonClickListener;
    private OnExcluirButtonClickListener excluirButtonClickListener;
    private Funcoes funcoes;
    private boolean vinculoConfirmado = false;

    public VinculosUsuarioAlunoRecyclerViewAdapter(Context contexto) {
        this.contexto = contexto;
        usuariosComQuemTemVinculo = new ArrayList<>();
        usuariosComSolicitacaoVinculoRecebida = new ArrayList<>();
        usuarios = new ArrayList<>();
        funcoes = new Funcoes();
    }

    public void setOnItemClickListener(VinculosUsuarioAlunoRecyclerViewAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnConfirmarButtonClickListener(OnConfirmarButtonClickListener listener){
        confirmarButtonClickListener = listener;
    }

    public void setOnExcluirButtonClickListener(OnExcluirButtonClickListener listener){
        excluirButtonClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int posicao, Usuario usuario);
    }

    public interface OnConfirmarButtonClickListener {
        void onButtonClick(Usuario usuario);
    }

    public interface OnExcluirButtonClickListener {
        void onButtonClick(Usuario usuario);
    }

    public static class VinculosUsuarioAluno extends RecyclerView.ViewHolder {
        public ImageView fotoURLUsuario;
        public TextView loginUsuario;
        public TextView nomeCompletoUsuario;
        public Button confirmarButton;
        public Button excluirButton;

        public VinculosUsuarioAluno(View usuarioVinculoItemView) {
            super(usuarioVinculoItemView);
            fotoURLUsuario = (ImageView) usuarioVinculoItemView.findViewById(R.id.fotoUsuarioItem);
            loginUsuario = (TextView) usuarioVinculoItemView.findViewById(R.id.loginUsuarioItem);
            nomeCompletoUsuario = (TextView) usuarioVinculoItemView.findViewById(R.id.nomeCompletoUsuarioItem);
            confirmarButton = (Button) usuarioVinculoItemView.findViewById(R.id.confirmar_vinculo_button);
            excluirButton = (Button) usuarioVinculoItemView.findViewById(R.id.excluir_vinculo_button);
        }
    }

    @Override
    public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    @Override
    public VinculosUsuarioAluno onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contexto).inflate(R.layout.item_vinculo_com_responsavel, parent, false);
        VinculosUsuarioAluno viewHolder = new VinculosUsuarioAluno(v);
        v.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VinculosUsuarioAluno viewHolder, final int posicao) {
        final Usuario usuario = usuarios.get(posicao);

        Picasso.with(contexto)
                .load(usuario.getFotoURL())
                .into(viewHolder.fotoURLUsuario);

        viewHolder.loginUsuario.setText(usuario.getLogin());
        viewHolder.nomeCompletoUsuario.setText(funcoes.converterLetrasNome(usuario.getNome()));

        Log.d("ONBIND:", usuario.toString());

        if (usuariosComSolicitacaoVinculoRecebida.contains(usuario)) {
            viewHolder.confirmarButton.setVisibility(View.VISIBLE);
            viewHolder.excluirButton.setVisibility(View.VISIBLE);
            viewHolder.confirmarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmarButtonClickListener.onButtonClick(usuario);
                    //usuariosComQuemTemVinculo.add(usuario);
                    //usuariosComSolicitacaoVinculoRecebida.remove(usuario);
                }
            });
            viewHolder.excluirButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    excluirButtonClickListener.onButtonClick(usuario);
                    usuarios.remove(usuario);
                }
            });
        } else {
            viewHolder.confirmarButton.setVisibility(View.GONE);
            viewHolder.excluirButton.setVisibility(View.GONE);
        }
    }

    public void adicionarUsuario(Usuario usuariosComAlgumTipoVinculo,
                                 int tipoVinculo) {
        if (tipoVinculo == Constantes.FLAG_USUARIOS_SOLICITACAO_VINCULO) {
            usuariosComSolicitacaoVinculoRecebida.add(usuariosComAlgumTipoVinculo);
        } else if (tipoVinculo == Constantes.FLAG_USUARIOS_VINCULO) {
            usuariosComQuemTemVinculo.add(usuariosComAlgumTipoVinculo);
        }
        Log.d("ADICIONOU:", "SOLICITAÇÂO DE VÍNCULO" + usuariosComSolicitacaoVinculoRecebida.size());
        Log.d("ADICIONOU:", "VÍNCULO " + usuariosComQuemTemVinculo.size());
        usuarios.add(usuariosComAlgumTipoVinculo);
    }

    public void confirmarVinculo(Usuario usuario) {
        usuariosComSolicitacaoVinculoRecebida.remove(usuario);
        usuariosComQuemTemVinculo.add(usuario);
        usuarios.clear();
        usuarios.addAll(usuariosComSolicitacaoVinculoRecebida);
        usuarios.addAll(usuariosComQuemTemVinculo);
    }

    public void excluirSolcitacaoVinculoUsuario(Usuario usuariosComAlgumTipoVinculo) {
        usuariosComSolicitacaoVinculoRecebida.remove(usuariosComAlgumTipoVinculo);
        usuarios.remove(usuariosComAlgumTipoVinculo);
    }

    public void excluirVinculoUsuario(Usuario usuariosComAlgumTipoVinculo) {
        usuariosComQuemTemVinculo.remove(usuariosComAlgumTipoVinculo);
        usuarios.remove(usuariosComAlgumTipoVinculo);
    }

}