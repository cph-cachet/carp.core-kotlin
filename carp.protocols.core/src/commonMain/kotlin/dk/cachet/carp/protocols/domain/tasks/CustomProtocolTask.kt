package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable


/**
 * A [TaskDescriptor] which contains a definition on how to run tasks, measures, and triggers which differs from the CARP domain model.
 */
@Serializable
data class CustomProtocolTask(
    override val name: String,
    /**
     * A definition on how to run a study on a master device, serialized as a string.
     */
    val studyProtocol: String
) : TaskDescriptor
{
    /**
     * This list is empty, since measures are defined in [studyProtocol] in a different format.
     */
    @Serializable( MeasuresSerializer::class )
    override val measures: List<Measure> = emptyList()
}
