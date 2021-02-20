# carp.deployment [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.deployment/carp.deployment.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.deployment) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.deployment/carp.deployment.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/deployment/) 

Maps the information specified in a study protocol to runtime configurations used by the 'client' subystem to run the protocol on concrete devices (e.g., a smartphone) and allow researchers to monitor their state.
To start collecting data, participants need to be invited, devices need to be registered, and consent needs to be given to collect the requested data.

## Deployment sequence

The following diagram depicts a typical sequence of calls to [application services in the deployment subsystem](#application-service) to create a study deployment, invite participants, and deploy it successfully on a client device.
The events between `DeploymentService` and `ParticipationService` are opaque to the user and are merely an implementation detail.
The application and domain services in `carp.studies` and `carp.client` abstract away this entire sequence.
Matching code for these calls can be found in the main README as part of the [carp.deployment](../README.md#example-deployment) and [carp.client](../README.md#example-client) examples.

![Study deployment sequence diagram](https://i.imgur.com/T9VjzQm.png)

## Study and device deployment state

Most of the [the `DeploymentService` endpoints](#application-service) return the current status of a study deployment after the requested operation has been executed.
Depending on the current state of the deployment, different operations are available.
This is represented by [`StudyDeploymentStatus`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/domain/StudyDeploymentStatus.kt), which reflects the underlying state machine:

![Study deployment state machine](https://i.imgur.com/HGpF8BI.png)

The overall deployment state depends on the aggregate of individual device deployment states.
Each device within the study deployment has a corresponding [`DeviceDeploymentStatus`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/domain/DeviceDeploymentStatus.kt):

![Device deployment state machine](https://i.imgur.com/Mkb78W4.png)

## Application services

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.

### [`DeploymentService`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/application/DeploymentService.kt)

Allows deploying study protocols and retrieving master device deployments for participating master devices as defined in the protocol.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createStudyDeployment` | Instantiate a study deployment for a given protocol. | | manage deployment: `studyDeploymentId`, in deployment: `studyDeploymentId` |
| `removeStudyDeployments` | Remove study deployments and all related data to it. | manage deployment: `studyDeploymentId`| |
| `getStudyDeploymentStatus` | Get the status for a study deployment. | in deployment: `studyDeploymentId` | |
| `getStudyDeploymentStatusList` | Get the statuses for a set of deployments. | in deployment: (all) `studyDeploymentIds` | |
| `registerDevice` | Register a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `unregisterDevice` | Unregister a device for a study deployment. | in deployment: `studyDeploymentId` | |
| `getDeviceDeploymentFor` | Get the deployment configuration for a master device in a study deployment. | in deployment: `studyDeploymentId` | |
| `deploymentSuccessful` | Indicate to stakeholders in a study deployment that a master device was deployed successfully, i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it. | in deployment: `studyDeploymentId` | |
| `stop` | Stop a study deployment. No further changes to this deployment will be allowed and no more data will be collected. | in deployment: `studyDeploymentId` | |

### [`ParticipationService`](../carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/application/ParticipationService.kt)

Allows inviting participants, retrieving participations for study deployments,
and managing data related to participants which is input by users.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `addParticipation` | Let a person with a specified identity participate in a study deployment, using a specified master device. | manage deployment: `studyDeploymentId` | in deployment (to account with `identity`): `studyDeploymentId` |
| `getActiveParticipationInvitations` | Get all participations of active study deployments a specified account has been invited to. | authenticated: `accountId` | |
| `getParticipantData` | Get currently set data for all expected participant data. | in deployment: `studyDeploymentId` | |
| `getParticipantDataList` |  Get currently set data for all expected participant data for a set of study deployments. | in deployment: `studyDeploymentId` | |
| `setParticipantData` | Set participant data for a specified study deployment. | in deployment: `studyDeploymentId` | |
