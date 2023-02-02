package br.com.ifood.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.UsuarioFirebase;

public class Empresa implements Serializable {

    private String idUsuario;
    private String foto;
    private String nome;
    private String categoria;
    private String tempoEntrega;
    private Double precoEntrega;

    public Empresa() {
    }

    public void salvar(){

        DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
        reference.child("empresas")
                .child(getIdUsuario())
                .setValue(this);
    }


    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTempoEntrega() {
        return tempoEntrega;
    }

    public void setTempoEntrega(String tempoEntrega) {
        this.tempoEntrega = tempoEntrega;
    }

    public Double getPrecoEntrega() {
        return precoEntrega;
    }

    public void setPrecoEntrega(Double precoEntrega) {
        this.precoEntrega = precoEntrega;
    }
}
