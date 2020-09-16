package br.com.projectmapes.modelo;

import java.io.Serializable;

public class NotificacaoMovimentacaoGeofence implements Serializable {

    private long dataHorario;
    private String movimentacao;
    private String uidAluno;
    private String uidResponsavel;

    public NotificacaoMovimentacaoGeofence() {
    }

    public NotificacaoMovimentacaoGeofence(long dataHorario, String movimentacao,
                                           String uidAluno, String uidResponsavel) {
        this.dataHorario = dataHorario;
        this.movimentacao = movimentacao;
        this.uidAluno = uidAluno;
        this.uidResponsavel = uidResponsavel;
    }

    public long getDataHorario() {
        return dataHorario;
    }

    public void setDataHorario(long dataHorario) {
        this.dataHorario = dataHorario;
    }

    public String getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(String movimentacao) {
        this.movimentacao = movimentacao;
    }

    public String getUidAluno() {
        return uidAluno;
    }

    public void setUidAluno(String uidAluno) {
        this.uidAluno = uidAluno;
    }

    public String getUidResponsavel() {
        return uidResponsavel;
    }

    public void setUidResponsavel(String uidResponsavel) {
        this.uidResponsavel = uidResponsavel;
    }
}
