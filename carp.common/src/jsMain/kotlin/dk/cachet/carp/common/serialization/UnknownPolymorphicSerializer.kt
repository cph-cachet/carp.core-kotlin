package dk.cachet.carp.common.serialization

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonOutput
import kotlin.reflect.KClass


actual abstract class UnknownPolymorphicSerializer<P : Any, W : P> actual constructor(
    private val baseClass: KClass<P>,
    wrapperClass: KClass<W>,
    verifyUnknownPolymorphicWrapper: Boolean
) : KSerializer<P>
{
    companion object
    {
        private val unsupportedException =
            SerializationException( "${UnknownPolymorphicSerializer::class.simpleName} only supports JSON serialization." )
    }

    init
    {
        // Enforce that wrapper type (W) implements UnknownPolymorphicWrapper.
        // Due to current limitations in Kotlin (stemming from the JVM) this cannot be statically enforced: https://stackoverflow.com/q/43790137/590790
        if ( verifyUnknownPolymorphicWrapper )
        {
            // TODO: Reflection in JavaScript runtime is limited and therefore I currently see no way to access implemented interfaces to verify this.
            //       This is not a major issue since JVM runtime unit tests do cover this.
        }
    }

    actual override val descriptor: SerialDescriptor
        get() = UnknownPolymorphicClassDesc

    actual override fun serialize( encoder: Encoder, value: P )
    {
        if ( encoder !is JsonOutput )
        {
            throw unsupportedException
        }

        @Suppress( "NAME_SHADOWING" )
        val encoder = encoder.beginStructure( descriptor )

        if ( value is UnknownPolymorphicWrapper )
        {
            encoder.encodeStringElement( descriptor, 0, value.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on accessing private properties dynamically since no raw JSON can be output using KOutput.
            val composer = encoder.asDynamic().composer_0
            val toPrint = "," + value.jsonSource // The ',' is needed since it is normally added by the Encoder which is not called here.
            composer.`print_61zpoe$`( toPrint ) // This is the generated 'print' function overload for strings.
        }
        else
        {
            val registeredSerializer = encoder.context.getPolymorphic( baseClass, value )
                ?: throw SerializationException( "${value.asDynamic().constructor.name} is not registered for polymorph serialization." )

            @Suppress( "UNCHECKED_CAST" )
            val saver = registeredSerializer as KSerializer<P>
            encoder.encodeStringElement( descriptor, 0, saver.descriptor.serialName )
            encoder.encodeSerializableElement( descriptor, 1, saver, value )
        }

        encoder.endStructure( descriptor )
    }

    actual override fun deserialize( decoder: Decoder ): P
    {
        // Get JSON serializer. This serializer assumes JSON serialization.
        if ( decoder !is JsonInput )
        {
            throw unsupportedException
        }
        val json = decoder.json

        @Suppress("NAME_SHADOWING" )
        val decoder = decoder.beginStructure( descriptor )

        // Determine class to be loaded and whether it is available at runtime.
        decoder.decodeElementIndex( descriptor )
        val className = decoder.decodeStringElement( descriptor, 0 )
        val registeredSerializer = decoder.context.getPolymorphic( baseClass, className )
        val canLoadClass = registeredSerializer != null

        // Deserialize object when serializer is available, or wrap in case type is unknown.
        val obj: P
        decoder.decodeElementIndex( descriptor )
        if ( canLoadClass )
        {
            @Suppress( "UNCHECKED_CAST" )
            val loader = registeredSerializer as KSerializer<P>
            obj = decoder.decodeSerializableElement( descriptor, 1, loader )
        }
        else
        {
            // TODO: Currently, the following relies on accessing properties dynamically.

            val reader = decoder.asDynamic().`reader_8be2vx$`

            // Get source string.
            val jsonSource: String = reader.source_0 as String

            // Find starting position of the unknown object.
            val start = reader.tokenPosition_0 as Int

            // Find end position of the unknown object by skipping to the next element.
            // Skipping the element is also needed since otherwise deserialization of subsequent elements fails.
            reader.skipElement()
            val end = reader.tokenPosition_0 as Int

            // Initialize wrapper for unknown object based on source string.
            val elementSource = jsonSource.subSequence( start, end ).toString()
            obj = createWrapper( className, elementSource, json )
        }

        decoder.endStructure( descriptor )

        return obj
    }

    actual abstract fun createWrapper( className: String, json: String, serializer: Json ): W
}
