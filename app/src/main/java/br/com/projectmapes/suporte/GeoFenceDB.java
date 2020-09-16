package br.com.projectmapes.suporte;

import android.content.Context;
import android.content.SharedPreferences;

import br.com.projectmapes.modelo.GeoFenceInfo;

public class GeoFenceDB {

    private final SharedPreferences sharedPreferences;

    public GeoFenceDB(Context context) {
        sharedPreferences = context.getSharedPreferences(Constantes.KEY_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
    }

    public GeoFenceInfo getGeofence(String id){
        double latitude = sharedPreferences.getFloat(
                getGeofenceFieldKey(id, Constantes.KEY_LATITUDE), Constantes.VALOR_INVALIDO_FLOAT);
        double longitude = sharedPreferences.getFloat(
                getGeofenceFieldKey(id, Constantes.KEY_LONGITUDE), Constantes.VALOR_INVALIDO_FLOAT);
        float raio = sharedPreferences.getFloat(
                getGeofenceFieldKey(id, Constantes.KEY_RAIO), Constantes.VALOR_INVALIDO_FLOAT);
        long duracaoExpiracao = sharedPreferences.getLong(
                getGeofenceFieldKey(id, Constantes.KEY_DURACAO_EXPIRACAO), Constantes.VALOR_INVALIDO_LONG);
        int tipoTransicao = sharedPreferences.getInt(
                getGeofenceFieldKey(id, Constantes.KEY_TIPO_TRANSICAO), Constantes.VALOR_INVALIDO_INT);

        if(latitude != Constantes.VALOR_INVALIDO_FLOAT
                && longitude != Constantes.VALOR_INVALIDO_FLOAT
                && raio != Constantes.VALOR_INVALIDO_FLOAT
                && duracaoExpiracao != Constantes.VALOR_INVALIDO_LONG
                && tipoTransicao != Constantes.VALOR_INVALIDO_INT){
            return new GeoFenceInfo(id, latitude, longitude, raio, duracaoExpiracao, tipoTransicao);
        } else {
            return null;
        }
    }

    public void salvarGeofence(String id, GeoFenceInfo geoFence){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(getGeofenceFieldKey(id, Constantes.KEY_LATITUDE),
                (float) geoFence.getLatitude());
        editor.putFloat(getGeofenceFieldKey(id, Constantes.KEY_LONGITUDE),
                (float) geoFence.getLongitude());
        editor.putFloat(getGeofenceFieldKey(id, Constantes.KEY_RAIO),
                geoFence.getRaio());
        editor.putLong(getGeofenceFieldKey(id, Constantes.KEY_DURACAO_EXPIRACAO),
                geoFence.getDuracaoExpiracao());
        editor.putInt(getGeofenceFieldKey(id, Constantes.KEY_TIPO_TRANSICAO),
                geoFence.getTipoTransicao());
        editor.commit();
    }

    public void removerGeofence(String id){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getGeofenceFieldKey(id, Constantes.KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, Constantes.KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, Constantes.KEY_RAIO));
        editor.remove(getGeofenceFieldKey(id, Constantes.KEY_DURACAO_EXPIRACAO));
        editor.remove(getGeofenceFieldKey(id, Constantes.KEY_TIPO_TRANSICAO));
        editor.commit();
    }

    private String getGeofenceFieldKey(String id, String key){
        return Constantes.KEY_PREFIX + "_" + id + "_" + key;
    }

}
