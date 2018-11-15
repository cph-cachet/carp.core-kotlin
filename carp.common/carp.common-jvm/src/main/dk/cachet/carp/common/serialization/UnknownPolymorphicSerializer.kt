package dk.cachet.carp.common.serialization

import kotlinx.serialization.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.isAccessible


actual abstract class UnknownPolymorphicSerializer<P: Any, W: P> actual constructor( wrapperClass: KClass<W>, verifyUnknownPolymorphicWrapper: Boolean ) : KSerializer<P>
{
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

    actual override fun serialize( output: Encoder, obj: P )
    {
        @Suppress( "NAME_SHADOWING" )
        val output = output.beginStructure( descriptor )

        if ( obj is UnknownPolymorphicWrapper )
        {
            output.encodeStringElement( descriptor, 0, obj.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on reflection since no raw JSON can be output using KOutput.
            val composerField = output::class.members.first { it.name == "w" }
            composerField.isAccessible = true
            val composer = composerField.call( output )!!
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
            // TODO: Currently the following relies on reflection and is probably specific to JSON parsing.

            // Get source string.
            val parserField = input::class.members.first { m -> m.name == "p" }
            parserField.isAccessible = true
            val parser = parserField.call( input ) as Any
            val parserMembers = parser::class.members
            val sourceField = parserMembers.first { m -> m.name == "source" }
            sourceField.isAccessible = true
            val jsonSource = sourceField.call( parser ) as String

            // Find starting position of the unknown object.
            val curPosField = parserMembers.first { m -> m.name == "tokenPos" }
            curPosField.isAccessible = true
            val start = curPosField.call( parser ) as Int

            // Find end position of the unknown object by skipping to the next element.
            // Skipping the element is also needed since otherwise deserialization of subsequent elements fails.
            val skipElementFunction = parserMembers.first { m -> m.name == "skipElement" }
            skipElementFunction.isAccessible = true
            skipElementFunction.call( parser )
            val end = curPosField.call( parser ) as Int

            // Initialize wrapper for unknown object based on source string.
            val elementSource = jsonSource.subSequence( start, end ).toString()
            obj = createWrapper( className, elementSource )
        }

        input.endStructure( descriptor )

        return obj
    }

    actual abstract fun createWrapper( className: String, json: String ): W
}