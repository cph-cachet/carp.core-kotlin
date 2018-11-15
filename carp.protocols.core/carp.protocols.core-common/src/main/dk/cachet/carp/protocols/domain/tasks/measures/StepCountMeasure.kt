package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable


/**
 * Measure the amount of steps taken as assessed by the device this measure is requested on.
 */
@Serializable
data class StepCountMeasure(
    @Serializable( PolymorphicSerializer::class )
    override val type: DataType = StepCountDataType() ) : DataStreamMeasure()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StepCountMeasure::class,
                StepCountMeasure.serializer(),
                "dk.cachet.carp.protocols.domain.tasks.measures.StepCountMeasure" )
        }
    }
}