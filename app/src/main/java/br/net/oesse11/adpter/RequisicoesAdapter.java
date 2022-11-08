package br.net.oesse11.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import br.net.oesse11.activity.R;
import br.net.oesse11.helper.Local;
import br.net.oesse11.model.Destino;
import br.net.oesse11.model.Requisicao;
import br.net.oesse11.model.Usuario;

import java.util.List;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario tecnico;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario tecnico) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.tecnico = tecnico;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent, false);
        return new MyViewHolder( item ) ;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Requisicao requisicao = requisicoes.get( position );
        Destino destino = requisicao.getDestino();

        holder.nome.setText("CLIENTE: " + requisicao.getNome());
        holder.produto.setText("PRODUTO: " + requisicao.getProduto());
        holder.defeito.setText("DEFEITO: " + requisicao.getDefeito());
        holder.status.setText("STATUS: " + requisicao.getStatus());

        if(tecnico!= null){

            LatLng localDestino = new LatLng(
                    Double.parseDouble(destino.getLatitude()),
                    Double.parseDouble(destino.getLongitude())
            );

            LatLng localTecnico = new LatLng(
                    Double.parseDouble(tecnico.getLatitude()),
                    Double.parseDouble(tecnico.getLongitude())
            );
            float distancia = Local.calcularDistancia(localTecnico, localDestino);
            String distanciaFormatada = Local.formatarDistancia(distancia);
            holder.distancia.setText(distanciaFormatada + "- aproximadamente");

        }

    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, produto, defeito, status, distancia;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            produto = itemView.findViewById(R.id.textRequisicaoProduto);
            defeito = itemView.findViewById(R.id.textRequisicaoDefeito);
            status = itemView.findViewById(R.id.textRequisicaaoStatus);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);

        }
    }

}
