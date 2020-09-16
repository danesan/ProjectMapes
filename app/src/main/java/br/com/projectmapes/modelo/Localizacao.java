package br.com.projectmapes.modelo;

import java.util.Calendar;

public class Localizacao {

    private long dataHora;
    private double latitude;
    private double longitude;

    public Localizacao() {
    }

    public Localizacao(long dataHora, double latitude, double longitude) {
        this.dataHora = dataHora;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getDataHora() {
        return dataHora;
    }

    public void setDataHora(long dataHora) {
        this.dataHora = dataHora;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
