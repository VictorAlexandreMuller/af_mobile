package helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import model.Produto;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolder> {
    private List<Produto> produtos;

    public interface OnItemClickListener {
        void onItemClick(Produto produto);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProdutoAdapter(List<Produto> produtos) {
        this.produtos = produtos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Produto p = produtos.get(pos);
        holder.nome.setText(p.getNome());
        holder.descricao.setText("Descricao: " + p.getDescricao());
        holder.preco.setText(String.format("R$" + p.getPreco()));


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private long lastClickTime = 0;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime < 300) {
                        deletarProduto(p.getId(), holder.getAdapterPosition(), v);
                    }
                    lastClickTime = currentTime;
                }
                return false;
            }
        });
    }

    private void deletarProduto(String idDocumento, int position, View view) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid(); // se quiser usar

            FirebaseFirestore.getInstance().collection("produtos")
                    .document(idDocumento)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        produtos.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(view.getContext(), "Produto deletado!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Erro ao deletar", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(view.getContext(), "Você precisa estar logado para realizar essa ação", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nome, descricao, preco;
        public ViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(android.R.id.nome);
            descricao = itemView.findViewById(android.R.id.descricao);
            preco = itemView.findViewById(android.R.preco);
        }
    }
}