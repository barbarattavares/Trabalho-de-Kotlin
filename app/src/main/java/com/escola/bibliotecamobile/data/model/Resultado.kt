package com.escola.bibliotecamobile.data.model

/** Resultado de uma chamada à API: ou deu certo (com os dados) ou deu erro (com a mensagem). */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dados: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()
}
