package br.com.projectmapes.suporte;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.projectmapes.R;
import br.com.projectmapes.TelaPrincipalUsuarioAlunoActivity;
import br.com.projectmapes.TelaVinculosAlunoFragment;
import br.com.projectmapes.modelo.Usuario;

public class NotificationUtils {

    private Usuario usuarioResponsavel;
    private Context context;

    public void criarNotificacaoSolictacaoVinculo(Context context, Usuario usuarioResponsavel){
        Funcoes funcoes = new Funcoes();
        this.usuarioResponsavel = usuarioResponsavel;
        this.context = context;

        PrepararNotificacaoTask prepararnotificacaoTask = new PrepararNotificacaoTask(usuarioResponsavel.getFotoURL());
        prepararnotificacaoTask.execute();
    }

    public PendingIntent criarPendingIntent(Context context, int flag){
        Intent resultIntent = new Intent(context, TelaVinculosAlunoFragment.class);
        //resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TelaPrincipalUsuarioAlunoActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(flag, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private void exibirNotificacao(Context context, Usuario usuarioResponsavel, Bitmap largeIconNotificacao){
        int flag = Constantes.FLAG_USUARIO_ALUNO;

        Uri uriSom = Uri.parse("android.resource://" + context.getPackageName() + "/raw/som_notificacao.mp3");

        Intent confirmarVinculoIntent = new Intent(Constantes.ACAO_ACEITAR_VINCULO);
        confirmarVinculoIntent.putExtra(Constantes.KEY_USUARIO_EXTRA, usuarioResponsavel);

        Intent rejeitarVinculoIntent = new Intent(Constantes.ACAO_REJEITAR_VINCULO);
        rejeitarVinculoIntent.putExtra(Constantes.KEY_USUARIO_EXTRA, usuarioResponsavel);

        PendingIntent pendingIntentNotificacao = criarPendingIntent(context, flag);
        PendingIntent confirmarVinculoPendingIntent = PendingIntent.getBroadcast(context,0,
                confirmarVinculoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rejeitarVinculoPendingIntent = PendingIntent.getBroadcast(context, 0,
                rejeitarVinculoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(Constantes.ACAO_ACEITAR_VINCULO)
                .setLabel(context.getResources().getString(R.string.digite_senha))
                .build();
        NotificationCompat.Action confirmarVinculoAction = new NotificationCompat.Action.Builder(
                0, context.getString(R.string.aceitar), confirmarVinculoPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String nomeCanalNotificacao = context.getString(R.string.nome_canal_notificacao);
            String descricao = context.getString(R.string.descricao_canal_notificacao);
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel =
                    new NotificationChannel(Constantes.NOTIFICACAO_CHANNEL_ID, nomeCanalNotificacao,
                            importancia);
            notificationChannel.setDescription(descricao);

            //notificationChannel.enableLights(true);
            //notificationChannel.setLightColor(Color.RED);
            //notificationChannel.setVibrationPattern();
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Funcoes funcoes = new Funcoes();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, Constantes.NOTIFICACAO_CHANNEL_ID);
        //new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.drawable.ic_notificacao)
                .setColor(context.getResources().getColor(R.color.cor_primaria_escura))
                .setContentTitle(usuarioResponsavel.getLogin() + " (" + funcoes.converterLetrasNome(usuarioResponsavel.getNome()) + ")")
                .setContentText("Enviou uma solicitação de vínculo!")
                .setTicker("Chegou uma notificação")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(largeIconNotificacao)
                .setAutoCancel(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntentNotificacao)
                //.setFullScreenIntent(pendingIntentNotificacao, false)
                .addAction(confirmarVinculoAction)
                .addAction(0, context.getString(R.string.rejeitar), rejeitarVinculoPendingIntent)
                .setLights(Color.BLUE, 1000, 5000)
                .setSound(uriSom)
                .setVibrate(new long[]{100, 500, 200, 800})
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setNumber(flag);

        notificationManager.notify(flag, builder.build());
    }

    class PrepararNotificacaoTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;

        public PrepararNotificacaoTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            URL caminhoURL = null;
            try {
                caminhoURL = new URL(url);
                HttpURLConnection conexao = (HttpURLConnection) caminhoURL.openConnection();
                conexao.setDoInput(true);
                conexao.connect();
                InputStream inputStream = conexao.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Bitmap largeIconNotificacao = bitmap;

            exibirNotificacao(context, usuarioResponsavel, largeIconNotificacao);
        }
    }


}
