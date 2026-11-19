package com.escola.bibliotecamobile.ui.resultados

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.escola.bibliotecamobile.R
import com.escola.bibliotecamobile.data.LivroRepository
import com.escola.bibliotecamobile.data.model.LivroResumo
import com.escola.bibliotecamobile.data.model.Resultado
import com.escola.bibliotecamobile.databinding.ActivityResultadosBinding
import com.escola.bibliotecamobile.ui.detalhes.DetalhesActivity
import kotlinx.coroutines.launch

/**
 * Tela 2 — Resultados. Recebe o termo da tela de busca, consulta a API
 * (GET /search.json via Retrofit) e mostra os livros numa lista.
 * Trata os estados: carregando, sucesso (com lista ou vazio) e erro.
 */
class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private val repository = LivroRepository()
    private val adapter = LivroAdapter { livro -> abrirDetalhes(livro) }
    private var termo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Dado recebido da tela anterior.
        termo = intent.getStringExtra(EXTRA_TERMO).orEmpty()
        if (termo.isBlank()) {
            Toast.makeText(this, R.string.erro_termo_vazio, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        supportActionBar?.title = getString(R.string.titulo_resultados, termo)

        binding.rvLivros.layoutManager = LinearLayoutManager(this)
        binding.rvLivros.adapter = adapter
        binding.erro.btnTentarNovamente.setOnClickListener { carregarLivros() }

        carregarLivros()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregarLivros() {
        mostrarCarregando()
        // lifecycleScope: a requisição é cancelada se a tela for fechada.
        lifecycleScope.launch {
            when (val resultado = repository.buscarLivros(termo)) {
                is Resultado.Sucesso -> {
                    if (resultado.dados.isEmpty()) mostrarVazio() else mostrarLivros(resultado.dados)
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }

    // ---------- Estados da tela ----------

    private fun mostrarCarregando() {
        binding.layoutCarregando.visibility = View.VISIBLE
        binding.rvLivros.visibility = View.GONE
        binding.tvVazio.visibility = View.GONE
        binding.tvResumo.visibility = View.GONE
        binding.erro.root.visibility = View.GONE
    }

    private fun mostrarLivros(livros: List<LivroResumo>) {
        binding.layoutCarregando.visibility = View.GONE
        binding.erro.root.visibility = View.GONE
        binding.tvVazio.visibility = View.GONE
        binding.tvResumo.visibility = View.VISIBLE
        binding.tvResumo.text = resources.getQuantityString(
            R.plurals.resumo_resultados, livros.size, livros.size
        )
        binding.rvLivros.visibility = View.VISIBLE
        adapter.atualizar(livros)
    }

    private fun mostrarVazio() {
        binding.layoutCarregando.visibility = View.GONE
        binding.erro.root.visibility = View.GONE
        binding.rvLivros.visibility = View.GONE
        binding.tvResumo.visibility = View.GONE
        binding.tvVazio.visibility = View.VISIBLE
        binding.tvVazio.text = getString(R.string.nenhum_resultado, termo)
    }

    private fun mostrarErro(mensagem: String) {
        binding.layoutCarregando.visibility = View.GONE
        binding.rvLivros.visibility = View.GONE
        binding.tvVazio.visibility = View.GONE
        binding.tvResumo.visibility = View.GONE
        binding.erro.root.visibility = View.VISIBLE
        binding.erro.tvMensagemErro.text = mensagem
    }

    private fun abrirDetalhes(livro: LivroResumo) {
        startActivity(DetalhesActivity.criarIntent(this, livro))
    }

    companion object {
        private const val EXTRA_TERMO = "extra_termo"

        fun criarIntent(context: Context, termo: String): Intent =
            Intent(context, ResultadosActivity::class.java).putExtra(EXTRA_TERMO, termo)
    }
}
