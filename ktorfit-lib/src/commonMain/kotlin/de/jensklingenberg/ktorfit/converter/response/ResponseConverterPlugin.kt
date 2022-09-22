package de.jensklingenberg.ktorfit.converter.response

import de.jensklingenberg.ktorfit.internal.InternalKtorfitApi
import de.jensklingenberg.ktorfit.upperBoundType
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass

/**
 * Foo<Bar> to Bar and Bar to Foo<Bar>
 * extend KtorPlugin
 * val jvmClient = HttpClient {
 * installKtorfitPlugins(ResponseResponseConverterPlugin(),CallResponseConverterPlugin())
 * }
 *
 * normal KtorPlugin but extend
 */

abstract class ResponseConverterPlugin : HttpClientPlugin<ResponseConverterPlugin.KtorfitPluginErrorHandler, ResponseConverterPlugin.KtorfitPluginErrorHandler> {

    data class KtorfitPluginErrorHandler(var onError: (Exception) -> Any? = {}, val clazz: KClass<*>)

    /**
     * Key of the plugin, must be unique
     */
    abstract fun getKey() : String

    /**
     * Don't override this
     */
    @InternalKtorfitApi
    override val key: AttributeKey<KtorfitPluginErrorHandler> = AttributeKey("Ktorfit"+getKey())

    private var foundCall = false
    private var exceptionFound: Exception? = null


    /**
     * Override this, when you want to receive an exception that occurs
     * while the Ktor request happens or while parsing the response
     * @return the data that should be returned instead of the exception or
     * null when you don`t want to intercept
     */
    open fun onErrorReturn(exception: Exception): Any? = null
    abstract fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any>

    /**
     * Set the class that this plugin should be use for
     */
    abstract fun pluginForClass(): KClass<*>

    override fun prepare(block: KtorfitPluginErrorHandler.() -> Unit): KtorfitPluginErrorHandler {
        return KtorfitPluginErrorHandler ({ er -> onErrorReturn(er) },pluginForClass())
    }

    override fun install(plugin: KtorfitPluginErrorHandler, scope: HttpClient) {
        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
            val result = if (info.type == pluginForClass()) {
                foundCall = true
                HttpResponseContainer(info.upperBoundType()!!, body)
            } else {
                HttpResponseContainer(info, body)
            }

            proceedWith(result)
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.Parse) { (info, body) ->
            val httpResponseContainer = HttpResponseContainer(info, body)

            try {
                proceedWith(httpResponseContainer)
            } catch (ex: Exception) {
                exceptionFound = ex
            }
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.After) { (info, body) ->
            val response = this.context.response

            val result = if (foundCall) {
                val (newInfo, call) = convert(info, body, response)
                HttpResponseContainer(newInfo, call)
            } else {
                HttpResponseContainer(info, body)
            }

            this.context.response
            foundCall = false
            proceedWith(result)
        }

    }

}