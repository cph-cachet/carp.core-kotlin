@file:JsExport

package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.DeviceRegistration
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Master [device] and its current [registration] assigned to participants as part of a [ParticipantGroup].
 */
@Serializable
data class AssignedMasterDevice( val device: AnyMasterDeviceDescriptor, val registration: DeviceRegistration? = null )
