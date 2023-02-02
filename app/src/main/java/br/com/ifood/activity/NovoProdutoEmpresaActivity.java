package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.ifood.R;
import br.com.ifood.databinding.ActivityNovoProdutoEmpresaBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.Permissao;
import br.com.ifood.helper.UsuarioFirebase;
import br.com.ifood.model.Produto;
import dmax.dialog.SpotsDialog;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    private ActivityNovoProdutoEmpresaBinding binding;
    private String[] permissoes = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int SELECAO_GALERIA = 200;
    private Produto produto;

    private List<String> listasFotosRecuperadas = new ArrayList<>();
    private List<String> listasUrlFotos= new ArrayList<>();
    private android.app.AlertDialog dialog;
    private StorageReference storage;
    private String identificadorUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_novo_produto_empresa);

        storage = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.identificadorUsuario();

        //validar permissoes
        Permissao.validarPermissoes(permissoes, this, 1 );

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inicializarComponentes();

        binding.btnCadastrarProduto.setOnClickListener(view -> {

            produto = configuraProduto();
            String valor = String.valueOf(binding.editPrecoProduto);

            if (listasFotosRecuperadas.size() != 0 ){

                if (!produto.getNome().isEmpty()){
                    if (!produto.getDescricao().isEmpty()){
                        if (!valor.isEmpty() && !valor.equals("0")){

                            salvarProduto();

                        }else{
                            mensagemToast("Preencha o valor do produto");
                        }
                    }else{
                        mensagemToast("Preencha a descrição do produto");
                    }
                }else{
                    mensagemToast("Preencha o nome do produto");
                }
            }
        });

    }

    private Produto configuraProduto(){

        String nomeProduto = binding.editNomeProduto.getText().toString();
        String descricaoProduto = binding.editDescricaoProduto.getText().toString();
        String precoProduto = binding.editPrecoProduto.getText().toString();

        Produto produto = new Produto();
        produto.setNome(nomeProduto);
        produto.setDescricao(descricaoProduto);
        produto.setPreco(precoProduto);

        return produto;
    }

    public  void salvarProduto(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando produto")
                .setCancelable(false)
                .build();
        dialog.show();

        //salvar  imagem no storage
        for (int i = 0; i <  listasFotosRecuperadas.size() ; i++){
            String urlImagem = listasFotosRecuperadas.get(i);
            int tamanhoLista =  listasFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista);
        }
    }

    private void salvarFotoStorage(String urlString, int totalFotos){

        //Criar nó no storage
        final StorageReference imagemProduto  = storage
                .child("imagens")
                .child("produtos")
                .child(produto.getIdProdutos());

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemProduto.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemProduto.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        Uri url =  task.getResult();
                        String urlConvertida = url.toString();
                        listasUrlFotos.add(urlConvertida);

                        if (totalFotos == listasUrlFotos.size()){
                            produto.setFoto(listasUrlFotos);
                            produto.salvar();

                            dialog.dismiss();
                            finish();
                        }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mensagemToast("Falha ao fazer upload da imagem");
                Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
            }
        });
    }


    private void mensagemToast(String texto){
        Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){

            //recupera imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //configura imagem no imageView
            if(requestCode == 1){
                binding.imageNovoProduto.setImageURI(imagemSelecionada);
            }

            listasFotosRecuperadas.add(caminhoImagem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }



    private void inicializarComponentes(){


        binding.imageNovoProduto.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);
        });

    }
}