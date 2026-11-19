package com.escola.bibliotecamobile.ui.detalhes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.escola.bibliotecamobile.R
import com.escola.bibliotecamobile.data.LivroRepository
import com.escola.bibliotecamobile.data.model.LivroResumo
import com.escola.bibliotecamobile.data.model.ObraDetalhe
import com.escola.bibliotecamobile.data.model.Resultado
import com.escola.bibliotecamobile.data.model.montarUrlCapa
import com.escola.bibliotecamobile.databinding.ActivityDetalhesBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

/**
 * Tela 3 — Detalhes. Recebe os dados básicos do livro pela Intent (mostrados na hora)
 * e busca a descrição e os assuntos na API (GET /works/{id}.json).
 */
class DetalhesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesBinding
    private val repository = LivroRepository()

    private var chave: String = ""
    private var titulo: String = ""
    private var autores: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.titulo_detalhes)

        // ---- Dados recebidos da tela de resultados ----
        chave = intent.getStringExtra(EXTRA_CHAVE).orEmpty()
        titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        autores = intent.getStringExtra(EXTRA_AUTORES).orEmpty()
        val ano = intent.getIntExtra(EXTRA_ANO, SEM_VALOR)
        val idCapa = intent.getLongExtra(EXTRA_ID_CAPA, SEM_VALOR.toLong())

        if (chave.isBlank()) {
            Toast.makeText(this, R.string.erro_livro_invalido, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvTitulo.text = titulo
        binding.tvAutores.text = autores
        binding.tvAno.text = if (ano != SEM_VALOR) {
            getString(R.string.publicado_em, ano)
        } else {
            getString(R.string.ano_desconhecido)
        }
        binding.ivCapa.load(montarUrlCapa(idCapa, "L")) {
            crossfade(true)
            placeholder(R.drawable.ic_livro)
            error(R.drawable.ic_livro)
        }

        binding.erro.btnTentarNovamente.setOnClickListener { carregarDetalhes() }
        binding.btnCompartilhar.setOnClickListener { compartilhar() }
        binding.btnAbrirSite.setOnClickListener { abrirNoSite() }

        carregarDetalhes()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregarDetalhes() {
        mostrarCarregando()
        lifecycleScope.launch {
            when (val resultado = repository.detalharObra(chave)) {
                is Resultado.Sucesso -> mostrarDetalhes(resultado.dados)
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }

    // ---------- Estados da seção "Sobre a obra" ----------

    private fun mostrarCarregando() {
        binding.layoutCarregando.visibility = View.VISIBLE
        binding.layoutConteudo.visibility = View.GONE
        binding.erro.root.visibility = View.GONE
    }

    private fun mostrarDetalhes(obra: ObraDetalhe) {
        binding.layoutCarregando.visibility = View.GONE
        binding.erro.root.visibility = View.GONE
        binding.layoutConteudo.visibility = View.VISIBLE

        binding.tvDescricao.text = obra.descricaoTexto() ?: getString(R.string.sem_descricao)

        val assuntos = obra.assuntos.orEmpty().take(MAX_ASSUNTOS)
        binding.chipGroupAssuntos.removeAllViews()
        assuntos.forEach { assunto ->
            val chip = Chip(this).apply {
                text = assunto
                isClickable = false
            }
            binding.chipGroupAssuntos.addView(chip)
        }
        val temAssuntos = assuntos.isNotEmpty()
        binding.tvTituloAssuntos.visibility = if (temAssuntos) View.VISIBLE else View.GONE
        binding.chipGroupAssuntos.visibility = if (temAssuntos) View.VISIBLE else View.GONE
    }

    private fun mostrarErro(mensagem: String) {
        binding.layoutCarregando.visibility = View.GONE
        binding.layoutConteudo.visibility = View.GONE
        binding.erro.root.visibility = View.VISIBLE
        binding.erro.tvMensagemErro.text = mensagem
    }

    // ---------- Ações ----------

    private fun urlDaObra(): String = "https://openlibrary.org$chave"

    private fun compartilhar() {
        val texto = getString(R.string.texto_compartilhar, titulo, autores, urlDaObra())
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.compartilhar)))
    }

    private fun abrirNoSite() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlDaObra())))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.erro_sem_navegador, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXTRA_CHAVE = "extra_chave"
        private const val EXTRA_TITULO = "extra_titulo"
        private const val EXTRA_AUTORES = "extra_autores"
        private const val EXTRA_ANO = "extra_ano"
        private const val EXTRA_ID_CAPA = "extra_id_capa"
        private const val SEM_VALOR = -1
        private const val MAX_ASSUNTOS = 8

        /** Monta a Intent com os dados que a tela de detalhes precisa. */
        fun criarIntent(context: Context, livro: LivroResumo): Intent =
            Intent(context, DetalhesActivity::class.java).apply {
                putExtra(EXTRA_CHAVE, livro.chave)
                putExtra(EXTRA_TITULO, livro.titulo)
                putExtra(EXTRA_AUTORES, livro.autoresFormatados())
                putExtra(EXTRA_ANO, livro.anoPrimeiraPublicacao ?: SEM_VALOR)
                putExtra(EXTRA_ID_CAPA, livro.idCapa ?: SEM_VALOR.toLong())
            }
    }
}
