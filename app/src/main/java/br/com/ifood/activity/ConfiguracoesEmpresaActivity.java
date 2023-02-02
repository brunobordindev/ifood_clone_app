package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import br.com.ifood.R;
import br.com.ifood.databinding.ActivityConfiguracoesEmpresaBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.Permissao;
import br.com.ifood.helper.UsuarioFirebase;
import br.com.ifood.model.Empresa;
import br.com.ifood.model.Usuario;

public class ConfiguracoesEmpresaActivity extends AppCompatActivity {

    private ActivityConfiguracoesEmpresaBinding binding;
    private DatabaseReference usuarioRef;
    private StorageReference storageRef;
    private String identificadorUsuario;
    private Usuario usuarioLogado;
    private String[] permissoes = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int SELECAO_GALERIA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_configuracoes_empresa);

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configuração empresa");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.identificadorUsuario();

        //validar permissoes
        Permissao.validarPermissoes(permissoes, this, 1 );

        //Recuperar os dados do usuario
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        binding.editNomeEmpresa.setText(user.getDisplayName());

        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("empresas")
                .child(usuarioLogado.getId());
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               Empresa empresa = snapshot.getValue(Empresa.class);

                if (empresa == null){

                    binding.editCategoria.getText().toString();
                    binding.editTempoEntrega.getText().toString();
                    binding.editTaxaEntrega.getText().toString();
                }else{
                    binding.editCategoria.setText(empresa.getCategoria());
                    binding.editTempoEntrega.setText( empresa.getTempoEntrega());
                    binding.editTaxaEntrega.setText(empresa.getPrecoEntrega().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Recuperar os dados do usuario da foto
        Uri url  =  user.getPhotoUrl();
        if (url != null){
            Glide.with(ConfiguracoesEmpresaActivity.this)
                    .load(url)
                    .into(binding.imagePerfilEmpresa);
        }else{

            binding.imagePerfilEmpresa.setImageResource(R.drawable.perfil);
        }

        //botao salvar
        binding.btnSalvarConfE.setOnClickListener(view -> {

            String nome = binding.editNomeEmpresa.getText().toString();
            String categoria = binding.editCategoria.getText().toString();
            String tempoEntrega = binding.editTempoEntrega.getText().toString();
            String precoTaxa= binding.editTaxaEntrega.getText().toString();

            Empresa empresa = new Empresa();
            empresa.setIdUsuario(identificadorUsuario);
            empresa.setNome(nome);
            empresa.setFoto(usuarioLogado.getFoto());
            empresa.setCategoria(categoria);
            empresa.setTempoEntrega(tempoEntrega);
            empresa.setPrecoEntrega(Double.parseDouble(precoTaxa));
            empresa.salvar();

            //atualizar nome
            UsuarioFirebase.atualizarNomeUsuario(nome);
            usuarioLogado.setNome(nome);
            usuarioLogado.atualizarEmpresa();
            finish();
        });


        binding.imagePerfilEmpresa.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, SELECAO_GALERIA);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                //Seleciona apenas da galeria
                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //caso tenha sido escolhido uma imagem
                if (imagem != null){

                    //configura imagem para aparecer na tela
                    binding.imagePerfilEmpresa.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte[] dadosImagem = baos.toByteArray();


                    //salvar imagem no firebase
                    final StorageReference imageRef = storageRef
                            .child("imagens")
                            .child("empresas")
                            .child(identificadorUsuario + ".jpeg");
                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getApplicationContext(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizarFotoUsuario(url);
                                }

                            });

                            Toast.makeText(getApplicationContext(), "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void atualizarFotoUsuario(Uri url) {

        //atualizar foto no perfil
        UsuarioFirebase.atualizarFotoUsuario(url);

        //Atualizar foto no Firebase
        usuarioLogado.setFoto(url.toString());
        usuarioLogado.atualizar();

        Toast.makeText(getApplicationContext(), "Sua foto foi atualizada!", Toast.LENGTH_SHORT).show();
    }
}