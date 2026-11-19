package com.escola.bibliotecamobile.data

import com.escola.bibliotecamobile.data.model.LivroResumo
import com.escola.bibliotecamobile.data.model.ObraDetalhe
import com.escola.bibliotecamobile.data.model.Resultado
import com.escola.bibliotecamobile.data.remote.OpenLibraryApi
import com.escola.bibliotecamobile.data.remote.RetrofitClient
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Faz as chamadas à API e transforma qualquer falha em [Resultado.Erro],
 * para que as telas nunca recebam uma exceção (o app não fecha sem internet).
 */
class LivroRepository(private val api: OpenLibraryApi = RetrofitClient.api) {

    suspend fun buscarLivros(termo: String): Resultado<List<LivroResumo>> =
        when (val resultado = executar { api.buscarLivros(termo) }) {
            is Resultado.Sucesso -> Resultado.Sucesso(
                // Descartamos itens sem chave ou sem título (não teriam como abrir os detalhes).
                resultado.dados.livros.orEmpty()
                    .filter { !it.chave.isNullOrBlank() && !it.titulo.isNullOrBlank() }
            )
            is Resultado.Erro -> resultado
        }

    /** @param chaveObra valor de "key" da busca, ex.: "/works/OL45804W" */
    suspend fun detalharObra(chaveObra: String): Resultado<ObraDetalhe> {
        val idObra = chaveObra.substringAfterLast('/')
        return executar { api.detalharObra(idObra) }
    }

    private suspend fun <T> executar(chamada: suspend () -> Response<T>): Resultado<T> {
        return try {
            val resposta = chamada()
            val corpo = resposta.body()
            when {
                !resposta.isSuccessful -> Resultado.Erro(mensagemParaCodigoHttp(resposta.code()))
                corpo == null -> Resultado.Erro("O serviço respondeu sem dados. Tente novamente.")
                else -> Resultado.Sucesso(corpo)
            }
        } catch (e: CancellationException) {
            throw e // a tela foi fechada: a coroutine precisa ser cancelada normalmente
        } catch (e: UnknownHostException) {
            Resultado.Erro("Sem conexão com a internet. Verifique sua rede e tente novamente.")
        } catch (e: SocketTimeoutException) {
            Resultado.Erro("O servidor demorou demais para responder. Tente novamente.")
        } catch (e: IOException) {
            Resultado.Erro("Falha de comunicação com o servidor. Tente novamente.")
        } catch (e: JsonParseException) {
            Resultado.Erro("O serviço retornou uma resposta inválida.")
        } catch (e: Exception) {
            Resultado.Erro("Ocorreu um erro inesperado: ${e.localizedMessage ?: "desconhecido"}")
        }
    }

    private fun mensagemParaCodigoHttp(codigo: Int): String = when (codigo) {
        404 -> "Livro não encontrado no catálogo (erro 404)."
        429 -> "Muitas requisições em pouco tempo. Aguarde um instante e tente novamente."
        in 500..599 -> "O serviço da Open Library está indisponível no momento (erro $codigo)."
        else -> "Não foi possível obter os dados (erro $codigo)."
    }
}
