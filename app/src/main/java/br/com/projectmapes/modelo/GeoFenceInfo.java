package br.com.projectmapes.modelo;

import com.google.android.gms.location.Geofence;

public class GeoFenceInfo {

    final String id;
    final double latitude;
    final double longitude;
    final float raio;
    long duracaoExpiracao;
    int tipoTransicao;

    public GeoFenceInfo(String id, double latitude, double longitude,
                        float raio, long duracaoExpiracao, int tipoTransicao) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.duracaoExpiracao = duracaoExpiracao;
        this.tipoTransicao = tipoTransicao;
    }

    public Geofence getGeofence(){
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(tipoTransicao)
                .setCircularRegion(latitude, longitude, raio)
                .setExpirationDuration(duracaoExpiracao)
                .build();
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRaio() {
        return raio;
    }

    public long getDuracaoExpiracao() {
        return duracaoExpiracao;
    }

    public int getTipoTransicao() {
        return tipoTransicao;
    }
}
