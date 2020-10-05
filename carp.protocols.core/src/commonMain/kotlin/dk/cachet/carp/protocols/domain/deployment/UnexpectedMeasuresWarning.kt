package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.data.DataType
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.tasks.measures.Measure


/**
 * Evaluates whether a [StudyProtocol] contains [Measure]s that are requested on a device for which the requested [DataType] is not expected.
 *
 * This is allowed, but requires the client implementation to have corresponding support to handle this unexpected [DataType].
 */
class UnexpectedMeasuresWarning internal constructor() : DeploymentWarning
{
    /**
     * Holds an unexpected [measure] which at some point in the [StudyProtocol] may be sent to [device].
     */
    data class UnexpectedMeasure( val device: AnyDeviceDescriptor, val measure: Measure )

    override val description: String =
        "The study protocol contains measures that are requested on a device for which the requested data type is not expected." +
        "This is allowed, but requires the client implementation to have corresponding support to handle this unexpected data type."


    override fun isIssuePresent( protocol: StudyProtocol ): Boolean = getUnexpectedMeasures( protocol ).any()

    fun getUnexpectedMeasures( protocol: StudyProtocol ): Set<UnexpectedMeasure> =
        protocol.triggers
            // Map each triggered measure to `UnexpectedMeasure`, even though we haven't verified this yet.
            .flatMap { protocol.getTriggeredTasks( it ) }
            .flatMap { triggeredTask ->
                triggeredTask.task.measures.map { UnexpectedMeasure( triggeredTask.targetDevice, it ) } }
            // Only keep triggered measures which are in fact unexpected.
            .filter { it.measure.type !in it.device.supportedDataTypes }
            .toSet()
}
