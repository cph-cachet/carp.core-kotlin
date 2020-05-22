# carp.studies [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.studies/carp.studies.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.studies) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.studies/carp.studies.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/studies/) 

Supports management of research studies, including the recruitment of participants and assigning metadata (e.g., contact information).
This subsystem maps pseudonymized data (managed by the 'deployment' subsystem) to actual participants.

## Application service

[StudyService](../carp.studies.core/src/commonMain/kotlin/dk/cachet/carp/studies/application/StudyService.kt) allows creating and managing studies.

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.
New users that are allowed to add studies should be given a 'study owner' claim, e.g., their user ID.
In case you want to support organizations this could be the ID of the organization they belong to.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `createStudy` | Create a new study for a specified owner. | study owner: `owner.id` | manage study: `studyId` |
| `setInternalDescription` | Set study details which are visible only to the owner. | manage study: `studyId` | |
| `getStudyDetails` | Gets detailed information about a study, including which study protocol is set. | manage study: `studyId` | |
| `getStudyStatus` | Get the status for a study. | manage study: `studyId` | |
| `getStudiesOverview` | Get status for all studies created by the specified owner. | study owner: `owner.id` | |
| `addParticipant` | Add a participant identified by a specified email address to a study. | manage study: `studyId` | |
| `getParticipants` | Get all participants for a study. | manage study: `studyId` | |
| `setInvitation` | Specify an invitation, shared with participants once they are invited to a study. | manage study: `studyId` | |
| `setProtocol` | Specify the study protocol to use for a study. | manage study: `studyId` | |
| `goLive` | Lock in the current study protocol so that a study may be deployed to participants. | manage study: `studyId` | |
| `deployParticipantGroup` | Deploy a study to a group of previously added participants. | manage study: `studyId` | |
| `getParticipantGroupStatusList` | Get the status of all deployed participant groups in a study. | manage study: `studyId` | |
| `stopParticipantGroup` | Stop the study deployment in a study of a participant group. No further changes to this deployment will be allowed and no more data will be collected. | manage study: `studyId` | |