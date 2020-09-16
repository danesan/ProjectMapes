package br.com.projectmapes.modelo;

import java.util.Objects;

public class SolicitacaoVinculo {

    private String id;
    private String uidUsuarioResponsavel;
    private String uidUsuarioAluno;
    private String senha;

    public SolicitacaoVinculo() {
    }

    public SolicitacaoVinculo(String loginUsuarioResponsavel, String loginUsuarioAluno, String senha) {
        this.uidUsuarioResponsavel = loginUsuarioResponsavel;
        this.uidUsuarioAluno = loginUsuarioAluno;
        this.senha = senha;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SolicitacaoVinculo{" +
                "loginUsuarioResponsavel='" + uidUsuarioResponsavel + '\'' +
                ", loginUsuarioAluno='" + uidUsuarioAluno + '\'' +
                ", senha='" + senha + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolicitacaoVinculo that = (SolicitacaoVinculo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
