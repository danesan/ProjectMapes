package br.com.projectmapes.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;
import br.com.projectmapes.modelo.NotificacaoSolicitacaoVinculo;

public class NotificacaoDAO {

    private CollectionReference notificacoesMovimentacaoGeogenceCollectionReference;
    private CollectionReference notificacacoesSolicitacoesVinculosReference;
    private CollectionReference movimentacoesGeofenceCollectionReference;

    public NotificacaoDAO(){
        notificacoesMovimentacaoGeogenceCollectionReference = FirebaseFirestore.getInstance()
                .collection("notificacoes_movimentacao_geofence");
        notificacacoesSolicitacoesVinculosReference = FirebaseFirestore.getInstance()
                .collection("notificacoes_solicitacoes_vinculos");
        movimentacoesGeofenceCollectionReference = FirebaseFirestore.getInstance()
                .collection("movimentacoes_geofence");
        }

    public CollectionReference getNotificacoesMovimentacaoGeogenceCollectionReference() {
        return notificacoesMovimentacaoGeogenceCollectionReference;
    }

    public CollectionReference getNotificacacoesSolicitacoesVinculosReference() {
        return notificacacoesSolicitacoesVinculosReference;
    }

    public CollectionReference getMovimentacoesGeofenceCollectionReference() {
        return movimentacoesGeofenceCollectionReference;
    }
}
