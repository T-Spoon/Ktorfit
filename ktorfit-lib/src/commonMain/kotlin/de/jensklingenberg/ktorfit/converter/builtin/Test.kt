package de.jensklingenberg.ktorfit.converter.builtin

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*

class Test {

    var onError : (Exception) -> Any? ={}
    class Config {

    }
    fun onTest(){

    }

    fun setEror(eror: (Exception) -> Any?){
        onError = eror
    }

}


class MyPlugin2(val prin: String) : HttpClientPlugin<Test.Config, Test> {
    override val key: AttributeKey<Test> = AttributeKey("MyPlugin2")

    override fun install(plugin: Test, scope: HttpClient) {
        println("INSTALLED2"+prin)
    }

    override fun prepare(block: Test.Config.() -> Unit): Test {
        return Test()
    }
}