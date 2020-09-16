package br.com.projectmapes.suporte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import br.com.projectmapes.dao.MovimentacaoGeofenceDAO;
import br.com.projectmapes.dao.NotificacaoDAO;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.MovimentacaoGeofence;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastRcv";
    private SharedPreferencesDAO sharedPreferencesDAO;
    private VinculoDAO vinculoDAO;
    private MovimentacaoGeofenceDAO movimentacaoGeofenceDAO;
    private UsuarioDAO usuarioDAO;

    @Override
    public void onReceive(final Context context, Intent intent) {
        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        sharedPreferencesDAO = new SharedPreferencesDAO(context);
        final Usuario usuarioAluno = sharedPreferencesDAO.getUsuarioLogado();

        Log.d("BROADCAST_REC", "GEOFENCE");
        Log.d("USUARIO", usuarioAluno.toString());

        if(geofencingEvent.hasError()){
            int errorCode = geofencingEvent.getErrorCode();
            Toast.makeText(context, "Erro mo serviço de localização:  " + errorCode,
                    Toast.LENGTH_SHORT).show();
        } else {
            vinculoDAO = new VinculoDAO();
            movimentacaoGeofenceDAO = new MovimentacaoGeofenceDAO();
            usuarioDAO = new UsuarioDAO();

            vinculoDAO.getVinculosCollectionReference().whereEqualTo("uidUsuarioAluno", usuarioAluno.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    long dataHora = System.currentTimeMillis();
                    int transicao = geofencingEvent.getGeofenceTransition();
                    String movimentacao = "";

                    if(transicao == Geofence.GEOFENCE_TRANSITION_ENTER){
                        movimentacao = "entrada";
                    } else if(transicao == Geofence.GEOFENCE_TRANSITION_EXIT){
                        movimentacao = "saida";
                    } else {
                        //acao = "erro no Geofence: " + transicao;
                    }

                    Log.d("MOVIMENTACAO", movimentacao + transicao);

                    for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                        Vinculo vinculo = documentSnapshot.toObject(Vinculo.class);
                        final MovimentacaoGeofence movimentacaoGeofence = new MovimentacaoGeofence(
                                dataHora, movimentacao, vinculo.getUidUsuarioAluno());

                        usuarioDAO.getUsuarioCollectionReference().document(vinculo.getUidUsuarioResponsavel())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Usuario usuarioResponsavel = documentSnapshot.toObject(Usuario.class);
                                movimentacaoGeofenceDAO.adicionarMovimentacaoGeofence(movimentacaoGeofence, usuarioResponsavel);
                            }
                        });
                    }
                }
            });
        }
    }
}
