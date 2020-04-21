package dk.cachet.carp.protocols

import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.infrastructure.toJson
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


class ProtocolsCodeSamples
{
    @Test
    fun readme() = runBlockingTest {
        // Create a new study protocol.
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )

        // Define which devices are used for data collection.
        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        // Define what needs to be measured, on which device, when.
        val measures: List<Measure> = listOf( Smartphone.geolocation(), Smartphone.stepcount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        // JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
        val json: String = protocol.getSnapshot().toJson()
    }
}
