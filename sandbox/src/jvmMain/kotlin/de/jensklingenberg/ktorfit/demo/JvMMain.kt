package de.jensklingenberg.ktorfit.demo


import com.example.api.JsonPlaceHolderApi
import com.example.model.Comment
import de.jensklingenberg.ktorfit.*
import de.jensklingenberg.ktorfit.converter.builtin.*
import de.jensklingenberg.ktorfit.internal.TestConverter
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*

import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking



val jvmClient = HttpClient {

    install(Logging) {
        level = LogLevel.NONE
    }
   // install(MyPlugin("no1"))

    install(CallPlugin())
    install(ContentNegotiation) {
        gson()
    }



    this.developmentMode = true
    expectSuccess = false


}



val jvmKtorfit = ktorfit {
    baseUrl(JsonPlaceHolderApi.baseUrl)
    httpClient(jvmClient)
    requestConverter(
        FlowRequestConverter(),
        RxRequestConverter(),
        KtorfitCallRequestConverter()
    )
    responseConverter(
       // MyResponseConverter(),
        TestConverter(),
        //PeopleResponseConverter(),
        KtorfitCallResponseConverter(),
        //CommentListResponseConverter()
    //CallPlugin()
    )
}



fun main() {

    val testApi = jvmKtorfit.create<JsonPlaceHolderApi>()


    println("==============================================")



    runBlocking {


     val res =   testApi.callCommentsByPostId(3)

       res.onExecute(object :Callback<List<Comment>>{
           override fun onResponse(call: List<Comment>, response: HttpResponse) {
               println(call)
           }

           override fun onError(exception: Throwable) {
                exception

           }

       })



        delay(3000)
    }

}
