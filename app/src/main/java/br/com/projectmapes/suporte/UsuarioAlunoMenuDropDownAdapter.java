package br.com.projectmapes.suporte;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.R;
import br.com.projectmapes.modelo.Usuario;

public class UsuarioAlunoMenuDropDownAdapter extends ArrayAdapter<Usuario> {

    private List<Usuario> usuariosAluno;
    private List<Usuario> usuariosAlunoFiltrados = new ArrayList<>();
    private OnItemUsuarioClickListener onItemUsuarioClickListener;

    public UsuarioAlunoMenuDropDownAdapter(@NonNull Context context, List<Usuario> usuariosAluno) {
        super(context, 0, usuariosAluno);
        this.usuariosAluno = new ArrayList<>(usuariosAluno);
        usuariosAlunoFiltrados = new ArrayList<>(usuariosAluno);
    }

    public void setOnItemUsuarioClickListener(OnItemUsuarioClickListener onItemUsuarioClickListener){
        this.onItemUsuarioClickListener = onItemUsuarioClickListener;
    }

    public interface OnItemUsuarioClickListener {
        void selecionarUsuario(Usuario usuarioAluno);
    }

    @Override
    public int getCount() {
        return usuariosAlunoFiltrados.size();
    }

    @Override
    public Filter getFilter(){
        //return filtrarUsuarios;
        return new UsuarioAlunoFilter(this, usuariosAluno);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return carregarItemMenuDropDown(position, convertView, parent);
    }

    /*
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return carregarItemMenuDropDown(position, convertView, parent);
    }*/

    private View carregarItemMenuDropDown(int position, View itemUsuarioAlunoMenuDropDown, ViewGroup parent){
        if(itemUsuarioAlunoMenuDropDown == null){
            itemUsuarioAlunoMenuDropDown = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_usuario_rota, parent, false);
        }

        ImageView fotoPerfilUsuarioAluno = itemUsuarioAlunoMenuDropDown.findViewById(R.id.foto_usuario_aluno_item);
        TextView loginUsuarioaluno = itemUsuarioAlunoMenuDropDown.findViewById(R.id.login_usuario_aluno_item);
        TextView nomeUsuarioaluno = itemUsuarioAlunoMenuDropDown.findViewById(R.id.nome_completo_usuario_item);

        final Usuario usuarioAluno = getItem(position);

        if(usuarioAluno != null) {
            Picasso.with(getContext()).load(usuarioAluno.getFotoURL()).into(fotoPerfilUsuarioAluno);
            loginUsuarioaluno.setText(usuarioAluno.getLogin());
            nomeUsuarioaluno.setText(usuarioAluno.getNome());
        }

        itemUsuarioAlunoMenuDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemUsuarioClickListener != null){
                    onItemUsuarioClickListener.selecionarUsuario(usuarioAluno);
                }
            }
        });

        return itemUsuarioAlunoMenuDropDown;
    }

    private Filter filtrarUsuarios = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Usuario> sugestoesEncontradas = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                sugestoesEncontradas.addAll(usuariosAluno);
            } else {
                String filtroPattern = constraint.toString().toLowerCase().trim();

                for(Usuario usuario : usuariosAluno){
                    if(usuario.getLogin().toLowerCase().contains(filtroPattern)){
                        sugestoesEncontradas.add(usuario);
                    }
                }
            }

            results.values = sugestoesEncontradas;
            results.count = sugestoesEncontradas.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Usuario) resultValue).getLogin();
        }
    };

    private class UsuarioAlunoFilter extends Filter {

        UsuarioAlunoMenuDropDownAdapter adapter;
        List<Usuario> usuariosAluno;
        List<Usuario> usuariosAlunoFiltrados;

        public UsuarioAlunoFilter(UsuarioAlunoMenuDropDownAdapter adapter, List<Usuario> usuariosAluno) {
            super();
            this.adapter = adapter;
            this.usuariosAluno = usuariosAluno;
            this.usuariosAlunoFiltrados = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            usuariosAlunoFiltrados.clear();
            final FilterResults results = new FilterResults();

            Log.d("Entrou", "filtro");

            if(constraint == null || constraint.length() == 0){
                usuariosAlunoFiltrados.addAll(usuariosAluno);
            } else {
                final String filtroPattern = constraint.toString().toLowerCase().trim();

                for(Usuario usuario : usuariosAluno){
                    if(usuario.getLogin().toLowerCase().contains(filtroPattern)){
                        usuariosAlunoFiltrados.add(usuario);
                    }
                }
            }

            results.values = usuariosAlunoFiltrados;
            results.count = usuariosAlunoFiltrados.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.usuariosAlunoFiltrados.clear();
            adapter.usuariosAlunoFiltrados.addAll((List) results.values);
            adapter.notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Usuario) resultValue).getLogin();
        }
    }

}
