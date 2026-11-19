package com.escola.bibliotecamobile.data.remote

import com.escola.bibliotecamobile.data.model.BuscaResponse
import com.escola.bibliotecamobile.data.model.ObraDetalhe
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints da Open Library usados pelo app. O Retrofit gera a implementação.
 * Documentação: https://openlibrary.org/developers/api
 */
interface OpenLibraryApi {

    /**
     * Busca livros por título, autor ou assunto.
     * Exemplo: GET https://openlibrary.org/search.json?q=dom+casmurro&fields=...&limit=20
     */
    @GET("search.json")
    suspend fun buscarLivros(
        @Query("q") termo: String,
        @Query("fields") campos: String = CAMPOS_BUSCA,
        @Query("limit") limite: Int = 20
    ): Response<BuscaResponse>

    /**
     * Detalhes de uma obra (descrição e assuntos).
     * Exemplo: GET https://openlibrary.org/works/OL45804W.json
     */
    @GET("works/{idObra}.json")
    suspend fun detalharObra(@Path("idObra") idObra: String): Response<ObraDetalhe>

    companion object {
        /** Pedimos só os campos que o app realmente usa (resposta menor e mais rápida). */
        const val CAMPOS_BUSCA = "key,title,author_name,first_publish_year,cover_i,edition_count"
    }
}
