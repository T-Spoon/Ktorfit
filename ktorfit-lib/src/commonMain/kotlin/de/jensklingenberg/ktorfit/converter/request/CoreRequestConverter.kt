package de.jensklingenberg.ktorfit.converter.request

import de.jensklingenberg.ktorfit.internal.TypeData

interface CoreRequestConverter {

    /**
     * Check if this converter supports the return type
     * @param returnTypeName is the qualified name of the outer type of
     * the return type. e.g. for Flow<String> it will be kotlinx.coroutines.flow.Flow
     */
    fun supportedType(returnTypeName: TypeData): Boolean

}

