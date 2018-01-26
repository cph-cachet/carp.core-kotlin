package carp.protocols.domain.tasks


data class StubTaskDescriptor(
    override val name: String = "Stub task",
    override val measures: Iterable<Measure> = listOf() ) : TaskDescriptor()