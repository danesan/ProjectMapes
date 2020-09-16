package br.com.projectmapes.dao;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.projectmapes.modelo.NotificacaoSolicitacaoVinculo;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;

public class SolicitacaoVinculoDAO {

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference solicitacoVinculoCollectionReference;
    private List<Usuario> usuarios;
    private String idReferenceSolicitacaoVinculo;

    public SolicitacaoVinculoDAO() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        solicitacoVinculoCollectionReference = firebaseFirestore.collection("solicitacoes_vinculos");
    }

    public CollectionReference getSolicitacoVinculoCollectionReference() {
        return solicitacoVinculoCollectionReference;
    }

    public void adicionarSolicitacaoVinculo(final SolicitacaoVinculo solicitacaoVinculo, final String alunoFirebaseToke){
        solicitacoVinculoCollectionReference.add(solicitacaoVinculo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        solicitacaoVinculo.setId(documentReference.getId());
                        Log.d("Document_REF", documentReference.toString());
                        documentReference.set(solicitacaoVinculo);

                        long dataHorario = System.currentTimeMillis();

                        NotificacaoSolicitacaoVinculo notificacaoSolicitacaoVinculo =
                                new NotificacaoSolicitacaoVinculo(dataHorario, solicitacaoVinculo.getUidUsuarioAluno(),
                                        solicitacaoVinculo.getUidUsuarioResponsavel(), solicitacaoVinculo.getId());

                        NotificacaoDAO notificacaoDAO = new NotificacaoDAO();
                        notificacaoDAO.getNotificacacoesSolicitacoesVinculosReference()
                                .document(alunoFirebaseToke)
                                .set(notificacaoSolicitacaoVinculo);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERRO", e.getMessage());
                    }
                });
    }

    public void removerSolicitacaoVinculo(SolicitacaoVinculo solicitacaoVinculo){
        solicitacoVinculoCollectionReference.document(solicitacaoVinculo.getId()).delete();
    }

}
