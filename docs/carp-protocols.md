# carp.protocols [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.protocols/carp.protocols.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.protocols) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.protocols/carp.protocols.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/protocols/) 

Implements open standards which can describe a study protocol—how a study should be run. Essentially, this subsystem has no technical dependencies on any particular sensor technology or application as it merely describes why, when, and what data should be collected.

## Domain objects

To configure a `StudyProtocol`, the following domain objects are involved:

![Protocols Domain Objects](https://i.imgur.com/Qy9KIWS.png)

- [`StudyProtocol`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/StudyProtocol.kt): A description of how a study is to be executed, defining the 'master' devices responsible for aggregating data, the optional devices connected to them, and the `Trigger`s which lead to data collection on said devices.
- [`DeviceDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/DeviceDescriptor.kt): Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone that collects data which can be incorporated into the platform after it has been processed by a 'master device' (potentially itself).
- [`MasterDeviceDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/MasterDeviceDescriptor.kt): A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself). Typically, a desktop computer, smartphone, or web server.
- [`TaskDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/TaskDescriptor.kt): Describes requested `Measure`s and/or output to be presented on a device.
- [`Measure`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/measures/Measure.kt): Defines data that needs to be measured/collected for a supported `DataType`.
- [`DataType`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/DataType.kt): Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
- [`Trigger`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/Trigger.kt): Any condition on a device which starts or stops tasks at certain points in time when the condition applies. The condition can either be time-bound, based on data streams, initiated by a user of the platform, or a combination of these.
- [`TriggeredTask`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/TriggeredTask.kt): Specifies a task which at some point during a `StudyProtocol` gets sent to a specific device. This allows modeling triggers which trigger multiple tasks targeting multiple devices.
- [`SamplingConfiguration`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/SamplingConfiguration.kt): Contains configuration on how to sample data.
- [`DataTypeSamplingScheme`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/DataTypeSamplingScheme.kt): Specifies the possible sampling configuration options for a `DataType`, including defaults and constraints.