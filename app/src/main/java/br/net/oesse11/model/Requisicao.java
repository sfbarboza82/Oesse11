package br.net.oesse11.model;

import com.google.firebase.database.DatabaseReference;
import br.net.oesse11.config.ConfiguracaoFirebase;

import java.util.HashMap;
import java.util.Map;

public class Requisicao {

    private String id;

    private String nome;
    private String produto;
    private String defeito;
    private String status;
    private Usuario atendente;
    private Usuario tecnico;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_FINALIZADA = "finalizada";
    public static final String STATUS_ENCERRADA = "encerrada";
    public static final String STATUS_CANCELADA = "cancelada";

    public Requisicao() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId( idRequisicao );

        requisicoes.child( getId() ).setValue(this);

    }

    public void atualizar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();

        objeto.put("nome", getNome() );
        objeto.put("produto", getProduto() );
        objeto.put("defeito", getDefeito() );
        objeto.put("tecnico", getTecnico() );
        objeto.put("status", getStatus());

        requisicao.updateChildren( objeto );

    }

    public void atualizarNome(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("nome", getNome());

        requisicao.updateChildren( objeto );

    }

    public void atualizarStatus(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren( objeto );

    }

    public void atualizarLocalizacaoTecnico(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef
                .child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId())
                .child("tecnico");

        Map objeto = new HashMap();
        objeto.put("latitude", getTecnico().getLatitude() );
        objeto.put("longitude", getTecnico().getLongitude());

        requisicao.updateChildren( objeto );

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public String getDefeito() {
        return defeito;
    }

    public void setDefeito(String defeito) {
        this.defeito = defeito;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

}
