package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl


object PolymorphicSerializerClassDesc : SerialClassDescImpl( "kotlin.Any" )
{
    override val kind: KSerialClassKind = KSerialClassKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "value" )
    }
}

actual object PolymorphicSerializer : KSerializer<Any>
{
    override val serialClassDesc: KSerialClassDesc = PolymorphicSerializerClassDesc

    override fun save( output: KOutput, obj: Any )
    {
        val saver = MultiplatformPolymorphicSerializer.getSerializerBySimpleClassName( obj::class.simpleName!! )

        @Suppress( "NAME_SHADOWING" )
        val output = output.writeBegin( serialClassDesc )
        output.writeStringElementValue( serialClassDesc, 0, saver.serialClassDesc.name )
        output.writeSerializableElementValue( serialClassDesc, 1, saver, obj )
        output.writeEnd( serialClassDesc )
    }

    override fun load( input: KInput ): Any
    {
        @Suppress( "NAME_SHADOWING" )
        val input = input.readBegin( serialClassDesc )
        var klassName: String? = null
        var value: Any? = null
        mainLoop@ while ( true )
        {
            when ( input.readElement( serialClassDesc ) )
            {
                KInput.READ_ALL ->
                {
                    klassName = input.readStringElementValue( serialClassDesc, 0 )
                    val loader = MultiplatformPolymorphicSerializer.getSerializerByQualifiedName( klassName )
                    value = input.readSerializableElementValue( serialClassDesc, 1, loader )
                    break@mainLoop
                }
                KInput.READ_DONE ->
                {
                    break@mainLoop
                }
                0 ->
                {
                    klassName = input.readStringElementValue( serialClassDesc, 0 )
                }
                1 ->
                {
                    klassName = requireNotNull( klassName ) { "Cannot read polymorphic value before its type token" }
                    val loader = MultiplatformPolymorphicSerializer.getSerializerByQualifiedName( klassName )
                    value = input.readSerializableElementValue( serialClassDesc, 1, loader )
                }
                else -> throw SerializationException( "Invalid index" )
            }
        }

        input.readEnd( serialClassDesc )
        return requireNotNull( value ) { "Polymorphic value have not been read" }
    }
}