package com.example.android_without_desugaring

import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisabledDesugaringTest {
    @Test
    fun create_study_protocol_with_desugaring_disabled() {
        val owner = ProtocolOwner()
        /**
         * Fails on Android pre API 26, because [StudyProtocol.creationDate] on JVM
         * uses [java.time.Instant] which isn't supported on API versions lower than 26.
         */
        StudyProtocol( owner, "Track patient movement" )
    }
}