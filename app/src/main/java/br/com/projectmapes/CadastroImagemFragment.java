package br.com.projectmapes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.projectmapes.suporte.UtilCamera;

public class CadastroImagemFragment extends Fragment implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private Button cadastrarGoogleButton;
    private Button cadastrarFacebookButton2;

    private File imagemPerfilUsuarioFile;
    private Uri caminhoImagemUri;
    private ImageView imagemCadastroImageView;
    private Bitmap imagemCarregada;
    private Button imagemCadastroButton;
    private CarregarImagemTask carregarImageTask;
    private int larguraImagem;
    private int alturaImagem;

    private static final int CARREGAR_IMAGEM = 12;
    private static final int CARREGAR_IMAGEM_GALERIA = 1;
    private static final int CARREGAR_IMAGEM_CAMERA = 2;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        String localFoto = UtilCamera.getUltimaMidia(getActivity());
        if(localFoto != null){
            imagemPerfilUsuarioFile = new File(localFoto);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_cadastro_imagem, container, false);

        cadastrarGoogleButton = (Button) layout.findViewById(R.id.cadastrarComGoogleButton);
        cadastrarFacebookButton2 = (Button) layout.findViewById(R.id.cadastrarComFacebookButton);
        imagemCadastroImageView = (ImageView) layout.findViewById(R.id.imagemCadastroImageView);
        imagemCadastroButton = (Button) layout.findViewById(R.id.imagemCadastroButton);

        imagemCadastroImageView.setOnClickListener(this);
        imagemCadastroButton.setOnClickListener(this);

        layout.getViewTreeObserver().addOnGlobalLayoutListener(this);

        return layout;
    }

    @Override
    public void onGlobalLayout(){
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        larguraImagem = imagemCadastroImageView.getWidth();
        alturaImagem = imagemCadastroImageView.getHeight();
        //carregarImagem();
    }

    private void carregarImagem() {
        if (imagemPerfilUsuarioFile != null && imagemPerfilUsuarioFile.exists()) {
            if (carregarImageTask == null || carregarImageTask.getStatus() != AsyncTask.Status.RUNNING) {
                carregarImageTask = new CarregarImagemTask();
                carregarImageTask.execute();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imagemCadastroButton || view.getId() == R.id.imagemCadastroImageView){
            if(ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(carregarImagemIntent(getContext()), CARREGAR_IMAGEM);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[] {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 0);
            }
        }
    }

    public Intent carregarImagemIntent(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();
        Intent fotoGaleriaIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        fotoGaleriaIntent.putExtra("REQUEST_CODE", CARREGAR_IMAGEM_GALERIA);

        Intent fotoCameraIntent = abrirCamera();
        fotoCameraIntent.putExtra("return-data", true);
        fotoCameraIntent.putExtra("REQUEST_CODE", CARREGAR_IMAGEM_CAMERA);

        intentList = addIntentsToList(context, intentList, fotoGaleriaIntent);
        intentList = addIntentsToList(context, intentList, fotoCameraIntent);

        if (intentList.size() > 0) {
            Log.d("INTENT_LIST_SIZE", intentList.size() + "");
            chooserIntent = Intent.createChooser(new Intent(),
                    context.getString(R.string.chooser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Bundle extrasIntent = data.getExtras();

        Log.d("ONACTIVITY","fragment");
        //Log.d("REQUESTCODE", extrasIntent.toString() + "");
        Log.d("REQUESTCODE", requestCode + "");
//        Log.d("DATA", data.getData().toString()+"");

        if (data == null) {
            carregarImagem();
        } else {
            try {
                Uri imagemSelecionada = data.getData();
                caminhoImagemUri = imagemSelecionada;
                if (imagemSelecionada != null) {
                    imagemCadastroImageView.setImageURI(imagemSelecionada);
                } else {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagemCadastroImageView.setImageBitmap(imageBitmap);
                }
            } catch (NullPointerException ex) {
            }
        }
        Log.d("CAIMNHO_URI", caminhoImagemUri.toString());
    }

    private Intent abrirCamera() {
        imagemPerfilUsuarioFile = null;
        try {
            imagemPerfilUsuarioFile = UtilCamera.novaMidia(UtilCamera.MIDIA_FOTO, getContext());
            Log.d("CAMINHO_foto", imagemPerfilUsuarioFile.toString());
        } catch (IOException ex){
        }
        if (imagemPerfilUsuarioFile != null) {
            caminhoImagemUri = FileProvider.getUriForFile(getContext(),
                    "br.com.projectmapes.fileprovider",
                    imagemPerfilUsuarioFile);;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, caminhoImagemUri);
            Log.d("CAMINHO_URI", caminhoImagemUri.toString());
            return intent;
        }
        return null;
    }


    class CarregarImagemTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            return UtilCamera.carregarImagem(imagemPerfilUsuarioFile, larguraImagem, alturaImagem);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                Log.d("BITMAP", bitmap.toString());
                imagemCarregada = bitmap;
                imagemCadastroImageView.setImageBitmap(bitmap);
                UtilCamera.salvarUltimaMidia(getActivity(),
                        UtilCamera.MIDIA_FOTO, imagemPerfilUsuarioFile.getAbsolutePath());
            }
        }
    }


    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public byte[] getFotoPerfilDoImageView(){
        //Bitmap bitmap = ((BitmapDrawable) imagemCadastroImageView.getDrawable()).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(
                imagemCadastroImageView.getWidth(),
                imagemCadastroImageView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        imagemCadastroImageView.draw(canvas);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        return data;
    }

    public Uri getUriFotoPerfilDoImageView(){
        return caminhoImagemUri;
    }

    public Button getCadastrarGoogleButton(){
        return cadastrarGoogleButton;
    }

    public Button getCadastrarFacebookButton() {
        return cadastrarFacebookButton2;
    }

    public ImageView getImagemCadastroImageView() {
        return imagemCadastroImageView;
    }

    public Button getImagemCadastroButton() {
        return imagemCadastroButton;
    }
}
