package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlin.reflect.KClass


internal object UnknownPolymorphicClassDesc : SerialClassDescImpl( "kotlin.Any" )
{
    override val kind: SerialKind = UnionKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "object" )
    }
}


/**
 * A serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param wrapperClass The definition of the class [W] which is used to wrap objects of type [P].
 * @param verifyUnknownPolymorphicWrapper
 *  For this serializer to work, all wrapper classes returned by this serializer need to implement [UnknownPolymorphicWrapper].
 *  In case it is impossible for a base return type to implement this interface you can disable the runtime verification by setting this to false.
 *  However, ensure that all deriving classes of this base type implement [UnknownPolymorphicWrapper], otherwise serialization will not output the original JSON found upon deserializing.
 */
expect abstract class UnknownPolymorphicSerializer<P: Any, W: P>( wrapperClass: KClass<W>, verifyUnknownPolymorphicWrapper: Boolean = true ) : KSerializer<P>
{
    override val descriptor: SerialDescriptor
    override fun serialize( output: Encoder, obj: P )
    override fun deserialize( input: Decoder ): P

    /**
     * Create a wrapper for a class which could not be deserialized since it is not in any loaded assembly.
     *
     * @param className The fully qualified name of the class.
     * @param json The JSON which could not be deserialized.
     */
    abstract fun createWrapper( className: String, json: String ): W
}


/**
 * Create a serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param createWrapper Create the wrapper based on the fully qualified name of the class and the JSON which could not be deserialized.
 */
inline fun <reified P: Any, reified W: P> createUnknownPolymorphicSerializer( crossinline createWrapper: (className: String, json: String) -> W ): UnknownPolymorphicSerializer<P, W>
{
    return object : UnknownPolymorphicSerializer<P, W>( W::class )
    {
        override fun createWrapper( className: String, json: String ): W
        {
            return createWrapper( className, json )
        }
    }
}