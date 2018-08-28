package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlin.reflect.KClass


actual abstract class UnknownPolymorphicSerializer<P: Any, W: P> actual constructor( wrapperClass: KClass<W>, verifyUnknownPolymorphicWrapper: Boolean ) : KSerializer<P>
{
    companion object
    {
        val notSupported: Throwable
            = UnsupportedOperationException( "Serialization of unknown objects is not yet supported for JavaScript." )
    }

    init
    {
        throw notSupported
    }


    actual override val serialClassDesc: KSerialClassDesc
        get() = UnknownPolymorphicClassDesc

    actual override fun save( output: KOutput, obj: P )
    {
        throw notSupported
    }

    actual override fun load( input: KInput ): P
    {
        throw notSupported
    }

    actual abstract fun createWrapper( className: String, json: String ): W
}