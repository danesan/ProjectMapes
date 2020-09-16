package br.com.projectmapes.modelo;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.Objects;

//implements Parcelable: para poder passar objetos entre as activities
public class Usuario implements Parcelable {

    private String email;
    private String firebaseToken;
    private String fotoURL;
    private String login;
    private String nome;
    private String perfil;
    private String uid;
    private boolean online;

    public Usuario() {
    }

    public Usuario(String email, String firebaseToken, String fotoURL, String login,
                   String nome, String perfil, String uId) {
        this.email = email;
        this.firebaseToken = firebaseToken;
        this.fotoURL = fotoURL;
        this.login = login;
        this.nome = nome;
        this.perfil = perfil;
        this.uid = uId;
    }

    private Usuario(Parcel origemParcel){
        email = origemParcel.readString();
        firebaseToken = origemParcel.readString();
        fotoURL = origemParcel.readString();
        login = origemParcel.readString();
        nome = origemParcel.readString();
        perfil = origemParcel.readString();
        uid = origemParcel.readString();
        online = origemParcel.readInt() == 1;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoURL() {
        return fotoURL;
    }

    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public static final Parcelable.Creator<Usuario> CREATOR = new Parcelable.Creator<Usuario>(){
        @Override
        public Usuario createFromParcel(Parcel source) {
            return new Usuario(source);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(firebaseToken);
        dest.writeString(fotoURL);
        dest.writeString(login);
        dest.writeString(nome);
        dest.writeString(perfil);
        dest.writeString(uid);
        dest.writeInt(online ? 1 : 0);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "firebaseToken='" + firebaseToken + '\'' +
                ", login='" + login + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", fotoURL=" + fotoURL +
                ", perfil='" + perfil + '\'' +
                ", uId=" + uid +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(email, usuario.email) &&
                Objects.equals(login, usuario.login);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(email, login);
    }
}
