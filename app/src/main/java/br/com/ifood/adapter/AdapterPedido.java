package br.com.ifood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.ifood.R;
import br.com.ifood.model.ItemPedido;
import br.com.ifood.model.Pedido;


public class AdapterPedido extends RecyclerView.Adapter<AdapterPedido.MyViewHolder> {

    private List<Pedido> pedidos;

    public AdapterPedido(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pedidos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.nome.setText( pedido.getNome() );
        holder.endereco.setText( "Endereço: " + pedido.getEndereco() + ", "  + pedido.getNumeroEndereco() + " - " + pedido.getComplemeto() + " - " + pedido.getBairroEndereco());
        holder.observacao.setText( "Obs: "+ pedido.getObservacao() );

        List<ItemPedido> itens = new ArrayList<>();
        itens = pedido.getItens();
        String descricaoItens = "";

        int numeroItem = 1;
        Double total = 0.0;
        for( ItemPedido itemPedido : itens ){//percorre a lista de itens

            int qtde = itemPedido.getQuantidade();
            Double preco = itemPedido.getPreco();
            total += (qtde * preco);

            String nome = itemPedido.getNomeProduto();
            descricaoItens += numeroItem + ") " + nome + " / (" + qtde + " x R$ " + preco + ") \n";
            numeroItem++;
        }
        descricaoItens += "Total: R$ " + total;
        holder.itens.setText(descricaoItens);

        int metodoPagamento = pedido.getMetodoPagamento();
        String pagamento = metodoPagamento == 0 ? "Dinheiro" : "Máquina cartão" ;
        holder.pgto.setText( "pgto: " + pagamento );

    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome;
        TextView endereco;
        TextView pgto;
        TextView observacao;
        TextView itens;


       public MyViewHolder(@NonNull View itemView) {
           super(itemView);

            nome = itemView.findViewById(R.id.text_pedido_nome);
            endereco = itemView.findViewById(R.id.text_pedido_endereco);
            pgto = itemView.findViewById(R.id.text_pedido_pagamento);
            observacao = itemView.findViewById(R.id.text_pedido_obs);
            itens = itemView.findViewById(R.id.text_pedido_itens);
       }
   }
}
