package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.Call
import de.jensklingenberg.ktorfit.Callback
import de.jensklingenberg.ktorfit.converter.builtin.Test
import de.jensklingenberg.ktorfit.upperBoundType
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass

abstract class MyMy : HttpClientPlugin<Test.Config, Test>, ResponseConverter {
    var foundCall = false
    var exceptionFound: Exception? = null


    open fun returnWhenException(exception: Exception): Any? = null
    abstract fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any>
    abstract fun getClass(): KClass<Call<*>>

    override fun prepare(block: Test.Config.() -> Unit): Test {
       val test =Test()
        test.onError = {er ->returnWhenException(er)}
        return  test
    }
}