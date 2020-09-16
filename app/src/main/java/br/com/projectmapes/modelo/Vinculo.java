package br.com.projectmapes.modelo;

public class Vinculo {

    private String id;
    private String uidUsuarioResponsavel;
    private String uidUsuarioAluno;

    public Vinculo() {
    }

    public Vinculo(String loginUsuarioResponsavel, String loginUsuarioAluno) {
        this.uidUsuarioResponsavel = loginUsuarioResponsavel;
        this.uidUsuarioAluno = loginUsuarioAluno;
    }

    public String getUidUsuarioResponsavel() {
        return uidUsuarioResponsavel;
    }

    public void setUidUsuarioResponsavel(String uidUsuarioResponsavel) {
        this.uidUsuarioResponsavel = uidUsuarioResponsavel;
    }

    public String getUidUsuarioAluno() {
        return uidUsuarioAluno;
    }

    public void setUidUsuarioAluno(String getLoginUsuarioAluno) {
        this.uidUsuarioAluno = getLoginUsuarioAluno;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
