package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.ifood.R;
import br.com.ifood.adapter.AdapterEmpresa;
import br.com.ifood.adapter.AdapterProduto;
import br.com.ifood.databinding.ActivityHomeBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.model.Empresa;
import br.com.ifood.model.Produto;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;
    private List<Empresa> listaEmpresa = new ArrayList<>();
    private AdapterEmpresa adapterEmpresa;
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ifood");
        setSupportActionBar(toolbar);

        //congig recycler
        adapterEmpresa = new AdapterEmpresa(listaEmpresa);
        binding.recyclerEmpresas.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerEmpresas.setHasFixedSize(true);
        binding.recyclerEmpresas.setAdapter(adapterEmpresa);

        recuperarEmpresas();

        searchView.setHint("Pesquisar restaurantes");
        searchView.setTextColor(R.color.black);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisarEmpresas(newText);
                return true;
            }
        });

        binding.recyclerEmpresas.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                binding.recyclerEmpresas,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Empresa empresaSelecionada = listaEmpresa.get(position);
                        Intent i = new Intent(HomeActivity.this, CardapioActivity.class);
                        i.putExtra("empresa", empresaSelecionada);
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

    }

    private void pesquisarEmpresas(String newText) {
        DatabaseReference empresasRef = firebaseRef.child("empresas");
        Query query = empresasRef.orderByChild("nome")
                .startAt(newText)
                .endAt(newText + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaEmpresa.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    listaEmpresa.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarEmpresas() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference empresaRef = firebaseRef
                .child("empresas");
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaEmpresa.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    listaEmpresa.add(ds.getValue(Empresa.class));
                }

                Collections.reverse(listaEmpresa);
                adapterEmpresa.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        searchView = findViewById(R.id.material_search_view);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario, menu);

        //config botao pesquisa
        MenuItem item = menu.findItem(R.id.menu_pesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_configuracoes:
                abrirConfiguracoes();
                break;
            case R.id.menu_sair:
                deslogarUsuario();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        startActivity(new Intent(HomeActivity.this, ConfiguracoesUsuarioActivity.class));
    }
}