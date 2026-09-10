package com.example.plastilaniaapp

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
        private const val BASE_URL = "http://192.168.1.48:5555/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
