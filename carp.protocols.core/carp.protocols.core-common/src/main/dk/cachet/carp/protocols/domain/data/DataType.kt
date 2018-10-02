package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.protocols.domain.notImmutableErrorFor
import dk.cachet.carp.common.serialization.UnknownPolymorphicSerializer
import kotlinx.serialization.*


internal object DataTypeSerializer : UnknownPolymorphicSerializer<DataType, CustomDataType>( CustomDataType::class )
{
    override fun createWrapper( className: String, json: String ): CustomDataType = CustomDataType( className, json )
}


/**
 * Defines a type of data which can be measured/collected.
 */
@Serializable
abstract class DataType : Immutable( notImmutableErrorFor( DataType::class ) )
{
    @Transient
    abstract val category: DataCategory
}