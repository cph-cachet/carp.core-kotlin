package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlin.reflect.KClass


actual abstract class UnknownPolymorphicSerializer<P: Any, W: P> actual constructor( wrapperClass: KClass<W>, verifyUnknownPolymorphicWrapper: Boolean ) : KSerializer<P>
{
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

    actual override fun serialize( output: Encoder, obj: P )
    {
        @Suppress( "NAME_SHADOWING" )
        val output = output.beginStructure( descriptor )

        if ( obj is UnknownPolymorphicWrapper )
        {
            output.encodeStringElement( descriptor, 0, obj.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on accessing private properties dynamically since no raw JSON can be output using KOutput.
            val composer = output.asDynamic().w_0
            val toPrint = "," + obj.jsonSource // The ',' is needed since it is normally added by the Encoder which is not called here.
            composer.`print_61zpoe$`( toPrint ) // This is the generated 'print' function overload for strings.
        }
        else
        {
            val saver = PolymorphicSerializer.getSerializerBySimpleClassName( obj::class.simpleName!! )
            output.encodeStringElement( descriptor, 0, saver.descriptor.name )
            output.encodeSerializableElement( descriptor, 1, saver, obj )
        }

        output.endStructure( descriptor )
    }

    actual override fun deserialize( input: Decoder ): P
    {
        @Suppress("NAME_SHADOWING" )
        val input = input.beginStructure( descriptor )

        // Determine class to be loaded and whether it is available at runtime.
        input.decodeElementIndex( descriptor )
        val className = input.decodeStringElement( descriptor, 0 )
        val canLoadClass = PolymorphicSerializer.isSerializerByQualifiedNameRegistered( className )

        // Deserialize object when serializer is available, or wrap in case type is unknown.
        val obj: P
        input.decodeElementIndex( descriptor )
        if ( canLoadClass )
        {
            @Suppress( "UNCHECKED_CAST" )
            val loader = PolymorphicSerializer.getSerializerByQualifiedName( className ) as KSerializer<P>
            obj = input.decodeSerializableElement( descriptor, 1, loader )
        }
        else
        {
            // TODO: Currently the following relies on accessing properties dynamically and is probably specific to JSON parsing.

            val parser = input.asDynamic().p_0

            // Get source string.
            val jsonSource: String = parser.source as String

            // Find starting position of the unknown object.
            val start = parser.tokenPos as Int

            // Find end position of the unknown object by skipping to the next element.
            // Skipping the element is also needed since otherwise deserialization of subsequent elements fails.
            parser.skipElement()
            val end = parser.tokenPos as Int

            // Initialize wrapper for unknown object based on source string.
            val elementSource = jsonSource.subSequence( start, end ).toString()
            obj = createWrapper( className, elementSource )
        }

        input.endStructure( descriptor )

        return obj
    }

    actual abstract fun createWrapper( className: String, json: String ): W
}