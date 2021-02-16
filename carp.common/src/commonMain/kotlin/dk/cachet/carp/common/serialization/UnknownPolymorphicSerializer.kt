package dk.cachet.carp.common.serialization

import dk.cachet.carp.common.reflect.reflectIfAvailable
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass


/**
 * A serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param wrapperClass The definition of the class [W] which is used to wrap objects of type [P].
 * @param verifyUnknownPolymorphicWrapper
 *  For this serializer to work, all wrapper classes returned by this serializer need to implement [UnknownPolymorphicWrapper].
 *  In case it is impossible for a base return type to implement this interface you can disable the runtime verification by setting this to false.
 *  However, ensure that all deriving classes of this base type implement [UnknownPolymorphicWrapper], otherwise serialization will not output the original JSON found upon deserializing.
 */
abstract class UnknownPolymorphicSerializer<P : Any, W : P>(
    baseClass: KClass<P>,
    wrapperClass: KClass<W>,
    verifyUnknownPolymorphicWrapper: Boolean = true
) : KSerializer<P>
{
    companion object
    {
        private fun unsupportedException( cause: Throwable? = null ) =
            SerializationException(
                "${UnknownPolymorphicSerializer::class.simpleName} only supports JSON serialization" +
                " with a class discriminator configured (no array polymorphism) when serializing unknown types.",
                cause
            )
    }

    init
    {
        // Enforce that wrapper type (W) implements UnknownPolymorphicWrapper.
        // Due to current limitations in Kotlin (stemming from the JVM) this cannot be statically enforced: https://stackoverflow.com/q/43790137/590790
        val reflect = reflectIfAvailable()
        if ( reflect != null && verifyUnknownPolymorphicWrapper )
        {
            val implementsInterface: Boolean = reflect.extendsType<UnknownPolymorphicWrapper>( wrapperClass )
            if ( !implementsInterface )
            {
                throw IllegalArgumentException( "'$wrapperClass' must implement '${UnknownPolymorphicWrapper::class}'." )
            }
        }
    }


    private val polymorphicSerializer: PolymorphicSerializer<P> = PolymorphicSerializer( baseClass )
    override val descriptor: SerialDescriptor = polymorphicSerializer.descriptor

    override fun serialize( encoder: Encoder, value: P )
    {
        // An encoder-agnostic fallback allows serialization only if the type is known.
        if ( encoder !is JsonEncoder )
        {
            try
            {
                encoder.encodeSerializableValue( polymorphicSerializer, value )
                return
            }
            catch ( ex: SerializationException )
            {
                // Serialization likely failed because the type is not registered for polymorphic serialization.
                throw unsupportedException( ex )
            }
        }

        // For JSON with class discriminators, we can verify whether the type is known and handle unknown types.
        // TODO: It should also be possible to support array polymorphism, but that is not a priority now.
        getClassDiscriminator( encoder.json ) // Throws on incorrect Json configuration.
        if ( value is UnknownPolymorphicWrapper )
        {
            // Output raw JSON as originally wrapped for unknown types.
            val jsonElement = Json.parseToJsonElement( value.jsonSource )
            encoder.encodeJsonElement( jsonElement )
        }
        else
        {
            // Normal polymorphic serialization for known types.
            encoder.encodeSerializableValue( polymorphicSerializer, value )
        }
    }

    @InternalSerializationApi
    override fun deserialize( decoder: Decoder ): P
    {
        // An encoder-agnostic fallback allows deserialization only if the type is known.
        if ( decoder !is JsonDecoder )
        {
            try
            {
                return decoder.decodeSerializableValue( polymorphicSerializer )
            }
            catch ( ex: SerializationException )
            {
                // Deserialization likely failed because the type is not registered for polymorphic serialization.
                throw unsupportedException( ex )
            }
        }

        // For JSON with class discriminators, we can verify whether the type is known and handle unknown types.
        // TODO: It should also be possible to support array polymorphism, but that is not a priority now.
        val classDiscriminator = getClassDiscriminator( decoder.json )

        // Determine class to be loaded and whether it is available at runtime.
        val jsonElement = decoder.decodeJsonElement()
        val className = jsonElement.jsonObject[ classDiscriminator ]!!.jsonPrimitive.content
        val registeredSerializer = polymorphicSerializer.findPolymorphicSerializerOrNull( decoder, className )
        val canLoadClass = registeredSerializer != null

        // Deserialize object when serializer is available, or wrap in case type is unknown.
        val jsonSource = jsonElement.toString()
        return if ( canLoadClass ) decoder.json.decodeFromString( polymorphicSerializer, jsonSource )
        else createWrapper( className, jsonSource, decoder.json )
    }

    // HACK: Since `Json.configuration` is internal, this is a workaround to find the configured class discriminator.
    //   I requested it to be public: https://github.com/Kotlin/kotlinx.serialization/issues/1323
    private fun getClassDiscriminator( json: Json ): String
    {
        var extractedDiscriminator: String? = null
        Json( json )
        {
            if ( useArrayPolymorphism ) throw unsupportedException()
            extractedDiscriminator = classDiscriminator
        }
        return extractedDiscriminator!!
    }

    /**
     * Create a wrapper for a class which could not be deserialized since it is not in any loaded assembly.
     *
     * @param className The fully qualified name of the class.
     * @param json The JSON which could not be deserialized.
     * @param serializer The [Json] serializer being used to deserialize the object.
     */
    abstract fun createWrapper( className: String, json: String, serializer: Json ): W
}


/**
 * Create a serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param createWrapper Create the wrapper based on the fully qualified name of the class and the JSON which could not be deserialized.
 */
inline fun <reified P : Any, reified W : P> createUnknownPolymorphicSerializer(
    crossinline createWrapper: (className: String, json: String, serializer: Json) -> W
): UnknownPolymorphicSerializer<P, W>
{
    return object : UnknownPolymorphicSerializer<P, W>( P::class, W::class )
    {
        override fun createWrapper( className: String, json: String, serializer: Json ): W
        {
            return createWrapper( className, json, serializer )
        }
    }
}
