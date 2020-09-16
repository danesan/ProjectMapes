package br.com.projectmapes.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoSolicitacaoVinculo;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;

public class MovimentacaoGeofenceDAO {

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference movimentacoesGeofenceCollectionReference;

    public MovimentacaoGeofenceDAO() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        movimentacoesGeofenceCollectionReference = firebaseFirestore.collection("movimentacoes_geofence");
    }

    public CollectionReference getMovimentacoesGeofenceCollectionReference() {
        return movimentacoesGeofenceCollectionReference;
    }

    public void adicionarMovimentacaoGeofence(final MovimentacaoGeofence movimentacaoGeofence, final Usuario usuarioResponsavel){
        movimentacoesGeofenceCollectionReference.document(movimentacaoGeofence.getUidAluno())
                .collection("movimentacoes_salvas").add(movimentacaoGeofence)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        long dataHorario = System.currentTimeMillis();
                        NotificacaoMovimentacaoGeofence notificacaoMovimentacaoGeofence =
                                new NotificacaoMovimentacaoGeofence(dataHorario, movimentacaoGeofence.getAcao(),
                                        movimentacaoGeofence.getUidAluno(), usuarioResponsavel.getUid());

                        NotificacaoDAO notificacaoDAO = new NotificacaoDAO();
                        notificacaoDAO.getNotificacoesMovimentacaoGeogenceCollectionReference()
                                .document(usuarioResponsavel.getFirebaseToken())
                                .set(notificacaoMovimentacaoGeofence);
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
