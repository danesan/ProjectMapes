package br.com.projectmapes.suporte;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TracaRotaHttp {

    public static List<LatLng> carregarRota(LatLng origem, LatLng destino) {

        List<LatLng> posicoes = new ArrayList<LatLng>();
        Funcoes funcoes = new Funcoes();

        try {
            String urlRota = String.format(Locale.US, Constantes.URL_GOOGLE_API_ROTAS_JSON,
                    origem.latitude, origem.longitude,
                    destino.latitude, destino.longitude);
            Log.d("URLROTA", urlRota);

            URL url = new URL(urlRota);
            String result = funcoes.converterBytesParaString(url.openConnection().getInputStream());
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonRota = jsonObject.getJSONArray("routes").getJSONObject(0);
            JSONObject leg = jsonRota.getJSONArray("legs").getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");
            final int numSteps = steps.length();
            JSONObject step;

            for(int i=0; i < numSteps; i++){
                step = steps.getJSONObject(i);
                String pontos = step.getJSONObject("polyline").getString("points");
                posicoes.addAll(PolyUtil.decode(pontos));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return posicoes;
    }

    private class RotaAsyncTask extends AsyncTaskLoader<List<LatLng>>{

        List<LatLng> rota;
        LatLng origem;
        LatLng destino;

        public RotaAsyncTask(@NonNull Context context, LatLng origem, LatLng destino) {
            super(context);
            this.origem = origem;
            this.destino = destino;
        }

        @Override
        protected void onStartLoading() {
            if(rota == null){
                forceLoad();
            } else {
                deliverResult(rota);
            }
        }

        @Nullable
        @Override
        public List<LatLng> loadInBackground() {
            rota = TracaRotaHttp.carregarRota(origem, destino);
            return rota;
        }
    }

}
