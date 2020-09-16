package br.com.projectmapes.dao;

import android.content.Context;
import android.content.SharedPreferences;

import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.suporte.Constantes;

public class SharedPreferencesDAO {

    private Usuario usuario;
    private SharedPreferences sharedPreferences;

    public SharedPreferencesDAO(Context context) {
        sharedPreferences = context.getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
    }

    public Usuario getUsuarioLogado(){
        String email = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_EMAIL,"");
        String firebaseToken = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_FIREBASETOKEN,"");
        String fotoURL = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_FOTO_URL,"");
        String login = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_LOGIN,"");
        String nome = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_NOME,"");
        String perfil = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_PERFIL,"");
        String uid = sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_UID,"");

        if(email.isEmpty()){
            return null;
        }
        Usuario usuario = new Usuario(email, firebaseToken, fotoURL, login,
                nome, perfil, uid);
        usuario.setFirebaseToken(firebaseToken);
        return usuario;
    }

    public void atualizarUsuarioLogin(Usuario usuario, String senha){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_EMAIL, usuario.getEmail());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_FIREBASETOKEN, usuario.getFirebaseToken());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_FOTO_URL, usuario.getFotoURL());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_LOGIN, usuario.getLogin());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_NOME, usuario.getNome());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_PERFIL, usuario.getPerfil());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_UID, usuario.getUid());
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_SENHA, senha);
        editor.putBoolean(Constantes.KEY_USUARIO_FIREBASE_JA_LOGADO, true);
        editor.commit();
    }

    public void atualizarFirebaseTokenUsuarioLogado(String firebaseToken){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_FIREBASETOKEN, firebaseToken);
        editor.commit();
    }

    public void atualizarUsuarioLogout(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constantes.KEY_USUARIO_FIREBASE_JA_LOGADO, false);
        editor.commit();
    }

    public boolean usarioJalogado(){
        return sharedPreferences.getBoolean(Constantes.KEY_USUARIO_FIREBASE_JA_LOGADO, false);
    }

    public String getPerfilUsarioJalogado(){
        return sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_PERFIL, "");
    }

    public String getEmailUsuarioJaLogadoAlgumaVez(){
        return sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_EMAIL, "");
    }

    public String getSenhaUsuarioJaLogadoAlgumaVez(){
        return sharedPreferences.getString(Constantes.KEY_USUARIO_FIREBASE_LOGADO_SENHA, "");
    }

    public long getIntervaloLocationRequest(){
        return sharedPreferences.getLong( Constantes.KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO, 0);
    }

}
