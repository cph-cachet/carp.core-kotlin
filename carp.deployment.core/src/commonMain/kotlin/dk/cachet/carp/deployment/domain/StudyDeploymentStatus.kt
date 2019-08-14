package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.*
import kotlinx.serialization.Serializable


/**
 * Describes the status of a [StudyDeployment]: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
data class StudyDeploymentStatus(
    @Serializable( with = UUIDSerializer::class )
    val studyDeploymentId: UUID,
    /**
     * The set of all devices which can or need to be registered for this study deployment.
     */
    val registrableDevices: Set<RegistrableDevice>,
    /**
     * The role names of all [registrableDevices] which still require registration for the study deployment to start running.
     */
    val remainingDevicesToRegister: Set<String>,
    /**
     * The role names of all devices which have been registered successfully and are ready for deployment.
     */
    val devicesReadyForDeployment: Set<String> )