package de.jensklingenberg.ktorfit.demo

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.RequestConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RxRequestConverter : RequestConverter {

    override fun supportedType(returnTypeName: TypeData): Boolean {
        return listOf("io.reactivex.rxjava3.core.Single", "io.reactivex.rxjava3.core.Observable","io.reactivex.rxjava3.core.Completable").contains(
            returnTypeName.packageName
        )
    }

    fun test(vava : suspend RxRequestConverter.()->Unit){

    }

    inline suspend fun <reified T> RxRequestConverter.hey(){

    }
    override fun <PRequest> convertRequest(
        returnTypeName: TypeData,
        requestFunction: suspend () -> Pair<PRequest, HttpResponse>,
        ktorfit: Ktorfit
    ): Any {

        test {
            hey<String>()
        }
        return when (returnTypeName.packageName) {
            "io.reactivex.rxjava3.core.Single" -> {
                Single.create<PRequest> { e ->

                    try {
                        GlobalScope.launch {
                            val result = async {
                                try {
                                    val (info, response) = requestFunction()
                                    info
                                }catch (ex: Exception){
                                    e.onError(ex)
                                    null
                                }
                            }
                            val success = result.await()
                            success?.let { e.onSuccess(it) }
                        }
                    } catch (ex: NumberFormatException) {
                        println("dfsdfsdfsdfds")
                        e.onError(ex)
                    }

                }
            }
            "io.reactivex.rxjava3.core.Observable" -> {
                Observable.create<PRequest> { e ->
                    try {
                        GlobalScope.launch {
                            val result = async {
                                try {
                                    val (info, response) = requestFunction()
                                    info
                                }catch (ex: Exception){
                                    e.onError(ex)
                                    null
                                }
                            }
                            val success = result.await()
                            success?.let {
                                e.onNext(it)
                                e.onComplete()
                            }
                        }
                    } catch (ex: NumberFormatException) {
                        println("dfsdfsdfsdfds")
                        e.onError(ex)
                    }
                }
            }

            "io.reactivex.rxjava3.core.Completable" -> {
                Completable.create { e ->
                    try {
                        GlobalScope.launch {
                            val result = async {
                                try {
                                    val (info, response) = requestFunction()
                                    response
                                }catch (ex: Exception){
                                    e.onError(ex)
                                    null
                                }
                            }
                            val success = result.await()
                            success?.let {
                                e.onComplete()
                            }
                        }
                    } catch (ex: NumberFormatException) {
                        e.onError(ex)
                    }
                }
            }
            else -> {
                throw NullPointerException()
            }
        }


    }

}
