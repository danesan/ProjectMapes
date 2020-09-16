package br.com.projectmapes.modelo;

public class NotificacaoSolicitacaoVinculo {

    private long dataHorario;
    private String uidAluno;
    private String uidResponsavel;
    private String idSolicitacaoVinculo;

    public NotificacaoSolicitacaoVinculo() {
    }

    public NotificacaoSolicitacaoVinculo(long dataHorario, String loginAluno,
                                         String loginResponsavel, String idSolicitacaoVinculo) {
        this.dataHorario = dataHorario;
        this.uidAluno = loginAluno;
        this.uidResponsavel = loginResponsavel;
        this.idSolicitacaoVinculo = idSolicitacaoVinculo;
    }

    public long getDataHorario() {
        return dataHorario;
    }

    public void setDataHorario(long dataHorario) {
        this.dataHorario = dataHorario;
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

    public String getIdSolicitacaoVinculo() {
        return idSolicitacaoVinculo;
    }

    public void setIdSolicitacaoVinculo(String idSolicitacaoVinculo) {
        this.idSolicitacaoVinculo = idSolicitacaoVinculo;
    }
}
