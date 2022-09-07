package de.jensklingenberg.ktorfit.demo

import com.example.api.StarWarsApi
import com.example.model.People
import de.jensklingenberg.ktorfit.http.*
import io.ktor.client.statement.*

interface JvmPlaceHolderApi : StarWarsApi {

    @GET("people/{id}/")
    suspend fun getPersonById2(@Path("id") peopleId: Int): People

    @GET("people/{id}/")
    suspend fun testQuery(@Path("id") peopleId: Int, @Query("huhu") name: Array<String?>): People

    @Streaming
    @GET("people/1/")
    suspend fun getPostsStreaming(): HttpStatement

    @GET("people/{id}/")
    suspend fun getPersonById2AsResponse(@Path("id") peopleId: Int): Response<People>

    @GET()
    suspend fun getResponse(@Url peopleId: String): Response<People>
}