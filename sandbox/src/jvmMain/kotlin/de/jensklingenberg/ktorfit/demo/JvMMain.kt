package de.jensklingenberg.ktorfit.demo


import com.example.api.StarWarsApi
import com.example.api.createJsonPlaceHolderApi
import com.example.model.People
import de.jensklingenberg.ktorfit.*
import de.jensklingenberg.ktorfit.converter.builtin.FlowResponseConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallResponseConverter
import de.jensklingenberg.ktorfit.internal.TestConverterFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PeopleTestConverterFactory: KConverter.Factory{

    override fun responseBodyConverter(type: String, ktorfit: Ktorfit): KConverter<HttpResponse, *>? {
        return PeopleKConverter()
    }
}

class PeopleKConverter : KConverter<HttpResponse, People> {
    override suspend fun convert(httpResponse: HttpResponse): People {

        val str =  httpResponse.body<String>()
        var json = Json { ignoreUnknownKeys = true }
        val test =json.decodeFromString<People>(str)
        return test
    }

}

val jvmClient = HttpClient {

    install(Logging) {
        level = LogLevel.ALL
    }

    this.developmentMode = true
    expectSuccess = false


}

val jvmKtorfit = ktorfit {
    baseUrl(StarWarsApi.baseUrl)
    httpClient(jvmClient)
    responseConverter(
        FlowResponseConverter(),
        RxResponseConverter(),
        KtorfitCallResponseConverter(),
        SuspendConverter()
    )
    kConverter(TestConverterFactory(),PeopleTestConverterFactory())
}


fun main() {
    val exampleApi = jvmKtorfit.create<JvmPlaceHolderApi>()
    val jsonPlaceHolderApi = jvmKtorfit.createJsonPlaceHolderApi()
    println("==============================================")


    runBlocking {
        jsonPlaceHolderApi.suscallPosts().onExecute(object : Callback<String> {
            override fun onResponse(call: String, response: HttpResponse) {
                call
            }

            override fun onError(exception: Throwable) {

            }

        })

       val peo = exampleApi.getPersonById2(3)
        println(peo)

        delay(3000)
    }

}
