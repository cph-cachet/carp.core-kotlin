package dk.cachet.carp.client.domain

import dk.cachet.carp.deployment.domain.MasterDeviceDeployment


/**
 * Describes the status of a [StudyRuntime].
 * TODO: This might also need to be turned into a sealed class representing a state machine at some point (similar to StudyDeploymentStatus).
 */
interface StudyRuntimeStatus
{
    /**
     * Unique ID of the study runtime on the [ClientManager].
     */
    val id: StudyRuntimeId

    /**
     * Determines whether the device has retrieved its [MasterDeviceDeployment] and was able to load all the necessary plugins to execute the study.
     */
    val isDeployed: Boolean

    /**
     * In case deployment succeeded ([isDeployed] is true), this contains all the information on the study to run.
     * TODO: This should be consumed within this domain model and not be public.
     *       Currently, it is in order to work towards a first MVP which includes server/client communication through the domain model.
     */
    val deploymentInformation: MasterDeviceDeployment?
}
