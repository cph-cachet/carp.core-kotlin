package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    @Serializable( MeasuresSerializer::class )
    override val measures: List<Measure> = listOf() ) : TaskDescriptor()
{
    companion object
    {
        init
        {
            PolymorphicSerializer.registerSerializer(
                StubTaskDescriptor::class,
                StubTaskDescriptor.serializer(),
                "dk.cachet.carp.protocols.domain.tasks.StubTaskDescriptor" )
        }
    }
}