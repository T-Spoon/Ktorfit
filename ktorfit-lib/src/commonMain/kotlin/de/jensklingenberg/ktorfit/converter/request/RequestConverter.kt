package de.jensklingenberg.ktorfit.converter.request

import kotlin.reflect.KClass

public interface RequestConverter {

    /**
     * Check if converter supports the types
     * @return true if this converter can convert [parameterType] to [requestType]
     */
    public fun supportedType(parameterType: KClass<*>, requestType: KClass<*>): Boolean

    /**
     * Convert given [data]
     * @return the converted [data]
     */
    public fun convert(data: Any): Any
}