package com.example.model

import com.example.api.JsonPlaceHolderApi
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallRequestConverter
import de.jensklingenberg.ktorfit.create
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.*


val httpClient2 = HttpClient() {

}

val commonKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(httpClient2)
    requestConverter(
        KtorfitCallRequestConverter(),
        FlowRequestConverter()
    )
}


val jsonPlaceHolderApi = commonKtorfit.create<JsonPlaceHolderApi>()

