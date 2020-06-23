package com.example.android_without_desugaring

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.cachet.carp.common.TimeSpan
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import dk.cachet.carp.protocols.infrastructure.toJson
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnabledDesugaringTest {
    @Test
    fun create_study_protocol_and_stringify_to_json_with_desugaring_enabled() {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )
        
        val phone = Smartphone( "Patient's phone" )
        {
            samplingConfiguration {
                geolocation { interval = TimeSpan.fromMinutes( 15.0 ) }
            }
        }
        protocol.addMasterDevice( phone )

        val measures: List<Measure> = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepCount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        val snapshot = protocol.getSnapshot()
        assert(snapshot.creationDate != null)

        // Look after desugaring tag in Logcat to see the output and verify a date is printed out.
        Log.v("desugaring", "Creation date is: ${snapshot.creationDate}.")

        /**
         *  Fails, on Android pre API 26, in [Immutable] on line 93, because because property
         *  val dk.cachet.carp.common.DateTime.dateTime: java.time.Instant! classifier is null.
         *  As seen the return type for the property is java.time.Instant!.
          */
        snapshot.toJson()
    }
}