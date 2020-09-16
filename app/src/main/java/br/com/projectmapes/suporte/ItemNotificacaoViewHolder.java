package br.com.projectmapes.suporte;

public class ItemNotificacaoViewHolder {

    private String fotoUsuario;
    private String login;
    private long dataHora;
    private String movimentacao;

    public ItemNotificacaoViewHolder(String fotoUsuario, String login, long data, String movimentacao) {
        this.fotoUsuario = fotoUsuario;
        this.login = login;
        this.dataHora = data;
        this.movimentacao = movimentacao;
    }

    public String getFotoUsuario() {
        return fotoUsuario;
    }

    public String getLogin() {
        return login;
    }

    public long getDataHora() {
        return dataHora;
    }

    public String getMovimentacao() {
        return movimentacao;
    }
}
