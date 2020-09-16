package br.com.projectmapes.suporte;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsuarioAlunoAdapter extends RecyclerView.Adapter<UsuarioAlunoAdapter.UsuarioAlunoViewHolder> {


    public static class UsuarioAlunoViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;

        public UsuarioAlunoViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public UsuarioAlunoAdapter.UsuarioAlunoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioAlunoAdapter.UsuarioAlunoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
