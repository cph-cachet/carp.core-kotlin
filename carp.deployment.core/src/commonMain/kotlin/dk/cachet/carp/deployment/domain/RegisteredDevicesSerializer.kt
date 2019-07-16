package dk.cachet.carp.deployment.domain

import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.*


/**
 * Custom serializer for a map containing [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
internal object RegisteredDevicesSerializer : KSerializer<Map<String, DeviceRegistration>>
    by HashMapSerializer( StringSerializer, DeviceRegistrationSerializer )