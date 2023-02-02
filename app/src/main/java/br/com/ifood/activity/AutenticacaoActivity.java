package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import br.com.ifood.R;
import br.com.ifood.databinding.ActivityAutenticacaoBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.UsuarioFirebase;
import br.com.ifood.model.Usuario;

public class AutenticacaoActivity extends AppCompatActivity {

    private ActivityAutenticacaoBinding binding;
    private FirebaseAuth autenticacao;
    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_autenticacao);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
//        autenticacao.signOut();


        binding.switchTipoAut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked){//cadastre-se

                    binding.editNomeAut.setVisibility(View.VISIBLE);
                    binding.linearTipoUsuario.setVisibility(View.VISIBLE);

                }else{//logar
                    binding.editNomeAut.setVisibility(View.GONE);
                    binding.linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        binding.btnAcessarAut.setOnClickListener(view -> {

            String nome = binding.editNomeAut.getText().toString();
            String email = binding.editEmailAut.getText().toString();
            String senha  = binding.editSenhaAut.getText().toString();

            if (binding.switchTipoAut.isChecked()){
                if (!nome.isEmpty()){
                    if (!email.isEmpty()){
                        if (!senha.isEmpty()){

                            Usuario usuario = new Usuario();
                            usuario.setNome(nome);
                            usuario.setEmail(email);
                            usuario.setSenha(senha);
                            usuario.setTipo(verificaTipoUsuario());

                            cadastrarUsuario(usuario);

                        }else{
                            Toast.makeText(getApplicationContext(), "Preencha a senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }else{
                logarUsuarioCad(email, senha);
                binding.editSenhaAut.setText("");
            }


        });
    }

    private void logarUsuarioCad(String email, String senha) {

        if (!email.isEmpty()){
            if (!senha.isEmpty()){

                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setSenha(senha);

                logarUsuario(usuario);

            }else{
                Toast.makeText(getApplicationContext(), "Preencha sua senha!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Preencha seu e-mail!", Toast.LENGTH_SHORT).show();
        }
    }

    private void logarUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    /*
                    Verificar o tipode usuario logado
                    Motorista ou Passageiro
                     */
                    UsuarioFirebase.redirecionaUsuarioLogado(AutenticacaoActivity.this);


                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não está cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não corresponde a um usuário cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao logar usuário" + e.getMessage();
                    }

                    Toast.makeText(getApplicationContext(), excecao, Toast.LENGTH_SHORT).show();
                }
            }


        });
    }

    private void cadastrarUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {

                        String id = task.getResult().getUser().getUid();
                        usuario.setId(id);
                        usuario.salvar();

                        //Atualizar o nome no UserProfile
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                      /*
                        Redirecionando o usuario de acordo com o seu tipo
                        Motorista - activity requisicoes
                        Passageito - activity Maps
                      */

                        if (verificaTipoUsuario() == "E"){
                            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
                            finish();
                            Toast.makeText(getApplicationContext(), "Sucesso ao cadastrar empresa!", Toast.LENGTH_SHORT).show();
                        }else{
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                            Toast.makeText(getApplicationContext(), "Sucesso ao cadastrar usuario!", Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e ){
                        e.printStackTrace();
                    }

                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um e-mail válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excecao = "E-mail já cadastrado!";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(AutenticacaoActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        UsuarioFirebase.redirecionaUsuarioLogado(this);
    }

    public String verificaTipoUsuario(){
        return binding.switchTipoUsuario.isChecked() ? "E" : "U";
    }

}