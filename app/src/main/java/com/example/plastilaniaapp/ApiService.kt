package com.example.plastilaniaapp

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class MovimentacaoRequest(
    val empCodigo: String,
    val mrcCodigo: String,
    val dataOcorrencia: String,
    val numeroDocumento: Int,
    val quantidade: Int,
    val servidorB: Boolean,
    val toaCodigo: Int,
    val gpxCodigo: Int,
    val prxCodigo: String,
    val locCodigo: Int,
    val endereco: String,
    val cliFor: Int
)

interface ApiService {
    @POST("api/estoque/movimentacoes/entrada")
    suspend fun registrarEntrada(@Body request: MovimentacaoRequest): Response<Unit>

    companion object {
        fun create(baseUrl: String): ApiService {
            val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(formattedUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun create(context: Context): ApiService {
            return create(ApiConfigManager.getBaseUrl(context))
        }

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl("http://192.168.1.48:5555/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
