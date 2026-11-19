package com.escola.bibliotecamobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa o JSON de GET /search.json:
 *
 * {
 *   "numFound": 1234,
 *   "docs": [ { "key": "/works/OL...W", "title": "...", "author_name": ["..."], ... } ]
 * }
 *
 * Os campos são anuláveis porque a API pode omitir qualquer um deles.
 */
data class BuscaResponse(
    @SerializedName("numFound") val totalEncontrado: Int?,
    @SerializedName("docs") val livros: List<LivroResumo>?
)

/** Um item de "docs": resumo de um livro na lista de resultados. */
data class LivroResumo(
    @SerializedName("key") val chave: String?,                       // "/works/OL45804W"
    @SerializedName("title") val titulo: String?,
    @SerializedName("author_name") val autores: List<String>?,
    @SerializedName("first_publish_year") val anoPrimeiraPublicacao: Int?,
    @SerializedName("cover_i") val idCapa: Long?,
    @SerializedName("edition_count") val quantidadeEdicoes: Int?
) {
    fun autoresFormatados(): String =
        autores?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Autor desconhecido"

    fun urlCapa(tamanho: String = "M"): String? = montarUrlCapa(idCapa, tamanho)
}

/** A Open Library serve as capas em outro domínio, a partir do id numérico da capa. */
fun montarUrlCapa(idCapa: Long?, tamanho: String = "M"): String? =
    idCapa?.takeIf { it > 0 }?.let { "https://covers.openlibrary.org/b/id/$it-$tamanho.jpg" }
