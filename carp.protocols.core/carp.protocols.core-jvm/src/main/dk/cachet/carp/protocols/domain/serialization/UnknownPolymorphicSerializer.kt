package dk.cachet.carp.protocols.domain.serialization

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

    actual override val serialClassDesc: KSerialClassDesc
        get() = UnknownPolymorphicClassDesc

    actual override fun save( output: KOutput, obj: P )
    {
        @Suppress( "NAME_SHADOWING" )
        val output = output.writeBegin( serialClassDesc )

        if ( obj is UnknownPolymorphicWrapper )
        {
            output.writeStringElementValue( serialClassDesc, 0, obj.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on reflection since no raw JSON can be output using KOutput.
            output.writeElement( serialClassDesc, 1 )
            val composerField = output::class.members.first { it.name == "w" }
            composerField.isAccessible = true
            val composer = composerField.call( output )!!
            val printMethod = composer::class.members.first {
                it.name == "print" &&
                it.parameters.any { it.type.classifier == String::class }
            }
            printMethod.isAccessible = true
            printMethod.call( composer, obj.jsonSource )
        }
        else
        {
            val saver = serializerByValue( obj )
            output.writeStringElementValue( serialClassDesc, 0, saver.serialClassDesc.name )
            output.writeSerializableElementValue( serialClassDesc, 1, saver, obj )
        }

        output.writeEnd( serialClassDesc )
    }

    actual override fun load( input: KInput ): P
    {
        @Suppress("NAME_SHADOWING" )
        val input = input.readBegin( serialClassDesc )

        // Determine class to be loaded and whether it is available at runtime.
        input.readElement( serialClassDesc )
        val className = input.readStringElementValue( serialClassDesc, 0 )
        var canLoadClass = false
        try
        {
            Class.forName( className )
            canLoadClass = true
        }
        catch ( e: ClassNotFoundException ) { }

        // Deserialize object when serializer is available, or wrap in case type is unknown.
        val obj: P
        input.readElement( serialClassDesc )
        if ( canLoadClass )
        {
            val loader = serializerBySerialDescClassname<P>( className )
            obj = input.readSerializableElementValue( serialClassDesc, 1, loader )
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

        input.readEnd( serialClassDesc )

        return obj
    }

    actual abstract fun createWrapper( className: String, json: String ): W
}