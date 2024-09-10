package com.example.common.util

import com.google.gson.internal.`$Gson$Preconditions`
import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

class ClazzUtil {
    companion object {
        fun getRawType(type: Type?): Class<*>? {
            return when (type) {
                is Class<*> -> {
                    // type is a normal class.
                    type
                }

                is ParameterizedType -> {
                    // I'm not exactly sure why getRawType() returns Type instead of Class.
                    // Neal isn't either but suspects some pathological case related
                    // to nested classes exists.
                    val rawType = type.rawType

                    `$Gson$Preconditions`.checkArgument(rawType is Class<*>)
                    rawType as Class<*>
                }

                is GenericArrayType -> {
                    val componentType = type.genericComponentType
                    getRawType(componentType)?.let { Array.newInstance(it, 0).javaClass }
                }

                is TypeVariable<*> -> {
                    // we could use the variable's bounds, but that won't work if there are multiple.
                    // having a raw type that's more general than necessary is okay
                    Any::class.java
                }

                is WildcardType -> {
                    getRawType(type.upperBounds[0])
                }

                else -> {
                    val className = if (type == null) "null" else type.javaClass.name
                    throw IllegalArgumentException(
                        "Expected a Class, ParameterizedType, or "
                                + "GenericArrayType, but <" + type + "> is of type " + className
                    )
                }
            }
        }
    }
}