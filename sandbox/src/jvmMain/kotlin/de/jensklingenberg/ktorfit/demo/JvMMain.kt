package de.jensklingenberg.ktorfit.demo


import com.example.api.JsonPlaceHolderApi
import com.example.model.Comment
import com.example.model.jsonPlaceHolderApi
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.KtorfitCallResponseConverter
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.CallRequestConverter
import de.jensklingenberg.ktorfit.installKtorfitPlugins
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

    installKtorfitPlugins(ResponseResponseConverterPlugin())

    this.developmentMode = true
    expectSuccess = false


}



val jvmKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(jvmClient)
    requestConverter(
        FlowRequestConverter(),
        RxRequestConverter(),
        CallRequestConverter()
    )
    responseConverter(KtorfitCallResponseConverter())
}


fun main() {

    runBlocking {


      val tt =  jsonPlaceHolderApi.callCommentsByPostId(3)
       tt.onExecute(object :Callback<List<Comment>>{
           override fun onResponse(call: List<Comment>, response: HttpResponse) {
               call
           }

           override fun onError(exception: Throwable) {
               exception
           }

       })



        delay(3000)
    }

}
