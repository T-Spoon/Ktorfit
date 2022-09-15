package com.example.model

import com.example.api.JsonPlaceHolderApi
import com.example.api.PostListResponseConverter
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallRequestConverter
import de.jensklingenberg.ktorfit.create
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


val httpClient2 = HttpClient() {
    install(ContentNegotiation) {
        // json(Json { isLenient = true; ignoreUnknownKeys = true })
    }
}

val commonKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(httpClient2)
    requestConverter(
        KtorfitCallRequestConverter(),
        FlowRequestConverter()
    )
    responseConverter(
        PostListResponseConverter()
    )
}


val jsonPlaceHolderApi = commonKtorfit.create<JsonPlaceHolderApi>()

