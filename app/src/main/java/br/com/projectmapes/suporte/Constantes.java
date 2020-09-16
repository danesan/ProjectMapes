package br.com.projectmapes.suporte;

import android.content.Context;
import android.content.Intent;

import br.com.projectmapes.R;

public class Constantes {

    public static final String NOME_APP = "Project MAPES";
    public static final String TELA_VINCULOS = "Vínculos";
    public static final String USUARIO_RESPONSAVEL = "responsável";
    public static final String USUARIO_ALUNO = "aluno";

    public static final String KEY_SHARED_PREFERENCES = "configuracoes";
    public static final String KEY_PREF_HORARIO_INICIAL = "horario_inicio";
    public static final String KEY_PREF_HORARIO_FINAL = "horario_fim";
    public static final String KEY_PREF_PERIODO_SOLICITACAO_LOCALIZACAO = "periodo_solicitacao_localizacao";

    //KEYS PARA A CLASSE SeekbarPreference
    public static final int VALOR_MINIMO_SEEKBAR = 15;
    public static final int VALOR_MAXIMO_SEEKBAR = 600;
    public static final int VALOR_PADRAO_SEEKBAR = 60;
    public static final int VALOR_INCREMENTO_SEEKBAR = 15;

    //KEYS PARA A CLASSE GeofenceDB
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_RAIO = "raio";
    public static final String KEY_DURACAO_EXPIRACAO = "duracao_expiracao";
    public static final String KEY_TIPO_TRANSICAO = "tipo_transicao";
    public static final String KEY_PREFIX = "key";
    public static final long VALOR_INVALIDO_LONG = -999l;
    public static final float VALOR_INVALIDO_FLOAT = -999.0f;
    public static final int VALOR_INVALIDO_INT = -999;

    //KEYS PARA COORDENADAS DO IFAM
    public static final double KEY_LATITUDE_IFAM = -3.134056; //eixo y
    public static final double KEY_LONGITUDE_IFAM = -60.012765; //eixo x
    public static final float KEY_RAIO_FENCE_IFAM = 78f;

    //KEYS PARA SEREM PASSADAS NAS INTENTS
    public static final String KEY_USUARIO_EXTRA = "usuario";
    public static final String KEY_DATA_STRING_EXTRA = "data_string";
    public static final String KEY_HORARIO_STRING_EXTRA = "horario_string";
    public static final String KEY_MOVIMENTACAO_EXTRA = "movimentacao";
    public static final String KEY_OPCAO_SLEECIONADA_MENU_LATERAL = "opcao_selecionada_menu_lateral";

    //KEYS PARA FIREBASEDATABASE, FIREBASEAUTH
    public static final String KEY_USUARIO_FIREBASE_LOGADO_EMAIL = "usuario_firebase_logado_email";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_SENHA = "usuario_firebase_logado_senha";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_LOGIN = "usuario_firebase_logado_login";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_NOME = "usuario_firebase_logado_nome";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_PERFIL = "usuario_firebase_logado_perfil";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_FIREBASETOKEN = "usuario_firebas_logado_firebasetoken";
    public static final String KEY_USUARIO_FIREBASE_JA_LOGADO = "usuario_firebase_ja_logado";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_FOTO_URL = "usuario_firebase_foto_url";
    public static final String KEY_USUARIO_FIREBASE_LOGADO_UID = "usuario_firebase_uid";

    public static final int FLAG_USUARIO_REPONSAVEL = 12;
    public static final int FLAG_USUARIO_ALUNO = 35;

    public static final String ACAO_ACEITAR_VINCULO = "confirmar_vinculo";
    public static final String ACAO_REJEITAR_VINCULO = "rejeitar_vinculo";
    public static final int FLAG_USUARIOS_VINCULO = 329;
    public static final int FLAG_USUARIOS_SOLICITACAO_VINCULO = 675;
    public static final String EXTRA_TIPO = "tipo_menu";

    public static String NOTIFICACAO_CHANNEL_ID = "channel_notificacao";

    public static String URL_GOOGLE_API_ROTAS_JSON = "http://maps.googleapis.com/maps/api/directions/json?" +
            "origin=%f,%f&destination=%f,%f&" + "sensor=true&mode=walking";

}
