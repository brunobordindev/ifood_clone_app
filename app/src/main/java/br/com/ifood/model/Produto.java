package br.com.ifood.model;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.UsuarioFirebase;

public class Produto {

    private String idProdutos;
    private List<String> foto;
    private String nome;
    private String descricao;
    private String  preco;

    public Produto() {
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("produtos");
        setIdProdutos(anuncioRef.push().getKey());
    }

    public void salvar(){

        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        DatabaseReference referenceRef = ConfiguracaoFirebase.getFirebaseDatabase().child("produtos");
        referenceRef.child(idUsuario)
                .child(getIdProdutos())
                .setValue(this);

    }

    public void remover(){
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("produtos")
                .child(idUsuario)
                .child(getIdProdutos());

        anuncioRef.removeValue();
    }


    public String getIdProdutos() {
        return idProdutos;
    }

    public void setIdProdutos(String idProdutos) {
        this.idProdutos = idProdutos;
    }

    public List<String> getFoto() {
        return foto;
    }

    public void setFoto(List<String> foto) {
        this.foto = foto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }
}
