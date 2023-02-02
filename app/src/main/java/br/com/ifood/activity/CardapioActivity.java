package br.com.ifood.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.ifood.R;
import br.com.ifood.adapter.AdapterProduto;
import br.com.ifood.databinding.ActivityCardapioBinding;
import br.com.ifood.helper.ConfiguracaoFirebase;
import br.com.ifood.helper.UsuarioFirebase;
import br.com.ifood.listener.RecyclerItemClickListener;
import br.com.ifood.model.Empresa;
import br.com.ifood.model.ItemPedido;
import br.com.ifood.model.Pedido;
import br.com.ifood.model.Produto;
import br.com.ifood.model.Usuario;
import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private ActivityCardapioBinding binding;
    private Empresa empresaSelecionada;
    private List<Produto> listaProdutos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private AdapterProduto adapterProduto;
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private AlertDialog dialog;
    private AlertDialog dialogUsuario;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int metodoPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cardapio);

        //Config Tollbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardápio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        idUsuarioLogado = UsuarioFirebase.identificadorUsuario();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");
            binding.textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            binding.textCategoriaCardapio.setText(empresaSelecionada.getCategoria());
            binding.textTempoCardapio.setText("Tempo entrega: " + empresaSelecionada.getTempoEntrega() + " min");

            idEmpresa = empresaSelecionada.getIdUsuario();

            String urlImagem = empresaSelecionada.getFoto();
            Picasso.get().load(urlImagem).into(binding.imageEmpresaCardapio);
        }

        adapterProduto = new AdapterProduto(listaProdutos, this);
        binding.recyclerCardapio.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCardapio.setHasFixedSize(true);
        binding.recyclerCardapio.setAdapter(adapterProduto);

        //evento de clique recycler
        binding.recyclerCardapio.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                binding.recyclerCardapio,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        confirmarQuantidade(position);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        recuperarProdutos();
        recuperarDadosUsuario();

    }

    private void confirmarQuantidade(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade");

        EditText editQuantidade = new EditText(this);
        editQuantidade.setText("1");
        builder.setView(editQuantidade);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String quantidade = editQuantidade.getText().toString();

                Produto produtoSelecionado = listaProdutos.get(position);
                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setIdProduto(produtoSelecionado.getIdProdutos());
                itemPedido.setNomeProduto(produtoSelecionado.getNome());
                itemPedido.setQuantidade(Integer.parseInt(quantidade));
                itemPedido.setPreco(Double.parseDouble(produtoSelecionado.getPreco()));

                itensCarrinho.add(itemPedido);

                if (pedidoRecuperado == null){
                    pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresa);
                }

                pedidoRecuperado.setNome(usuario.getNome());
                pedidoRecuperado.setEndereco(usuario.getEndereco().getLogradouro());
                pedidoRecuperado.setNumeroEndereco(usuario.getEndereco().getNumero());
                pedidoRecuperado.setComplemeto(usuario.getEndereco().getComplemento());
                pedidoRecuperado.setBairroEndereco(usuario.getEndereco().getBairro());
                pedidoRecuperado.setItens(itensCarrinho);
                pedidoRecuperado.salvar();

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void recuperarDadosUsuario() {

        dialogUsuario = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialogUsuario.show();

        DatabaseReference usuariosRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null){
                    usuario = snapshot.getValue(Usuario.class);
                }
                recuperarPedido();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarPedido() {

        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuarioLogado);
        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                qtdItensCarrinho = 0;
                totalCarrinho = 0.00;

                if (snapshot.getValue() != null){

                    pedidoRecuperado = snapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();

                    //percorre pelos itens do carrinho
                    for (ItemPedido itemPedido: itensCarrinho){

                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();

                        qtdItensCarrinho += qtde;
                        totalCarrinho += (preco * qtde);
                    }

                }

                DecimalFormat df = new DecimalFormat("0.00");

                binding.textCarrinhoQtd.setText("Qtd " + qtdItensCarrinho);
                binding.textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));
                dialogUsuario.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void recuperarProdutos() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando anúncios")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference produtoRef = firebaseRef.child("produtos").child(idEmpresa);

        produtoRef.addValueEventListener(new ValueEventListener() {
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
        inflater.inflate(R.menu.menu_cardapio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_pedido:
                confirmarPedido();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmarPedido() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um método de pagamento");

        CharSequence[] itens = new CharSequence[]{
            "Dinheiro", "Máquina de cartão"
        };
        //0 fica no dinheiro e 1 é máquina de cartao, caso queira colocar -1 nao vem nenhum marcado
        builder.setSingleChoiceItems(itens, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                metodoPagamento = i;
            }
        });

        EditText editObservacao = new EditText(this);
        editObservacao.setHint("Digite uma observação");
        builder.setView(editObservacao);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String observacao = editObservacao.getText().toString();
                pedidoRecuperado.setMetodoPagamento(metodoPagamento);
                pedidoRecuperado.setObservacao(observacao);
                pedidoRecuperado.setStatus("confirmado");
                pedidoRecuperado.confirmar();
                pedidoRecuperado.remover();
                pedidoRecuperado = null;

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}










