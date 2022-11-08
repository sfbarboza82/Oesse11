package br.net.oesse11.model;

public class Cliente {

    private String nome;
    private String produto;
    private String defeito;

    private String latitude;
    private String longitude;

    public Cliente() {
    }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getProduto() { return produto; }

    public void setProduto(String produto) { this.produto = produto; }

    public String getDefeito() { return defeito; }

    public void setDefeito(String defeito) { this.defeito = defeito; }

    public String getLatitude() { return latitude; }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }


}
