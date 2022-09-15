package de.jensklingenberg.ktorfit.demo


import com.example.api.CommentListResponseConverter
import com.example.api.JsonPlaceHolderApi
import com.example.api.PeopleResponseConverter
import com.example.model.Comment
import de.jensklingenberg.ktorfit.*
import de.jensklingenberg.ktorfit.converter.builtin.FlowRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallRequestConverter
import de.jensklingenberg.ktorfit.converter.builtin.KtorfitCallResponseConverter
import de.jensklingenberg.ktorfit.internal.TestConverter
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


val jvmClient = HttpClient {

    install(Logging) {
        level = LogLevel.ALL
    }

    install(ContentNegotiation) {
        // json(Json { isLenient = true; ignoreUnknownKeys = true })
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
        MyResponseConverter(),
        TestConverter(),
        PeopleResponseConverter(),
        KtorfitCallResponseConverter(),
        CommentListResponseConverter()
    )
}



fun main() {

    val testApi = jvmKtorfit.create<JsonPlaceHolderApi>()


    println("==============================================")



    runBlocking {

        testApi.callCommentsByPostId(3).onExecute(object : Callback<List<Comment>>{
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
