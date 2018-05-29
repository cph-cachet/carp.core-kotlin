package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.json.JSON
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.isAccessible


private object UnknownPolymorphicClassDesc : SerialClassDescImpl( Any::class.qualifiedName!! )
{
    override val kind: KSerialClassKind = KSerialClassKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "object" )
    }
}


/**
 * A serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 */
abstract class UnknownPolymorphicSerializer<P: Any, W: P>( wrapperClass: KClass<W> ) : KSerializer<P>
{
    init
    {
        // Enforce that wrapper type (W) implements UnknownPolymorphicWrapper.
        // Due to current limitations in Kotlin (stemming from the JVM) this can not be statically enforced: https://stackoverflow.com/q/43790137/590790
        val implementsInterface: Boolean = wrapperClass.supertypes.contains( UnknownPolymorphicWrapper::class.createType() )
        if ( !implementsInterface )
        {
            throw IllegalArgumentException( "'$wrapperClass' must implement '${UnknownPolymorphicWrapper::class}'." )
        }
    }

    override val serialClassDesc: KSerialClassDesc
        get() = UnknownPolymorphicClassDesc

    override fun save( output: KOutput, obj: P )
    {
        @Suppress("NAME_SHADOWING")
        val output = output.writeBegin( serialClassDesc )

        if ( obj is UnknownPolymorphicWrapper )
        {
            output.writeStringElementValue( serialClassDesc, 0, obj.className )

            // Output raw JSON as originally wrapped.
            // TODO: This relies on reflection since no raw JSON can be output using KOutput.
            output.writeElement( serialClassDesc, 1 )
            val composerField = output::class.members.first { m -> m.name == "w" }
            composerField.isAccessible = true
            val composer = composerField.call( output ) as JSON.Composer
            composer.print( obj.jsonSource )
        }
        else
        {
            val saver = serializerByValue( obj )
            output.writeStringElementValue( serialClassDesc, 0, saver.serialClassDesc.name )
            output.writeSerializableElementValue( serialClassDesc, 1, saver, obj )
        }

        output.writeEnd( serialClassDesc )
    }

    override fun load( input: KInput ): P
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