package de.jensklingenberg.ktorfit.converter


import kotlin.reflect.KClass


data class KtorfitPluginErrorHandler(var onError: (Exception) -> Any? = {}, val clazz: KClass<*>)

