package br.com.projectmapes.dao;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocalizacaoDAO {

    private CollectionReference localizacoesCollectionReference;

    public LocalizacaoDAO(){
        localizacoesCollectionReference = FirebaseFirestore.getInstance()
                .collection("localizacoes");
    }

    public CollectionReference getLocalizacoesCollectionReference() {
        return localizacoesCollectionReference;
    }
}
