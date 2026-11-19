package com.escola.bibliotecamobile.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuração única do Retrofit (padrão "singleton" com object).
 * Todas as telas usam a mesma instância de [OpenLibraryApi].
 */
object RetrofitClient {

    private const val BASE_URL = "https://openlibrary.org/"

    /** Mostra no Logcat a URL chamada e o código HTTP de cada requisição. */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        // A Open Library pede que os apps se identifiquem pelo User-Agent.
        .addInterceptor { chain ->
            val requisicao = chain.request().newBuilder()
                .header("User-Agent", "BibliotecaMobile/1.0 (projeto academico Android)")
                .build()
            chain.proceed(requisicao)
        }
        .addInterceptor(logging)
        .build()

    val api: OpenLibraryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> data classes
            .build()
            .create(OpenLibraryApi::class.java)
    }
}
