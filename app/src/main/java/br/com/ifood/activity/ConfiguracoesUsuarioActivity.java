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
import br.com.ifood.api.DataServiceAPI;
import br.com.ifood.databinding.ActivityConfiguracoesUsuarioBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.Permissao;
import br.com.ifood.helper.UsuarioFirebase;
import br.com.ifood.model.CEP;
import br.com.ifood.model.Usuario;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfiguracoesUsuarioActivity extends AppCompatActivity {

    private ActivityConfiguracoesUsuarioBinding binding;
    private DatabaseReference usuarioRef;
    private StorageReference storageRef;
    private String identificadorUsuario;
    private Usuario usuarioLogado;
    private String[] permissoes = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int SELECAO_GALERIA = 200;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_configuracoes_usuario);

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configuração usuário");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.identificadorUsuario();

        //API CEP
        retrofit = new Retrofit.Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        binding.btnBuscarConfig.setOnClickListener(view -> {
                String cep = binding.editCepConfig.getText().toString();
                recuperarCepRetrofit(cep);
        });

        //validar permissoes
        Permissao.validarPermissoes(permissoes, this, 1 );

        //Recuperar os dados do usuario
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        binding.editNomeConfig.setText(user.getDisplayName());

        //recuperarDasosFireabse
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("ceps")
                .child(usuarioLogado.getId());
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                CEP cep = snapshot.getValue(CEP.class);

                if (cep == null){

                    binding.editCepConfig.getText().toString();
                    binding.editRuaConfig.getText().toString();
                    binding.editNumeroConfig.getText().toString();
                    binding.editComplementoConfig.getText().toString();
                    binding.editCidadeConfig.getText().toString();
                    binding.editEstadoConfig.getText().toString();
                    binding.editBairroConfig.getText().toString();
                }else{
                    binding.editCepConfig.setText(cep.getCep());
                    binding.editRuaConfig.setText(cep.getLogradouro());
                    binding.editNumeroConfig.setText(cep.getNumero());
                    binding.editComplementoConfig.setText(cep.getComplemento());
                    binding.editCidadeConfig.setText(cep.getLocalidade());
                    binding.editEstadoConfig.setText(cep.getUf());
                    binding.editBairroConfig.setText(cep.getBairro());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Recuperar os dados do usuario da foto
        Uri url  =  user.getPhotoUrl();
        if (url != null){
            Glide.with(ConfiguracoesUsuarioActivity.this)
                    .load(url)
                    .into(binding.imagePerfilConfig);
        }else{

            binding.imagePerfilConfig.setImageResource(R.drawable.perfil);
        }

        binding.btnSalvarConfig.setOnClickListener(view -> {

            String nome = binding.editNomeConfig.getText().toString();
            String cep = binding.editCepConfig.getText().toString();
            String rua = binding.editRuaConfig.getText().toString();
            String numero = binding.editNumeroConfig.getText().toString();
            String complemento = binding.editComplementoConfig.getText().toString();
            String cidade = binding.editCidadeConfig.getText().toString();
            String estado = binding.editEstadoConfig.getText().toString();
            String bairro = binding.editBairroConfig.getText().toString();

            CEP cep1 = new CEP();
            cep1.setIdUsuario(identificadorUsuario);
            cep1.setCep(cep);
            cep1.setLogradouro(rua);
            cep1.setNumero(numero);
            cep1.setComplemento(complemento);
            cep1.setLocalidade(cidade);
            cep1.setUf(estado);
            cep1.setBairro(bairro);
            cep1.salvar();

            //atualizar dados
            UsuarioFirebase.atualizarNomeUsuario(nome);
            usuarioLogado.setNome(nome);
            usuarioLogado.setEndereco(cep1);
            usuarioLogado.atualizar();
            usuarioLogado.atualizarEnderecoUsuario();
            finish();

        });

        binding.imagePerfilConfig.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, SELECAO_GALERIA);
            }

        });
    }

    private void recuperarCepRetrofit(String cep) {

        DataServiceAPI serviceAPI = retrofit.create(DataServiceAPI.class);
        Call<CEP> call = serviceAPI.recuperarCEP(cep);

        call.enqueue(new Callback<CEP>() {
            @Override
            public void onResponse(Call<CEP> call, Response<CEP> response) {
                if (response.isSuccessful()){

                    CEP cep = response.body();
                    binding.editRuaConfig.setText(cep.getLogradouro());
                    binding.editCidadeConfig.setText(cep.getLocalidade());
                    binding.editEstadoConfig.setText(cep.getUf());
                    binding.editBairroConfig.setText(cep.getBairro());
                }
            }

            @Override
            public void onFailure(Call<CEP> call, Throwable t) {

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
                    binding.imagePerfilConfig.setImageBitmap(imagem);

                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 72, baos);
                    byte[] dadosImagem = baos.toByteArray();


                    //salvar imagem no firebase
                    final StorageReference imageRef = storageRef
                            .child("imagens")
                            .child("usuarios")
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