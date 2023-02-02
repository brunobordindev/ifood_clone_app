package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.ifood.R;
import br.com.ifood.R.color;
import br.com.ifood.adapter.AdapterProduto;
import br.com.ifood.databinding.ActivityEmpresaBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.model.Produto;
import dmax.dialog.SpotsDialog;

public class EmpresaActivity extends AppCompatActivity {

    private ActivityEmpresaBinding binding;
    private FirebaseAuth autenticacao;
    private List<Produto> listaProdutos = new ArrayList<>();
    private AdapterProduto adapterProduto;
    private DatabaseReference produtoUsuarioRef;
    private AlertDialog dialog;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_empresa);

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ifood - empresa");
        setSupportActionBar(toolbar);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        produtoUsuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("produtos")
                .child(ConfiguracaoFirebase.getIdUsuario());

        adapterProduto = new AdapterProduto(listaProdutos, this);
        binding.recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProdutos.setHasFixedSize(true);
        binding.recyclerProdutos.setAdapter(adapterProduto);

        recuperarProdutos();

        binding.recyclerProdutos.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                binding.recyclerProdutos,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(EmpresaActivity.this)
                                .setTitle("Apagar produto")
                                .setMessage("Você tem certeza que deseja apagar o produto?")
                                .setCancelable(false)
                                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Produto produtoSelecionado = listaProdutos.get(position);
                                        produtoSelecionado.remover();
                                        adapterProduto.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));
    }

    private void recuperarProdutos() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        produtoUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaProdutos.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    listaProdutos.add(ds.getValue(Produto.class));
                }

                Collections.reverse(listaProdutos);
                adapterProduto.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empresa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_novo_produto:
                abrirNovoProduto();
                break;
            case R.id.menu_pedidos:
                abrirPedidos();
                break;
            case R.id.menu_configuracoes:
                abrirConfiguracoes();
                break;
            case R.id.menu_sair:
                deslogarUsuario();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirPedidos() {
        startActivity(new Intent(EmpresaActivity.this, PedidosActivity.class));
    }

    private void deslogarUsuario() {

        try {
            autenticacao.signOut();
            finish();
        }catch (Exception e ){
            e.printStackTrace();
        }
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(EmpresaActivity.this, ConfiguracoesEmpresaActivity.class));
    }

    private void abrirNovoProduto() {
        startActivity(new Intent(EmpresaActivity.this, NovoProdutoEmpresaActivity.class));
    }
}