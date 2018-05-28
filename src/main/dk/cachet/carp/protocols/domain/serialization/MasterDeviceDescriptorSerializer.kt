package dk.cachet.carp.protocols.domain.serialization

import dk.cachet.carp.protocols.domain.devices.CustomMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.MasterDeviceDescriptor
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlin.reflect.jvm.isAccessible


internal object MasterDeviceDescriptorClassDesc : SerialClassDescImpl( MasterDeviceDescriptor::class.qualifiedName!! )
{
    override val kind: KSerialClassKind = KSerialClassKind.POLYMORPHIC

    init
    {
        addElement( "klass" )
        addElement( "device" )
    }
}


/**
 * A serializer for [MasterDeviceDescriptor] which wraps extending types unknown at runtime in a [CustomMasterDeviceDescriptor].
 * TODO: This class can likely be made generic to remove duplication for types which need similar treatment.
 */
object MasterDeviceDescriptorSerializer : KSerializer<MasterDeviceDescriptor>
{
    override val serialClassDesc: KSerialClassDesc
        get() = MasterDeviceDescriptorClassDesc

    override fun save( output: KOutput, obj: MasterDeviceDescriptor )
    {
        @Suppress("NAME_SHADOWING" )
        val output = output.writeBegin( serialClassDesc )

        val saver = serializerByValue( obj )
        output.writeStringElementValue( serialClassDesc, 0, saver.serialClassDesc.name )
        output.writeSerializableElementValue( serialClassDesc, 1, saver, obj )

        output.writeEnd( serialClassDesc )
    }

    override fun load( input: KInput ): MasterDeviceDescriptor
    {
        @Suppress("NAME_SHADOWING" )
        val input = input.readBegin( serialClassDesc )

        // Determine class to be loaded and whether it is available at runtime.
        input.readElement( serialClassDesc )
        val klassName = input.readStringElementValue( serialClassDesc, 0 )
        var canLoadClass = false
        try
        {
            Class.forName( klassName )
            canLoadClass = true
        }
        catch ( e: ClassNotFoundException ) { }

        // Deserialize object when serializer is available, or wrap in a 'Custom' object in case type is unknown.
        var obj: MasterDeviceDescriptor
        input.readElement( serialClassDesc )
        if ( canLoadClass )
        {
            val loader = serializerBySerialDescClassname<Any>( klassName )
            obj = input.readSerializableElementValue( serialClassDesc, 1, loader ) as MasterDeviceDescriptor
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

            // Element needs to be skipped, otherwise subsequent deserialization fails.
            val skipElementFunction = parserMembers.first { m -> m.name == "skipElement" }
            skipElementFunction.isAccessible = true
            skipElementFunction.call( parser )

            obj = CustomMasterDeviceDescriptor( jsonSource )
        }

        input.readEnd( serialClassDesc )

        return obj
    }
}