package de.jensklingenberg.ktorfit.demo


import com.example.api.JsonPlaceHolderApi
import com.example.api.StarWarsApi
import com.example.model.People
import de.jensklingenberg.ktorfit.*
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallRequestConverter
import de.jensklingenberg.ktorfit.internal.TestConverterFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

class KtorfitCallConverterFactory: KConverter.Factory{

    override fun responseBodyConverter(type: String, ktorfit: Ktorfit): KConverter<HttpResponse, *>? {
        return KtorfitCallKConverter()
    }
}

class KtorfitCallKConverter : KConverter<HttpResponse, Call<*>> {
    override suspend fun convert(httpResponse: HttpResponse): Call<*> {
        val str =  httpResponse.body<String>()
return object : Call<Any>{
    override fun onExecute(callBack: Callback<Any>) {
        try {

            var json = Json { ignoreUnknownKeys = true }
            val test =json.decodeFromString<People>(str)
            callBack.onResponse(str,httpResponse)
        }catch (ex: Exception){
            callBack.onError(ex)
        }
    }

}


    }

}


val jvmClient = HttpClient {

    install(Logging) {
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {


        json(Json { isLenient = true; ignoreUnknownKeys = true })
    }
    this.developmentMode = true
    expectSuccess = false


}

val jvmKtorfit = ktorfit {
    baseUrl(StarWarsApi.baseUrl)
    httpClient(jvmClient)
    requestConverter(
        FlowRequestConverter(),
        RxRequestConverter(),
        KtorfitCallRequestConverter(),
        Converter()
    )
    repsonseConverter(
        TestConverterFactory(),
        PeopleTestConverterFactory()
    )
}

val testKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(jvmClient)
    requestConverter(
        FlowRequestConverter(),
        RxRequestConverter(),
        KtorfitCallRequestConverter(),
        Converter()
    )
   // kConverter(TestConverterFactory(),PeopleTestConverterFactory())
}

fun main() {

    val testApi = jvmKtorfit.create<JvmPlaceHolderApi>()


    println("==============================================")



    runBlocking {

        val tec =  testApi.getPersonById2AsResponse(3)

        when(tec){

            is Response.Success<People> -> {
                tec.data
            }


            else -> {
                println("Error")
            }
        }

        println(tec)



        delay(3000)
    }

}
