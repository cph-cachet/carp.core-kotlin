# carp.clients [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.clients/carp.clients.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.clients) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.clients/carp.clients.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/clients/) 

This is the runtime which performs the actual data collection on a device (e.g., desktop computer or smartphone).
This subsystem contains reusable components which understand the runtime configuration derived from a study protocol by the ‘deployment’ subsystem.
Integrations with sensors are loaded through a 'device data collector' plug-in system to decouple sensing—not part of core—from sensing logic.

[`ClientManager`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/application/ClientManager.kt) is the main entry point into this subsystem.
Concrete devices extend on it, e.g., [`SmartphoneClient`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/domain/SmartphoneClient.kt) manages data collection on a smartphone.

## Study state

[`StudyStatus`](../carp.clients.core/src/commonMain/kotlin/dk/cachet/carp/clients/application/study/StudyStatus.kt) represents the status of a single study which runs on `ClientManager`.

![Study state machine](https://i.imgur.com/fi348XB.png)