package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.*


object MultiplatformPolymorphicSerializer
{
    private val simpleNameSerializers = mutableMapOf<String, KSerializer<Any>>()
    private val qualifiedSerializers = mutableMapOf<String, KSerializer<Any>>()

    fun <T: Any> registerSerializer( klass: KClass<T>, qualifiedName: String )
    {
        val className = klass.simpleName!! // TODO: Is this dangerous?
        val serializer = klass.serializer() as KSerializer<Any> // TODO: Is this dangerous?

        simpleNameSerializers[ className ] = serializer
        qualifiedSerializers[ qualifiedName ] = serializer
    }

    fun getSerializerBySimpleClassName( className: String ): KSerializer<Any>
    {
        return simpleNameSerializers[ className ]!!
    }

    fun getSerializerByQualifiedName( qualifiedName: String ): KSerializer<Any>
    {
        return qualifiedSerializers[ qualifiedName ]!!
    }
}
