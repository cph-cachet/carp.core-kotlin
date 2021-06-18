package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.protocols.domain.StudyProtocol


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
        protocol.devices
            // Get measures per device.
            .flatMap { device ->
                protocol.getTasksForDevice( device )
                    .flatMap { it.measures.filterIsInstance<Measure.DataStream>() }
                    .map { device to it }
            }
            // Filter out measures which are not supported on the device.
            .filter { (device, measure) ->
                measure.type !in device.getSupportedDataTypes()
            }
            .map { (device, measure) -> UnexpectedMeasure( device, measure ) }
            .toSet()
}
