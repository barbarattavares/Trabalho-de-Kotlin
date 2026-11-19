package com.escola.bibliotecamobile.ui.resultados

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.escola.bibliotecamobile.R
import com.escola.bibliotecamobile.data.model.LivroResumo
import com.escola.bibliotecamobile.databinding.ItemLivroBinding

/** Adapter da lista de resultados. [aoClicar] é chamado quando o usuário toca num livro. */
class LivroAdapter(
    private val aoClicar: (LivroResumo) -> Unit
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

    private val livros = mutableListOf<LivroResumo>()

    @SuppressLint("NotifyDataSetChanged") // a lista inteira é trocada a cada busca
    fun atualizar(novosLivros: List<LivroResumo>) {
        livros.clear()
        livros.addAll(novosLivros)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val binding = ItemLivroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LivroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        holder.exibir(livros[position])
    }

    override fun getItemCount(): Int = livros.size

    inner class LivroViewHolder(private val binding: ItemLivroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun exibir(livro: LivroResumo) {
            val contexto = binding.root.context
            binding.tvTitulo.text = livro.titulo
            binding.tvAutores.text = livro.autoresFormatados()

            val ano = livro.anoPrimeiraPublicacao?.toString()
                ?: contexto.getString(R.string.ano_desconhecido)
            val edicoes = livro.quantidadeEdicoes ?: 0
            binding.tvInfo.text = contexto.resources.getQuantityString(
                R.plurals.info_ano_edicoes, edicoes, ano, edicoes
            )

            binding.ivCapa.load(livro.urlCapa("M")) {
                crossfade(true)
                placeholder(R.drawable.ic_livro)
                error(R.drawable.ic_livro)
            }

            binding.root.setOnClickListener { aoClicar(livro) }
        }
    }
}
