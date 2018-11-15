package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * Information on the amount of steps somebody has taken.
 * TODO: Does this data type prescribe how to interpret values of this type? I.e., which time interval does the step count apply to?
 */
@Serializable
data class StepCountDataType( override val category: DataCategory = DataCategory.Movement ) : DataType()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StepCountDataType::class,
                StepCountDataType.serializer(),
                "dk.cachet.carp.protocols.domain.data.StepCountDataType" )
        }
    }
}