package de.jensklingenberg.ktorfit.converter

import de.jensklingenberg.ktorfit.upperBoundType
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass

abstract class KtorfitPlugin : HttpClientPlugin<KtorfitPluginErrorHandler, KtorfitPluginErrorHandler> {

    abstract fun getKey() : String

    /**
     * Don't override this
     */
    override val key: AttributeKey<KtorfitPluginErrorHandler> = AttributeKey("Ktorfit"+getKey())

    private var foundCall = false
    private var exceptionFound: Exception? = null


    open fun returnWhenException(exception: Exception): Any? = null
    abstract fun convert(info: TypeInfo, body: Any, response: HttpResponse): Pair<TypeInfo, Any>
    abstract fun getClass(): KClass<*>

    override fun prepare(block: KtorfitPluginErrorHandler.() -> Unit): KtorfitPluginErrorHandler {
        return KtorfitPluginErrorHandler ({ er -> returnWhenException(er) },getClass())
    }

    override fun install(plugin: KtorfitPluginErrorHandler, scope: HttpClient) {
        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
            val result = if (info.type == getClass()) {
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