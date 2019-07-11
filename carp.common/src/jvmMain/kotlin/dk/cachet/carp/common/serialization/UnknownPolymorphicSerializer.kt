package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.isAccessible


actual abstract class UnknownPolymorphicSerializer<P: Any, W: P> actual constructor( wrapperClass: KClass<W>, verifyUnknownPolymorphicWrapper: Boolean ) : KSerializer<P>
{
    companion object
    {
        private val unsupportedException
            = SerializationException( "${UnknownPolymorphicSerializer::class.simpleName} only supports JSON serialization." )
    }

    init
    {
        // Enforce that wrapper type (W) implements UnknownPolymorphicWrapper.
        // Due to current limitations in Kotlin (stemming from the JVM) this cannot be statically enforced: https://stackoverflow.com/q/43790137/590790
        if ( verifyUnknownPolymorphicWrapper )
        {
            val implementsInterface: Boolean = wrapperClass.supertypes.contains( UnknownPolymorphicWrapper::class.createType() )
            if ( !implementsInterface )
            {
                throw IllegalArgumentException( "'$wrapperClass' must implement '${UnknownPolymorphicWrapper::class}'." )
            }
        }
    }

    actual override val descriptor: SerialDescriptor
        get() = UnknownPolymorphicClassDesc

    actual override fun serialize( encoder: Encoder, obj: P )
    {
        if ( encoder !is JsonOutput )
        {
            throw unsupportedException
        }

        @Suppress( "NAME_SHADOWING" )
        val encoder = encoder.beginStructure( descriptor )

        if ( obj is UnknownPolymorphicWrapper )
        {
            encoder.encodeStringElement( descriptor, 0, obj.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on reflection since no raw JSON can be output using KOutput.
            val composerField = encoder::class.members.first { it.name == "composer" }
            composerField.isAccessible = true
            val composer = composerField.call( encoder )!!
            val printMethod = composer::class.members.first {
                it.name == "print" &&
                it.parameters.any { it.type.classifier == String::class }
            }
            printMethod.isAccessible = true
            printMethod.call( composer, "," + obj.jsonSource ) // The ',' is needed since it is normally added by the Encoder which is not called here.
        }
        else
        {
            val saver = PolymorphicSerializer.getSerializerBySimpleClassName( obj::class.simpleName!! )
            encoder.encodeStringElement( descriptor, 0, saver.descriptor.name )
            encoder.encodeSerializableElement( descriptor, 1, saver, obj )
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
        val canLoadClass = PolymorphicSerializer.isSerializerByQualifiedNameRegistered( className )

        // Deserialize object when serializer is available, or wrap in case type is unknown.
        val obj: P
        decoder.decodeElementIndex( descriptor )
        if ( canLoadClass )
        {
            @Suppress( "UNCHECKED_CAST" )
            val loader = PolymorphicSerializer.getSerializerByQualifiedName( className ) as KSerializer<P>
            obj = decoder.decodeSerializableElement( descriptor, 1, loader )
        }
        else
        {
            // TODO: Currently the following relies on reflection and is probably specific to JSON parsing.

            // Get source string.
            val readerField = decoder::class.members.first { m -> m.name == "reader" }
            readerField.isAccessible = true
            val reader = readerField.call( decoder ) as Any
            val parserMembers = reader::class.members
            val sourceField = parserMembers.first { m -> m.name == "source" }
            sourceField.isAccessible = true
            val jsonSource = sourceField.call( reader ) as String

            // Find starting position of the unknown object.
            val curPosField = parserMembers.first { m -> m.name == "tokenPosition" }
            curPosField.isAccessible = true
            val start = curPosField.call( reader ) as Int

            // Find end position of the unknown object by skipping to the next element.
            // Skipping the element is also needed since otherwise deserialization of subsequent elements fails.
            val skipElementFunction = parserMembers.first { m -> m.name == "skipElement" }
            skipElementFunction.isAccessible = true
            skipElementFunction.call( reader )
            val end = curPosField.call( reader ) as Int

            // Initialize wrapper for unknown object based on source string.
            val elementSource = jsonSource.subSequence( start, end ).toString()
            obj = createWrapper( className, elementSource, json )
        }

        decoder.endStructure( descriptor )

        return obj
    }

    actual abstract fun createWrapper( className: String, json: String, serializer: Json ): W
}