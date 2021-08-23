package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    override val measures: List<Measure> = emptyList(),
    override val description: String? = null,
    private val _interactionDataTypes: Set<DataType> = emptySet()
) : TaskDescriptor
{
    override fun getInteractionDataTypes(): Set<DataType> = _interactionDataTypes
}
