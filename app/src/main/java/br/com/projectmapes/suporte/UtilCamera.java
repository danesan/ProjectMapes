package br.com.projectmapes.suporte;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.com.projectmapes.CadastrarActivity;

public class UtilCamera {

    public static final int MIDIA_FOTO = 0;
    public static final int REQUESTCODE_FOTO = 1;
    private static final String KEY_ULTIMA_FOTO = "ultima_foto";
    private static final String PREFERENCIA_MIDIA = "midia_prefs";
    private static final String PASTA_MIDIA = "project_mapes";
    private static final String EXTENSAO = ".jpg";

    public static File novaMidia(int tipo, Context contexto) throws IOException { //gera um nome para a nova mídia utilizando a data do aparelho
        //String nomeMidia = new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(Calendar.getInstance().getTime()) + EXTENSAO;
        //File dirMidia = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), PASTA_MIDIA);
        //String storage = Environment.getExternalStorageDirectory().toString() + "/" + nomeMidia;
        //File dirMidia = new File(storage);
        String nomeMidia = new SimpleDateFormat("yyyy-MM-dd_HHmm").format(Calendar.getInstance().getTimeInMillis());
        //File dirMidia = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PASTA_MIDIA);
        File dirMidia = new File(contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PASTA_MIDIA);

        if(!dirMidia.exists()){
            dirMidia.mkdirs();
        }

        File imagem = File.createTempFile(
                    nomeMidia,
                    EXTENSAO,
                    dirMidia
            );
        return imagem;
    }

    public static void salvarUltimaMidia(Context contexto, int tipo, String midia){
        SharedPreferences sharedPreferences = contexto.getSharedPreferences(PREFERENCIA_MIDIA, contexto.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(KEY_ULTIMA_FOTO, midia)
                .commit();

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse(midia);
        mediaScanIntent.setData(contentUri);
        contexto.sendBroadcast(mediaScanIntent);
    }

    public static String getUltimaMidia(Context contexto){
        return contexto.getSharedPreferences(PREFERENCIA_MIDIA, Context.MODE_PRIVATE)
                .getString(KEY_ULTIMA_FOTO, null);
    }

    public static Bitmap carregarImagem(File imagem, int larguraImageView, int alturaImageView){
        if(larguraImageView == 0 || alturaImageView == 0) return null;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true; //apenas ler o tamanho da imagem sem carregá-la realmente em memória
        BitmapFactory.decodeFile(imagem.getAbsolutePath(), bmOptions);

        int larguraFoto = bmOptions.outWidth;
        int alturaFoto = bmOptions.outHeight;
        int escala = Math.min(
                larguraFoto / larguraImageView,
                alturaFoto / alturaImageView);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = escala;
        bmOptions.inPurgeable = true;
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(imagem.getAbsolutePath(), bmOptions);
        //Log.d("ABS_PATH", imagem.getAbsolutePath());
        bitmap = rotacionar(bitmap, imagem.getAbsolutePath()); //verifica a orientação do aparelho
        return bitmap;
    }

    private static Bitmap rotacionar(Bitmap bitmap, String path){
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotacionar(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotacionar(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotacionar(bitmap, 270);
                    break;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private static Bitmap rotacionar(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(
                source, 0, 0,
                source.getWidth(), source.getHeight(),
                matrix, true);
        return bitmap;
    }

}
