package com.example.ecommerce_gamer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import helper.ProdutoAdapter;
import model.Produto;


public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText edtNome, edtDescricao, edtPreco;
    private RecyclerView recyclerProdutos;
    private List<Produto> listaProdutos = new ArrayList<>();
    private ProdutoAdapter adapter;
    private Produto produtoEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNome);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtPreco = findViewById(R.id.edtPreco);
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProdutoAdapter(listaProdutos);
        recyclerProdutos.setAdapter(adapter);
        findViewById(R.id.btnCadastrar).setOnClickListener(v -> salvarProduto());
        carregarProdutos();
    }

    private void carregarProdutos() {
        db.collection("produtos")
                .get()
                .addOnSuccessListener(query -> {
                    listaProdutos.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Produto p = doc.toObject(Produto.class);
                        p.setId(doc.getId());
                        listaProdutos.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
        adapter.setOnItemClickListener(produto -> {
            edtNome.setText(produto.getNome());
            edtDescricao.setText(produto.getDescricao());
            edtPreco.setText(String.valueOf(produto.getPreco()));
            produtoEditando = produto;
            ((Button) findViewById(R.id.btnCadastrar)).setText("Atualizar Produto");
        });
    }

    private void limparCampos() {
        edtNome.setText("");
        edtDescricao.setText("");
        edtPreco.setText("");
        produtoEditando = null;
        ((Button) findViewById(R.id.btnCadastrar)).setText("Salvar Produto");
    }
    private void salvarProduto() {
        String nome = edtNome.getText().toString();
        String descricao = edtDescricao.getText().toString();
        Double preco = Double.parseDouble(edtPreco.getText().toString());
        if (produtoEditando == null) {
            Produto produto = new Produto(null, nome, descricao, preco);
            db.collection("produtos")
                    .add(produto)
                    .addOnSuccessListener(doc -> {
                        produto.setId(doc.getId());
                        Toast.makeText(this, "Produto salvo!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarProdutos();
                    });
        } else {
            produtoEditando.setNome(nome);
            produtoEditando.setDescricao(descricao);
            produtoEditando.setPreco(preco);
            db.collection("produtos").document(produtoEditando.getId())
                    .set(produtoEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarProdutos();
                    }); } }

}