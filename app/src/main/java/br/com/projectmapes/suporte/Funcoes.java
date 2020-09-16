package br.com.projectmapes.suporte;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.projectmapes.modelo.NotificacaoMovimentacaoGeofence;

public class Funcoes {

    public Funcoes(){
    }

    public boolean emailEValido(final String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        return false;
    }

    public void setEditTextOnChangeFocus(TextInputLayout textInputLayout, String textoErro) {
        if (textInputLayout.getEditText().getText().toString().isEmpty()) {
            textInputLayout.setError(textoErro);
        } else {
            textInputLayout.setError(null);
        }
    }

    public void setValidacaoEmailOnChangeFocus(TextInputLayout textInputLayout, String textoErro) {
        boolean emailValido = emailEValido(textInputLayout.getEditText().getText().toString());
        if (emailValido != true) {
            textInputLayout.setError(textoErro);
        } else {
            textInputLayout.setError(null);
        }
    }

    public void setValidacaoSenhaOnChangeFocus(TextInputLayout senhaInputLayout, TextInputLayout confirmaSenhaInputLayout, String textoErro){
        if(!senhaInputLayout.getEditText().getText().toString().equals(confirmaSenhaInputLayout.getEditText().getText().toString())){
            confirmaSenhaInputLayout.setError(textoErro);
        } else {
            confirmaSenhaInputLayout.setError(null);
        }
    }

    public long converterHorarioStringEmMilisegundos(String horarioString){
        try {
            int horas = Integer.parseInt(horarioString.split(":")[0]);
            int minutos = Integer.parseInt(horarioString.split(":")[1]);
            return ((horas * 60) + minutos) * 60 * 1000;
        } catch (NumberFormatException e){
            Calendar horarioAtual = Calendar.getInstance();
            Log.d("EXCEÇÃO", horarioAtual.toString());
            return horarioAtual.getTimeInMillis() - (4 * 60 * 60 * 1000);
        }
    }

    public String converterDataMilisegundosEmString(Long data){
        DateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        return formatoData.format(new Time(data));
    }

    public long converterDataStringEmMilisegundos(String data){
        int dia = Integer.parseInt(data.split("/")[0]);
        int mes = Integer.parseInt(data.split("/")[1]);
        int ano = Integer.parseInt(data.split("/")[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(ano, mes-1, dia);

        Log.d("DATA", ano + " " + mes + " " + dia);
        return calendar.getTimeInMillis();
    }

    public long converterDataHorarioStringEmMilisegundos(String data, String horario){
        int dia = Integer.parseInt(data.split("/")[0]);
        int mes = Integer.parseInt(data.split("/")[1]) - 1;
        int ano = Integer.parseInt(data.split("/")[2]);

        int horas = Integer.parseInt(horario.split(":")[0]);
        int minutos = Integer.parseInt(horario.split(":")[1]);

        Calendar dataHorario = Calendar.getInstance();
        dataHorario.set(ano, mes, dia, horas, minutos);

        return dataHorario.getTimeInMillis();
    }

    public String converterHorarioMilisegundosEmString(Long horario){
        DateFormat formatoHorario = new SimpleDateFormat("HH:mm:ss");
        //formatoHorario.setTimeZone(new SimpleTimeZone(Calendar.ZONE_OFFSET, "GMT+05:00"));
        return formatoHorario.format(new Timestamp(horario));
    }

    public boolean verificarHorariosIguais(Calendar horario1, Calendar horario2){
        int horas1 = horario1.get(Calendar.HOUR_OF_DAY);
        int minutos1 = horario1.get(Calendar.MINUTE);
        int segundos1 = horario1.get(Calendar.SECOND);
        int horas2 = horario2.get(Calendar.HOUR_OF_DAY);
        int minutos2 = horario2.get(Calendar.MINUTE);
        int segundos2 = horario2.get(Calendar.SECOND);

        return (horas1 == horas2 && minutos1 == minutos2 && segundos1 == segundos2);
    }



    public int compararHorarios(Calendar horario1, Calendar horario2){
        int horas1 = horario1.get(Calendar.HOUR_OF_DAY);
        int minutos1 = horario1.get(Calendar.MINUTE);
        int horas2 = horario2.get(Calendar.HOUR_OF_DAY);
        int minutos2 = horario2.get(Calendar.MINUTE);

        if(horas1 > horas2){
            return -1;
        } else if(horas1 < horas2){
            return 1;
        } else if (minutos1 > minutos2){
            return -1;
        } else if (minutos1 < minutos2){
            return 1;
        }
        return 0;
    }

    public String getLoginDoEmail(String email){
        String login = email.split("@")[0];
        return login;
    }

    public List<NotificacaoMovimentacaoGeofence> inverterLista(List<NotificacaoMovimentacaoGeofence> lista) {
        List<NotificacaoMovimentacaoGeofence> listaInvertida = new ArrayList<>();
        for (int i = lista.size() - 1; i >= 0; i--) {
            listaInvertida.add(lista.get(i));
        }
        return listaInvertida;
    }

    public String converterLetrasNome(String nome){
        String[] nomeSobrenomes = nome.split(" ");
        for (int i = 0; i < nomeSobrenomes.length; i++){
            String primeiraLetraMaiuscula = nomeSobrenomes[i].substring(0, 1).toUpperCase();
            String restanteNomeSobrenome = nomeSobrenomes[i].substring(1);
            String nomeSobrenomeConvertido = primeiraLetraMaiuscula + restanteNomeSobrenome;
            nome = nome.replace(nomeSobrenomes[i], nomeSobrenomeConvertido);
        }
        return nome;
    }

    public String criptografarSenha(String senha) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            byte senhaCriptografa[] = algorithm.digest(senha.getBytes("UTF-8"));

            return new String(senhaCriptografa);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getBitmapFromVector(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap;
        if (width < height) {    //make a square
            bitmap = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0,
                drawable.getIntrinsicWidth(),    //use dimensions of Drawable
                drawable.getIntrinsicHeight()
        );
        drawable.draw(canvas);
        return bitmap;
    }

    public String converterBytesParaString(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesLidos;

        while((bytesLidos = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, bytesLidos);
        }
        return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
    }
}
