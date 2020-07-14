package dk.cachet.carp.protocols

import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.CustomProtocolDevice
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.CustomProtocolTask
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
        {
            // Configure device-specific options, e.g., frequency to collect data at.
            samplingConfiguration {
                geolocation { interval = TimeSpan.fromMinutes( 15.0 ) }
            }
        }
        protocol.addMasterDevice( phone )

        // Define what needs to be measured, on which device, when.
        val measures: List<Measure> = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepcount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        // JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
        val json: String = protocol.getSnapshot().toJson()
    }

    @Test
    fun custom_protocol() = runBlockingTest {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study for CAMS runtime" )

        val phone = CustomProtocolDevice( "CAMS smartphone" )
        protocol.addMasterDevice( phone )

        val camsProtocol = """{ "custom": "configuration" }""" // Anything which can be serialized to a string.
        val camsTask = CustomProtocolTask( "Monitor diabetes", camsProtocol )
        protocol.addTriggeredTask( phone.atStartOfStudy(), camsTask, phone )

        val json: String = protocol.getSnapshot().toJson()
    }
}
