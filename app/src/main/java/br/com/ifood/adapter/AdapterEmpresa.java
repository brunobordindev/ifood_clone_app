package br.com.ifood.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.ifood.R;
import br.com.ifood.model.Empresa;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterEmpresa extends RecyclerView.Adapter<AdapterEmpresa.MyViewHolder> {

    private List<Empresa> listaEmpresas;

    public AdapterEmpresa(List<Empresa> listaEmpresas) {
        this.listaEmpresas = listaEmpresas;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_empresa, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Empresa empresa = listaEmpresas.get(position);
        holder.nomeEmpresa.setText(empresa.getNome());
        holder.categoria.setText(empresa.getCategoria());
        holder.tempo.setText(empresa.getTempoEntrega() + " Min");
        holder.entrega.setText("R$ " + empresa.getPrecoEntrega().toString());

        //Carregar imagem
        String urlImagem = empresa.getFoto();
        Picasso.get().load( urlImagem ).into( holder.imagemEmpresa );
    }

    @Override
    public int getItemCount() {
        return listaEmpresas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imagemEmpresa;
        TextView nomeEmpresa;
        TextView categoria;
        TextView tempo;
        TextView entrega;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeEmpresa = itemView.findViewById(R.id.text_nome_adapter_empresa);
            categoria = itemView.findViewById(R.id.text_categoria_adapter_empresa);
            tempo = itemView.findViewById(R.id.text_tempo_adapter_empresa);
            entrega = itemView.findViewById(R.id.text_taxa_adapter_empresa);
            imagemEmpresa = itemView.findViewById(R.id.image_adapter_empresa);
        }
    }
}
