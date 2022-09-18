package de.jensklingenberg.ktorfit.demo


import com.example.api.JsonPlaceHolderApi
import com.example.model.Post
import com.example.model.jsonPlaceHolderApi
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.KtorfitPlugin
import de.jensklingenberg.ktorfit.converter.builtin.CallPlugin
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallRequestConverter
import de.jensklingenberg.ktorfit.create
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json


val jvmClient = HttpClient {

    install(Logging) {
        level = LogLevel.ALL
    }

    install(ContentNegotiation) {
        json(Json { isLenient = true; ignoreUnknownKeys = true })
    }

    installKtorfitPlugins(ResponsePlugin(),CallPlugin())

    this.developmentMode = true
    expectSuccess = false


}

private fun HttpClientConfig<*>.installKtorfitPlugins(vararg responsePlugin: KtorfitPlugin) {
    responsePlugin.forEach {
        this.install(it)
    }
}


val jvmKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(jvmClient)
    requestConverter(
        FlowRequestConverter(),
        RxRequestConverter(),
        KtorfitCallRequestConverter()
    )
}


fun main() {

    val testApi = jvmKtorfit.create<JsonPlaceHolderApi>()


    println("==============================================")

        testApi.callPosts().onExecute(object :Callback<List<Post>>{
            override fun onResponse(call: List<Post>, response: HttpResponse) {
                call
            }

            override fun onError(exception: Throwable) {
                exception
            }

        })

    runBlocking {


      val tt =  jsonPlaceHolderApi.getCommentsByPostId(3)
        println(tt)


        delay(3000)
    }

}
