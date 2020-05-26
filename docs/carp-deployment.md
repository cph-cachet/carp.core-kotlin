# carp.deployment [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.deployment/carp.deployment.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.deployment) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.deployment/carp.deployment.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/deployment/) 

Maps the information specified in a study protocol to runtime configurations used by the 'client' subystem to run the protocol on concrete devices (e.g., a smartphone) and allow researchers to monitor their state.
To start collecting data, participants need to be invited, devices need to be registered, and consent needs to be given to collect the requested data.

## Study and device deployment state

Most of the [the `DeploymentService` endpoints](#application-service) return the current status of a study deployment after the requested operation has been executed.
Depending on the current state of the deployment, different operations are available.
This is represented by [`StudyDeploymentStatus`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/domain/StudyDeploymentStatus.kt), which reflects the underlying state machine:

![Study deployment state machine](https://i.imgur.com/e6u5Om7.png)

The overall deployment state depends on the aggregate of individual device deployment states.
Each device within the study deployment has a corresponding [`DeviceDeploymentStatus`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/domain/DeviceDeploymentStatus.kt):

![Device deployment state machine](https://i.imgur.com/VRfD4wL.png)

## Application service

[DeploymentService](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/application/DeploymentService.kt) allows deploying study protocols, registering participations, and retrieving deployment information for participating master devices as defined in the protocol.

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createStudyDeployment` | Instantiate a study deployment for a given protocol. | | manage deployment: `studyDeploymentId`, in deployment: `studyDeploymentId` |
| `getStudyDeploymentStatus` | Get the status for a study deployment. | in deployment: `studyDeploymentId` | |
| `getStudyDeploymentStatusList` | Get the statuses for a set of deployments. | in deployment: (all) `studyDeploymentIds` | |
| `registerDevice` | Register a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `unregisterDevice` | Unregister a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `getDeviceDeploymentFor` | Get the deployment configuration for a master device in a study deployment. | in deployment: `studyDeploymentId` | |
| `deploymentSuccessful` | Indicate to stakeholders in a study deployment that a master device was deployed successfully, i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it. | in deployment: `studyDeploymentId` | |
| `stop` | Stop a study deployment. No further changes to this deployment will be allowed and no more data will be collected. | in deployment: `studyDeploymentId` | |
| `addParticipation` | Let a person with a specified identity participate in a study deployment, using a specified master device. | manage deployment: `studyDeploymentId` | in deployment (to account with `identity`): `studyDeploymentId` |
| `getParticipationInvitations` | Get all participations in study deployments a specified account has been invited to. | authenticated: `accountId` | |