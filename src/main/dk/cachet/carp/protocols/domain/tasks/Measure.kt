package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.common.Immutable
import dk.cachet.carp.protocols.domain.data.CustomDataType
import dk.cachet.carp.protocols.domain.data.DataType
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.protocols.domain.serialization.UnknownPolymorphicSerializer
import kotlinx.serialization.Serializable


object DataTypeSerializer : UnknownPolymorphicSerializer<DataType, CustomDataType>( CustomDataType::class )
{
    override fun createWrapper( className: String, json: String ): CustomDataType = CustomDataType( className, json )
}


/**
 * Defines data that needs to be measured/collected as part of a task defined by [TaskDescriptor].
 */
@Serializable
abstract class Measure : Immutable( notImmutableErrorFor( Measure::class ) )
{
    /**
     * The type of data this measure collects.
     */
    @Serializable( with = DataTypeSerializer::class )
    abstract val type: DataType
}