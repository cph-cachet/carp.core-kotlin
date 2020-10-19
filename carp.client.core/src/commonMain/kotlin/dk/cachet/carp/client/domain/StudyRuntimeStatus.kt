package dk.cachet.carp.client.domain

import dk.cachet.carp.common.ImplementAsDataClass
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor


/**
 * Describes the status of a [StudyRuntime].
 */
@ImplementAsDataClass
sealed class StudyRuntimeStatus
{
    /**
     * Unique ID of the study runtime on the [ClientManager].
     */
    abstract val id: StudyRuntimeId


    /**
     * Deployment cannot succeed yet because other master devices have not been registered yet.
     */
    data class NotReadyForDeployment( override val id: StudyRuntimeId ) : StudyRuntimeStatus()

    /**
     * Deployment can complete after [remainingDevicesToRegister] have been registered.
     */
    data class RegisteringDevices(
        override val id: StudyRuntimeId,
        /**
         * Contains all the information on the study to run.
         *
         * TODO: This should be consumed within this domain model and not be public.
         *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
         */
        val deploymentInformation: MasterDeviceDeployment,
        val remainingDevicesToRegister: List<AnyDeviceDescriptor>
    ) : StudyRuntimeStatus()

    /**
     * Study runtime status when deployment has been successfully completed:
     * the [MasterDeviceDeployment] has been retrieved and all necessary plugins to execute the study have been loaded.
     */
    data class Deployed(
        override val id: StudyRuntimeId,
        /**
         * Contains all the information on the study to run.
         *
         * TODO: This should be consumed within this domain model and not be public.
         *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
         */
        val deploymentInformation: MasterDeviceDeployment
    ) : StudyRuntimeStatus()
}
