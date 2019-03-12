package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlin.reflect.*


/**
 * The serialization description for [PolymorphicSerializer].
 */
object PolymorphicSerializerClassDesc : SerialClassDescImpl( "kotlin.Any" )
{
    override val kind: SerialKind = UnionKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "value" )
    }
}


/**
 * A serializer which can (de)serialize registered polymorph types by including type information in the serialized representation (supporting multiplatform).
 *
 * This is a custom version of the PolymorphicSerializer included in `kotlinx.serialization` which relies on types being registered manually.
 * This allows this serializer to also be used when targeting a JavaScript runtime (the normal PolymorphicSerializer is only supported on JVM).
 *
 * The downsides are (due to the fact that the JavaScript runtime does not have access to fully qualified class names):
 * (1) all types that need to be serialized need to be registered by passing their fully qualified name manually.
 * (2) no types with the same name can be registered.
 */
object PolymorphicSerializer : KSerializer<Any>
{
    override val descriptor: SerialDescriptor = PolymorphicSerializerClassDesc

    private val simpleNameSerializers = mutableMapOf<String, KSerializer<Any>>()
    private val qualifiedSerializers = mutableMapOf<String, KSerializer<Any>>()


    fun <T: Any> registerSerializer( klass: KClass<T>, serializer: KSerializer<T>, qualifiedName: String )
    {
        val className = klass.simpleName!! // TODO: I presume anonymous classes don't have a name, but can these be serialized at all?
        @Suppress( "UNCHECKED_CAST" )
        val anySerializer = serializer as KSerializer<Any>

        // Cannot register duplicate class names.
        val error = "For now, polymorphic serialization in JavaScript does not allow duplicate class names."
        if ( simpleNameSerializers.containsKey( className ) )
        {
            throw IllegalArgumentException( "A class with the name '$className$' is already registered. $error" )
        }
        if ( qualifiedSerializers.containsKey( qualifiedName ) )
        {
            throw IllegalArgumentException( "A class with the qualified name '$qualifiedName' is already registered. $error" )
        }

        simpleNameSerializers[ className ] = anySerializer
        qualifiedSerializers[ qualifiedName ] = anySerializer
    }

    fun getSerializerBySimpleClassName( className: String ): KSerializer<Any>
    {
        if ( !simpleNameSerializers.containsKey( className ) )
        {
            throw NoSuchElementException( "No polymorphic serializer is registered for the class '$className'." )
        }

        return simpleNameSerializers[ className ]!!
    }

    fun getSerializerByQualifiedName( qualifiedName: String ): KSerializer<Any>
    {
        if ( !isSerializerByQualifiedNameRegistered( qualifiedName ) )
        {
            throw NoSuchElementException( "No polymorphic serializer is registered with the qualified name '$qualifiedName'." )
        }

        return qualifiedSerializers[ qualifiedName ]!!
    }

    fun isSerializerByQualifiedNameRegistered( qualifiedName: String ): Boolean
    {
        return qualifiedSerializers.containsKey( qualifiedName )
    }

    override fun serialize( encoder: Encoder, obj: Any )
    {
        val saver = getSerializerBySimpleClassName( obj::class.simpleName!! )

        @Suppress( "NAME_SHADOWING" )
        val encoder = encoder.beginStructure( descriptor )
        encoder.encodeStringElement( descriptor, 0, saver.descriptor.name )
        encoder.encodeSerializableElement( descriptor, 1, saver, obj )
        encoder.endStructure( descriptor )
    }

    override fun deserialize( decoder: Decoder ): Any
    {
        @Suppress( "NAME_SHADOWING" )
        val decoder = decoder.beginStructure( descriptor )
        var klassName: String? = null
        var value: Any? = null
        mainLoop@ while ( true )
        {
            when ( decoder.decodeElementIndex( descriptor ) )
            {
                CompositeDecoder.READ_ALL ->
                {
                    klassName = decoder.decodeStringElement( descriptor, 0 )
                    val loader = getSerializerByQualifiedName( klassName )
                    value = decoder.decodeSerializableElement( descriptor, 1, loader )
                    break@mainLoop
                }
                CompositeDecoder.READ_DONE ->
                {
                    break@mainLoop
                }
                0 ->
                {
                    klassName = decoder.decodeStringElement( descriptor, 0 )
                }
                1 ->
                {
                    klassName = requireNotNull( klassName ) { "Cannot read polymorphic value before its type token" }
                    val loader = getSerializerByQualifiedName( klassName )
                    value = decoder.decodeSerializableElement( descriptor, 1, loader )
                }
                else -> throw SerializationException( "Invalid index" )
            }
        }

        decoder.endStructure( descriptor )
        return requireNotNull( value ) { "Polymorphic value have not been read" }
    }
}


/**
 * A serializer for polymorphic [List]'s relying on the [PolymorphicSerializer] (supporting multiplatform).
 */
object PolymorphicArrayListSerializer : KSerializer<List<Any>> by ArrayListSerializer( PolymorphicSerializer )