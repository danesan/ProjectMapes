package br.com.projectmapes.suporte;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.com.projectmapes.R;
import br.com.projectmapes.TelaPrincipalUsuarioResponsavelActivity;
import br.com.projectmapes.dao.SharedPreferencesDAO;
import br.com.projectmapes.dao.SolicitacaoVinculoDAO;
import br.com.projectmapes.dao.UsuarioDAO;
import br.com.projectmapes.dao.VinculoDAO;
import br.com.projectmapes.modelo.SolicitacaoVinculo;
import br.com.projectmapes.modelo.Usuario;
import br.com.projectmapes.modelo.Vinculo;

public class NotificacoesFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private FirebaseAuth firebaseAuth;
    private NotificacaoReceiver notificacaoReceiver;
    private Usuario usuarioLogado;
    private SharedPreferencesDAO sharedPreferencesDAO;
    private String idSolicitacaoVinculo;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        firebaseAuth = FirebaseAuth.getInstance();
        //firebaseAuth.signInWithEmailAndPassword(usuarioLogado.getEmail(), usuarioLogado.getSenha());

        String usuarioId = firebaseAuth.getCurrentUser().getUid();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        usuarioDAO.getUsuarioCollectionReference()
                .document(usuarioId).update("firebaseToken", token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //enviarIdRegistrationParaServidor(token);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificacaoReceiver = new NotificacaoReceiver();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("Teste", remoteMessage.getMessageId());
        // Verifica se a mensagem não veio nula (em caso de erro)
        //if (remoteMessage.getData().size() > 0) {
        //RemoteMessage.Notification notification = remoteMessage.getNotification();
        //Log.i("Notification", notification.toString());

        Map<String, String> dados = remoteMessage.getData();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        if(dados.get("tipo").equals("movimentacao_geofence")){
            String uidResponsavel = dados.get("uidResponsavel");
            final long dataHorario = Long.parseLong(dados.get("dataHorario"));
            final String movimentacao = dados.get("movimentacao");
            String uidAluno = dados.get("uidAluno");

            Log.d("uidResponsavel", uidResponsavel);
            Log.d("dataHorario", dataHorario + "");
            Log.d("movimentacao", movimentacao);
            Log.d("uidAluno", uidAluno);

            usuarioDAO.getUsuarioCollectionReference()
                    .document(uidAluno)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Usuario usuarioAluno = documentSnapshot.toObject(Usuario.class);

                            CarregarNotificacaoImagemPerfilUsuario carregarNotificacaoImagemPerfilUsuario =
                                    new CarregarNotificacaoImagemPerfilUsuario();
                            carregarNotificacaoImagemPerfilUsuario.setMovimentacao(movimentacao);
                            carregarNotificacaoImagemPerfilUsuario.setDataHorario(dataHorario);
                            carregarNotificacaoImagemPerfilUsuario.execute(usuarioAluno);

                        }
                    });

        } else {
            String uidResponsavel = dados.get("sender");
            final String uidAluno = dados.get("title");
            final String dataHora = dados.get("body");
            idSolicitacaoVinculo = dataHora.split(" ")[1];
            sharedPreferencesDAO = new SharedPreferencesDAO(this);

            usuarioDAO.getUsuarioCollectionReference()
                    .document(uidResponsavel)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Usuario usuarioResponsalvel = documentSnapshot.toObject(Usuario.class);

                        /*
                        Intent notificacoesIntent = new Intent(getApplicationContext() , TelaVinculosAlunoActivity.class);
                        notificacoesIntent.putExtra("usuario", usuarioResponsalvel);

                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                0, notificacoesIntent, 0);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        String notificationChaneelId = "channel_notificacao";

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            NotificationChannel notificationChannel =
                                    new NotificationChannel(notificationChaneelId, "Notificacao solictiacao vinculo",
                                            NotificationManager.IMPORTANCE_DEFAULT);

                            notificationChannel.setDescription("Channel description");
                            notificationChannel.enableLights(true);
                            notificationChannel.setLightColor(Color.RED);
                            //notificationChannel.setVibrationPattern();

                            notificationManager.createNotificationChannel(notificationChannel);
                        }
                        Funcoes funcoes = new Funcoes();
                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(getApplicationContext(), notificationChaneelId);
                        builder.setAutoCancel(true)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(usuarioResponsalvel.getLogin() + " (" + funcoes.converterLetrasNome(usuarioResponsalvel.getNome()) + ")")
                                .setContentText("Enviou uma solicitação de vínculo!")
                                .setContentIntent(pendingIntent);

                        //notificationManager.notify(1, builder.build());*/

                            (new NotificationUtils()).criarNotificacaoSolictacaoVinculo(getBaseContext(), usuarioResponsalvel);
                            registerReceiver(notificacaoReceiver, new IntentFilter(Constantes.ACAO_ACEITAR_VINCULO));
                            registerReceiver(notificacaoReceiver, new IntentFilter(Constantes.ACAO_REJEITAR_VINCULO));
                        }
                    });

        }
        //Fazer algo com a mensagem
        //}
        // Check if message contains a notification payload.
    }

    class NotificacaoReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(final Context context, Intent intent) {
            unregisterReceiver(notificacaoReceiver);

            final Usuario usuarioResponsavel = intent.getParcelableExtra(Constantes.KEY_USUARIO_EXTRA);
            usuarioLogado = sharedPreferencesDAO.getUsuarioLogado();

            if(intent.getAction().equals(Constantes.ACAO_ACEITAR_VINCULO)){
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                Log.d("MSG+RECEBIDA", usuarioLogado.toString());

                if (remoteInput != null) {
                    final Funcoes funcoes = new Funcoes();
                    final String senha = funcoes.criptografarSenha((String) remoteInput.getCharSequence(Constantes.ACAO_ACEITAR_VINCULO));
                    final Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_blue_24dp);

                    final SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
                    solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                            .document(idSolicitacaoVinculo)
                            //.whereEqualTo("loginUsuarioResponsavel", usuarioResponsavel.getLogin())
                            //.whereEqualTo("loginUsuarioAluno", usuarioLogado.getLogin())
                            //.limit(1)
                            .get()
                            //.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    SolicitacaoVinculo solicitacaoVinculo = documentSnapshot.
                                            toObject(SolicitacaoVinculo.class);

                                    NotificationCompat.Builder notificacaoResposta =
                                            new NotificationCompat.Builder(context, Constantes.NOTIFICACAO_CHANNEL_ID);

                                    if(solicitacaoVinculo.getSenha().equals(senha)){
                                        solicitacaoVinculoDAO.removerSolicitacaoVinculo(solicitacaoVinculo);

                                        VinculoDAO vinculoDAO = new VinculoDAO();
                                        vinculoDAO.adicionarVinculo(new Vinculo(usuarioResponsavel.getUid(), usuarioLogado.getUid()));

                                        notificacaoResposta
                                                .setSmallIcon(R.drawable.ic_notificacao)
                                                .setColor(context.getResources().getColor(R.color.cor_primaria_escura))
                                                .setContentText(getString(R.string.solicitacao_vinculo_confirmada))
                                                .setContentTitle(usuarioResponsavel.getLogin() + " (" + funcoes.converterLetrasNome(usuarioResponsavel.getNome()) + ")")
                                                .setLargeIcon(largeIcon)
                                                .setAutoCancel(true)
                                                .setLights(Color.BLUE, 1000, 5000);

                                    } else {
                                        PendingIntent pendingIntentNotificacao = (new NotificationUtils()).
                                                criarPendingIntent(context, Constantes.FLAG_USUARIO_ALUNO);

                                        notificacaoResposta
                                                .setSmallIcon(R.drawable.ic_notificacao)
                                                .setColor(context.getResources().getColor(R.color.cor_primaria_escura))
                                                .setContentText(getString(R.string.erro_senha_aceitar_soliitacao_vinculo))
                                                .setContentTitle(usuarioResponsavel.getLogin() + " (" + funcoes.converterLetrasNome(usuarioResponsavel.getNome()) + ")")
                                                .setContentIntent(pendingIntentNotificacao)
                                                //.setLargeIcon()
                                                .setAutoCancel(true)
                                                .setLights(Color.BLUE, 1000, 5000);
                                    }
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    notificationManager.notify(Constantes.FLAG_USUARIO_ALUNO, notificacaoResposta.build());

                                }
                            });
                }
            } else if(intent.getAction().equals(Constantes.ACAO_REJEITAR_VINCULO)) {

                final SolicitacaoVinculoDAO solicitacaoVinculoDAO = new SolicitacaoVinculoDAO();
                solicitacaoVinculoDAO.getSolicitacoVinculoCollectionReference()
                        .document(idSolicitacaoVinculo)
                        .delete();

                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Constantes.FLAG_USUARIO_ALUNO);
            }
        }
    }

    public static boolean isAppEmBackground(Context context) {
        boolean estaEmBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            estaEmBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;

            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                estaEmBackground = false;
            }
        }

        return estaEmBackground;
    }


    public class CarregarNotificacaoImagemPerfilUsuario extends AsyncTask<Usuario,Void,Void> {

        String movimentacao;
        long dataHorario;

        public void setMovimentacao(String movimentacao){
            this.movimentacao = movimentacao;
        }

        public void setDataHorario(long dataHorario){
            this.dataHorario = dataHorario;
        }


        @Override
        protected Void doInBackground(Usuario... usuarios) {
            Usuario usuarioAluno = usuarios[0];

            Intent notificacoesIntent = new Intent(getApplicationContext(),
                    TelaPrincipalUsuarioResponsavelActivity.class);
            notificacoesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    0, notificacoesIntent, 0);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String notificationChaneelId = "channel_notificacao";
            String notificationChannelNome = "notificacao_geofence";

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel notificationChannel =
                        new NotificationChannel(notificationChaneelId, notificationChannelNome,
                                NotificationManager.IMPORTANCE_HIGH);

                notificationChannel.setDescription("channel_descricao");
                notificationChannel.setLightColor(getApplicationContext().getResources().getColor(R.color.cor_primaria_escura));
                notificationManager.createNotificationChannel(notificationChannel);
            }

            String movimentacao_aluno;
            if(movimentacao.equals("entrada")){
                movimentacao_aluno = "Chegou ao";
            } else {
                movimentacao_aluno = "Saiu do";
            }

            Funcoes funcoes = new Funcoes();
            NotificationCompat.Builder notificacaoBuilder =
                    new NotificationCompat.Builder(getApplicationContext(),
                            Constantes.NOTIFICACAO_CHANNEL_ID);

            try{
                Bitmap imagemPerfilUsuario = Picasso.with(getApplicationContext())
                        .load(usuarioAluno.getFotoURL()).get();

                notificacaoBuilder
                        .setSmallIcon(R.drawable.ic_notificacao)
                        .setLargeIcon(imagemPerfilUsuario)
                        .setColor(getApplicationContext().getResources().getColor(R.color.cor_primaria_escura))
                        .setContentText(movimentacao_aluno + " IFAM às " + funcoes.converterHorarioMilisegundosEmString(dataHorario))
                        .setContentTitle(usuarioAluno.getLogin() + " (" + funcoes.converterLetrasNome(usuarioAluno.getNome()) + ")")
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setLights(Color.BLUE, 1000, 5000);

                notificationManager.notify(Constantes.FLAG_USUARIO_ALUNO, notificacaoBuilder.build());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
