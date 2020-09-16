package br.com.projectmapes.dao;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.projectmapes.modelo.Usuario;

public class UsuarioDAO {

    //private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usuarioDatabaseReference;
    private CollectionReference usuarioCollectionReference;
    private FirebaseStorage firebaseStorage;
    private List<Usuario> usuarios;

    public UsuarioDAO() {
        //firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //usuarioDatabaseReference = firebaseDatabase.getReference("usuarios");
        usuarioCollectionReference = firebaseFirestore.collection("usuarios");
        usuarios = new ArrayList<Usuario>();
    }

    public void cadastrarUsuario(Usuario usuario, FirebaseUser firebaseUser){
        //usuarioDatabaseReference.child(firebaseUser.getUid()).setValue(usuario);
        usuarioCollectionReference
                //.add(usuario)
                .document(firebaseUser.getUid())
                .set(usuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERRO", e.getMessage());
                    }
                });
    }

    public CollectionReference getUsuarioCollectionReference(){
        return usuarioCollectionReference;
    }

    public StorageReference getFotosPerfilUsuarioStorageReference(String login) {
        return firebaseStorage.getReference("fotos_perfil_usuarios/" + login + ".png");
    }
}
