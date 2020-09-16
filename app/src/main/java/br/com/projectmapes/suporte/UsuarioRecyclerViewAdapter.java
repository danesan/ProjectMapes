package br.com.projectmapes.suporte;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.R;
import br.com.projectmapes.modelo.Usuario;

public class UsuarioRecyclerViewAdapter extends RecyclerView.Adapter<UsuarioRecyclerViewAdapter.UsuarioViewHolder> {

    private Context contexto;
    private List<Usuario> todosUsuarios;
    private List<Usuario> vinculos;
    private List<Usuario> solicitacoesVinculoEnviadas;
    private OnItemClickListener listener;
    private Funcoes funcoes;

    public UsuarioRecyclerViewAdapter(Context contexto) {
        this.contexto = contexto;
        todosUsuarios = new ArrayList<>();
        vinculos = new ArrayList<>();
        solicitacoesVinculoEnviadas = new ArrayList<>();
        funcoes = new Funcoes();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int posicao, Usuario usuario);
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder{
        public ImageView fotoURLUsuario;
        public TextView loginUsuario;
        public TextView nomeCompletoUsuario;
        public TextView solicitacaoEnviada;
        public ImageView adicionaSolicitacaoVinculo;

        public UsuarioViewHolder(View usuarioItemView){
            super(usuarioItemView);
            fotoURLUsuario = (ImageView) usuarioItemView.findViewById(R.id.fotoUsuarioItem);
            loginUsuario = (TextView) usuarioItemView.findViewById(R.id.loginUsuarioItem);
            nomeCompletoUsuario = (TextView) usuarioItemView.findViewById(R.id.nomeCompletoUsuarioItem);
            solicitacaoEnviada = (TextView) usuarioItemView.findViewById(R.id.solicitacaoEnviadaTextView);
            adicionaSolicitacaoVinculo = (ImageView) usuarioItemView.findViewById(R.id.adicionarSolicitacaoVinculoButton);
        }
    }

    @Override
    public int getItemCount(){
        Log.d("ENTROU", todosUsuarios.size() + " ITEMCOUNT");
        return todosUsuarios.size();
    }

    @Override
    public UsuarioViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(contexto).inflate(R.layout.item_usuario_vinculo, parent, false);
        UsuarioViewHolder viewHolder = new UsuarioViewHolder(v);
        v.setTag(viewHolder);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    UsuarioViewHolder viewHolder = (UsuarioViewHolder) view.getTag();
                    int posicao = viewHolder.getAdapterPosition();
                    //listener.onItemClick(view, posicao, todosUsuariosEPesquisa.get(posicao));
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UsuarioViewHolder viewHolder, final int posicao) {
        final Usuario usuario = todosUsuarios.get(posicao);
        Log.d("ENTROU", todosUsuarios.size() + " ONBIND");
        Log.d("ENTROU", usuario.toString());

        Picasso.with(contexto)
                .load(usuario.getFotoURL())
                .into(viewHolder.fotoURLUsuario);

        viewHolder.loginUsuario.setText(usuario.getLogin());
        viewHolder.nomeCompletoUsuario.setText(funcoes.converterLetrasNome(usuario.getNome()));

        if(solicitacoesVinculoEnviadas.contains(usuario)){
            Log.d("ENTROU",  "SOLICITAÇÃO DE VÍNCULO");
            viewHolder.itemView.setClickable(false);
            viewHolder.solicitacaoEnviada.setVisibility(View.VISIBLE);
            viewHolder.adicionaSolicitacaoVinculo.setVisibility(View.VISIBLE);
            viewHolder.adicionaSolicitacaoVinculo.setImageResource(R.drawable.ic_done_blue_24dp);
            //viewHolder.adicionaSolicitacaoVinculo.getLayoutParams().height = 36;
            //viewHolder.adicionaSolicitacaoVinculo.getLayoutParams().width = 36;
        } else if(vinculos.contains(usuario)){
            Log.d("ENTROU",  "VÍNCULO");
            viewHolder.itemView.setClickable(true);
            viewHolder.adicionaSolicitacaoVinculo.setVisibility(View.GONE);
            viewHolder.solicitacaoEnviada.setVisibility(View.GONE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contexto, "Clicou no seu amigo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d("ENTROU",  "PESQUISA");
            viewHolder.adicionaSolicitacaoVinculo.setImageResource(R.drawable.ic_person_add_blue_24dp);
            viewHolder.adicionaSolicitacaoVinculo.setVisibility(View.VISIBLE);
            viewHolder.itemView.setClickable(true);
            viewHolder.solicitacaoEnviada.setVisibility(View.INVISIBLE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, posicao, usuario);
                }
            });
        }
    }

    public void adicionarUsuariosPesquisa(List<Usuario> usuarios) {
        todosUsuarios.clear();
        todosUsuarios.addAll(usuarios);
    }

    public void adicionarNaListaSolicitacaoVinculoEnviada(Usuario usuario){
        solicitacoesVinculoEnviadas.add(usuario);
        todosUsuarios.add(usuario);
    }

    public void adicionarVinculo(Usuario usuario){
        vinculos.add(usuario);
        todosUsuarios.add(usuario);
    }

    public void removerSolicitacaoVinculo(Usuario usuario){
        Log.d("ANTES", solicitacoesVinculoEnviadas.size() + " t");
        solicitacoesVinculoEnviadas.remove(usuario);
        Log.d("DEPOIS", solicitacoesVinculoEnviadas.size() + " t");
        todosUsuarios.remove(usuario);
    //    todosUsuarios.add(usuario);
    }

    public void resetarListaUsuarios(){
        todosUsuarios.clear();
        todosUsuarios.addAll(solicitacoesVinculoEnviadas);
        todosUsuarios.addAll(vinculos);
    }

    public void adicionarUsuarioSolicitacaVinculoEnviada(Usuario usuario){
        solicitacoesVinculoEnviadas.add(usuario);
        Log.d("ANTES", todosUsuarios.size() + " t");
        resetarListaUsuarios();
        Log.d("DEPOIS", todosUsuarios.size() + " t");
    }
}

