package br.com.projectmapes.suporte;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.projectmapes.R;
import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;

public class NotificacaoRecyclerViewAdapter extends RecyclerView.Adapter<NotificacaoRecyclerViewAdapter.NotificacaoViewHolder> {

    private Context contexto;
    private List<ItemNotificacaoViewHolder> notificacoes;
    private OnClickListener onClickListener;

    public NotificacaoRecyclerViewAdapter(Context contexto, List<ItemNotificacaoViewHolder> notificacoes){
        this.contexto = contexto;
        this.notificacoes = notificacoes;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void selecionarNotificacao(View view, int posicao, ItemNotificacaoViewHolder notificacao);
    }

    @NonNull
    @Override
    public NotificacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(contexto).inflate(R.layout.item_notificacao, parent, false);
        NotificacaoViewHolder notificacaoViewHolder = new NotificacaoViewHolder(layout);
        notificacaoViewHolder.fotoUsuario = layout.findViewById(R.id.image_perfil_notificacao);
        notificacaoViewHolder.login = layout.findViewById(R.id.login_usuario_notificacao);
        notificacaoViewHolder.data = layout.findViewById(R.id.data_notificacao);
        notificacaoViewHolder.textoNotificacao = layout.findViewById(R.id.texto_notificacao);

        layout.setTag(notificacaoViewHolder);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickListener != null){
                    NotificacaoViewHolder viewHolder = (NotificacaoViewHolder) v.getTag();
                    int posicao = viewHolder.getAdapterPosition();
                    onClickListener.selecionarNotificacao(v, posicao, notificacoes.get(posicao));
                }
            }
        });
        return notificacaoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoViewHolder holder, int position) {
        ItemNotificacaoViewHolder notificacao = notificacoes.get(position);
        Funcoes funcoes = new Funcoes();
        Picasso.with(contexto).load(notificacao.getFotoUsuario()).into(holder.fotoUsuario);
        holder.login.setText(notificacao.getLogin());
        holder.data.setText(funcoes.converterDataMilisegundosEmString(notificacao.getDataHora()));

        if(notificacao.getMovimentacao().equals("entrada")){
            holder.textoNotificacao.setText("Chegou às " + funcoes.converterHorarioMilisegundosEmString(notificacao.getDataHora()));
        } else {
            holder.textoNotificacao.setText("Saiu às " + funcoes.converterHorarioMilisegundosEmString(notificacao.getDataHora()));
        }
    }

    @Override
    public int getItemCount() {
        return notificacoes != null ? notificacoes.size() : 0;
    }

    public static class NotificacaoViewHolder extends RecyclerView.ViewHolder{
        public ImageView fotoUsuario;
        public TextView login;
        public TextView data;
        public TextView textoNotificacao;

        public NotificacaoViewHolder(View parent){
            super(parent);
        }
    }

}
