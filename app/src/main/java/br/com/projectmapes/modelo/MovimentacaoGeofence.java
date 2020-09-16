package br.com.projectmapes.modelo;

public class MovimentacaoGeofence {

    private long dataHora;
    private String acao;
    private String uidAluno;

    public MovimentacaoGeofence() {
    }

    public MovimentacaoGeofence(long dataHora, String acao, String uidAluno) {
        this.dataHora = dataHora;
        this.acao = acao;
        this.uidAluno = uidAluno;
    }

    public long getDataHora() {
        return dataHora;
    }

    public void setDataHora(long dataHora) {
        this.dataHora = dataHora;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getUidAluno() {
        return uidAluno;
    }

    public void setUidAluno(String uidAluno) {
        this.uidAluno = uidAluno;
    }
}
