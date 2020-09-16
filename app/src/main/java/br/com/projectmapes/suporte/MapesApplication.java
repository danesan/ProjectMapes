package br.com.projectmapes.suporte;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.projectmapes.LoginActivity;
import br.com.projectmapes.dao.UsuarioDAO;

public class MapesApplication extends Application implements Application.ActivityLifecycleCallbacks {


    private void setOnline(boolean isOnline){
        try {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("MAPESAPLICATION", "uid:" + uid);
            if(uid != null){
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                usuarioDAO.getUsuarioCollectionReference()
                        .document(uid)
                        .update("online", isOnline);
            }
        } catch (NullPointerException ex) {
            Log.d("MAPESAPLICATION", "USUARIO NÂO AUTENTICADO");
            //exibir a tela de login
            //Intent telaLoginIntent = new Intent(getBaseContext(), LoginActivity.class);
            //startActivity(telaLoginIntent);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d("MAPESAPLICATION", activity.toString());
        setOnline(true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        setOnline(false);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
