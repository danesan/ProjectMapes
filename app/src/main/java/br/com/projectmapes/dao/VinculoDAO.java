package br.com.projectmapes.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;

public class VinculoDAO {


    private FirebaseFirestore firebaseFirestore;
    private CollectionReference vinculosCollectionReference;
    private List<Usuario> usuarios;

    public VinculoDAO() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        vinculosCollectionReference = firebaseFirestore.collection("vinculos");
    }

    public CollectionReference getVinculosCollectionReference() {
        return vinculosCollectionReference;
    }

    public void adicionarVinculo(final Vinculo vinculo){
        vinculosCollectionReference.add(vinculo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        vinculo.setId(documentReference.getId());
                        documentReference.set(vinculo);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERRO", e.getMessage());
                    }
                });
    }
}
