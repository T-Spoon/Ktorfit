
Let`s say you have a function that requests a list of comments

```kotlin
@GET("posts/{postId}/comments")
suspend fun getCommentsByPostId(@Path("postId") postId: Int): List<Comment>
```

But now you want to directly wrap your comment list in your data holder class e.g. "MyOwnResponse"

```kotlin
sealed class MyOwnResponse<T> {
    data class Success<T>(val data: T) : Response<T>()
    class Error(val ex:Throwable) : Response<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(ex: Throwable) = Error(ex)
    }
}
```

To enable that, you have to implement a ResponseConverter. This class will be used to wrap the Ktor response
inside your wrapper class.

```kotlin
class MyOwnResponseConverterPlugin : ResponseConverter
    
   //The identifier of your plugin. Must be unique
    override fun getKey(): String {
        return "ResponsePlugin"
    }
    
    //This will convert T to MyOwnResponse<T>
    override fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any> {
        val newInfo = TypeInfo(pluginForClass(), info.reifiedType, info.kotlinType)
        val call = MyOwnResponse.success(body)
        return newInfo to call
    }
    
    override fun pluginForClass() = MyOwnResponse::class
    
    override fun onErrorReturn(exception: Exception): Any? {
        return MyOwnResponse.error(exception)
    }
}
```

Now you need to install that plugin inside the configuration of your http client

```kotlin
val client = HttpClient {
    installKtorfitPlugins(MyOwnResponseConverter())
}
```

Now add MyOwnResponse to your function
```kotlin
@GET("posts/{postId}/comments")
suspend fun getCommentsByPostId(@Path("postId") postId: Int): MyOwnResponse<List<Comment>>
```