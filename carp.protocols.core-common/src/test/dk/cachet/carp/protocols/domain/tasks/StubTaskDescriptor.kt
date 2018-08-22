package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.domain.serialization.*


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    @SerializableWith( MeasuresSerializer::class )
    override val measures: List<Measure> = listOf() ) : TaskDescriptor()