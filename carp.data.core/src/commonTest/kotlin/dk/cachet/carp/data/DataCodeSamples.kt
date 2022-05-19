package dk.cachet.carp.data

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.Geolocation
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.data.infrastructure.dataStreamId
import kotlinx.coroutines.test.runTest
import kotlin.test.*


class DataCodeSamples
{
    @Test
    fun readme() = runTest {
        val dataStreamService: DataStreamService = createDataStreamEndpoint()
        val studyDeploymentId: UUID = getStudyDeploymentId() // Provided by the 'deployments' subsystem.

        // This is called by the `DeploymentsService` once the deployment starts running.
        val device = "Patient's phone"
        val geolocation = DataStreamsConfiguration.ExpectedDataStream( device, CarpDataTypes.GEOLOCATION.type )
        val stepCount = DataStreamsConfiguration.ExpectedDataStream( device, CarpDataTypes.STEP_COUNT.type )
        val configuration = DataStreamsConfiguration( studyDeploymentId, setOf( geolocation, stepCount ) )
        dataStreamService.openDataStreams( configuration )

        // Upload data from the client.
        val geolocationData = MutableDataStreamSequence<Geolocation>(
            dataStream = dataStreamId<Geolocation>( studyDeploymentId, device ),
            firstSequenceId = 0,
            triggerIds = listOf( 0 ) // Provided by device deployment; maps to the `atStartOfStudy()` trigger.
        )
        val uploadData: DataStreamBatch = MutableDataStreamBatch().apply {
            appendSequence( geolocationData )
        }
        dataStreamService.appendToDataStreams( studyDeploymentId, uploadData )
    }


    private fun createDataStreamEndpoint(): DataStreamService = InMemoryDataStreamService()
    private fun getStudyDeploymentId(): UUID = UUID.randomUUID()
}
