# carp.protocols [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.protocols/carp.protocols.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.protocols) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.protocols/carp.protocols.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/protocols/) 

Implements open standards which can describe a study protocol—how a study should be run.
Essentially, this subsystem has no technical dependencies on any particular sensor technology or application as it merely describes why, when, and what data should be collected.

## Domain objects

To configure a `StudyProtocol`, the following domain objects are involved:

![Protocols Domain Objects](https://i.imgur.com/Qy9KIWS.png)

- [`StudyProtocol`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/StudyProtocol.kt):
A description of how a study is to be executed, defining the 'master' devices responsible for aggregating data, the optional devices connected to them, and the `Trigger`s which lead to data collection on said devices.
- [`DeviceDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/DeviceDescriptor.kt):
Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone that collects data which can be incorporated into the platform after it has been processed by a 'master device' (potentially itself).
- [`MasterDeviceDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/MasterDeviceDescriptor.kt):
A device which aggregates, synchronizes, and optionally uploads incoming data received from one or more connected devices (potentially just itself).
Typically, a desktop computer, smartphone, or web server.
- [`TaskDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/TaskDescriptor.kt):
Describes requested `Measure`s and/or output to be presented on a device.
- [`Measure`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/measures/Measure.kt):
Defines data that needs to be measured/collected for a supported `DataType`.
- [`DataType`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/DataType.kt):
Defines a type of data which can be processed by the platform (e.g., measured/collected/uploaded).
- [`Trigger`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/Trigger.kt):
Any condition on a device which starts or stops tasks at certain points in time when the condition applies.
The condition can either be time-bound, based on incoming data, initiated by a user of the platform, or a combination of these.
- [`TriggeredTask`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/TriggeredTask.kt):
Specifies a task which at some point during a `StudyProtocol` gets sent to a specific device.
This allows modeling triggers which trigger multiple tasks targeting multiple devices.
- [`SamplingConfiguration`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/SamplingConfiguration.kt):
Contains configuration on how to sample data.
- [`DataTypeSamplingScheme`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/DataTypeSamplingScheme.kt):
Specifies the possible sampling configuration options for a `DataType`, including defaults and constraints.

## Built-in types

### Data types

`DataType`s are identified by a given _name_ within a _namespace_ and prescribe the data contained within each data point when measured.
When a data type describes data over the course of a time interval, the time interval is stored within the header (shared by all data types) and not in data-type specific data.

All of the built-in data types belong to the namespace: **dk.cachet.carp**.

| Name | Description |
| --- | --- |
| [geolocation](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/carp/Geolocation.kt) | Geographic location data, representing longitude and latitude. |
| [stepcount](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/carp/Stepcount.kt) | The number of steps a participant has taken in a specified time interval. |

### Device descriptors

| Class | Master | Description |
| --- | :---: | --- |
| [Smartphone](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/Smartphone.kt) | Yes | An internet-connected phone with built-in sensors. |
| [AltBeacon](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/AltBeacon.kt) | | A beacon meeting the open AltBeacon standard. |
| [CustomProtocolDevice](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/CustomProtocolDevice.kt) | Yes | A master device which uses a single `CustomProtocolTask` to determine how to run a study on the device. |

### Tasks

| Class | Description |
| --- | --- |
| [ConcurrentTask](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/ConcurrentTask.kt) | Specifies that all containing measures should start immediately once triggered and run indefinitely until all containing measures have completed. |
| [CustomProtocolTask](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/CustomProtocolTask.kt) | Contains a definition on how to run tasks, measures, and triggers which differs from the CARP domain model. |

### Measures

| Class | Description |
| --- | --- |
| [DataTypeMeasure](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/measures/DataTypeMeasure.kt) | Defined by nothing else but a `DataType` identifier. It is up to the client to determine how to handle this measure. |
| [PhoneSensorMeasure](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/measures/PhoneSensorMeasure.kt) | Measures any of the sensors typically integrated in smartphones (e.g., accelerometer), or data which is derived from them using vendor-specific APIs (e.g., stepcount, or mode of transport). |

### Triggers

| Class | Description |
| --- | --- |
| [ElapsedTimeTrigger](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/ElapsedTimeTrigger.kt) | Triggers after a specified amount of time has elapsed since the start of a study deployment. |
| [ScheduledTrigger](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/ScheduledTrigger.kt) | Trigger using a recurring schedule starting on the date that the study starts, specified using [the iCalendar recurrence rule standard](https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html). |
| [ManualTrigger](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/triggers/ManualTrigger.kt) | Initiated by a user, i.e., the user decides when to start a task. |

## Extending domain objects

CARP contains default implementations of the [domain objects which make up a study protocol](#domain-objects) which represent common use cases that have currently been implemented.
In case these do not provide the functionality you require, the following abstract classes can be extended to model your own custom study logic:

- Extend `DeviceDescriptor` or `MasterDeviceDescriptor` to add support for a new type of device, and extend `DeviceRegistration` to specify how a single instance of this device should be uniquely identified, the capabilities it has, and device-specific configuration options needed for the device to operate.
Example: [`AltBeacon`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/devices/AltBeacon.kt).  
- Extend `TaskDescriptor` to provide custom logic on how to schedule the containing `Measure`s, or if you need to trigger custom tasks unrelated to the study protocol in your client application.
- Extend `Measure` in case you need to specify custom options on _what_ to measure for a given `DataType`.
- Specify new `DataType`s by extending from `DataTypeSamplingScheme`, and optionally extend from `SamplingConfiguration` to specify a custom configuration on _how_ your new data type can be measured.
Example: [`Geolocation`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/data/carp/Geolocation.kt)
- Extend `Trigger` to describe custom conditions which you want to use to trigger tasks.

All extending classes (except `DataTypeSamplingScheme`) should be **immutable data classes**.
The domain objects using them expect them to remain unchanged (they are [DDD value objects](https://deviq.com/value-object/)).
To enforce correct implementation of extending objects, the `Immutable` base type they extend from uses reflection to verify whether the class is a data class and whether all members are immutable during initialization; if not, an exception is thrown.
However, due to (current) limitations of Kotlin, the JS runtime currently does not verify this.

Add a `@Serializable` annotation to the class so that it can be serialized by `kotlinx.serialization`.
In most cases this is sufficient, but for more information, check [the serialization documentation for CARP developers](serialization.md).

## Application services

[ProtocolService](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/application/ProtocolService.kt) allows managing multiple versions of study protocols.

The _'Require'_ and _'Grant'_ column lists claim-based authorization recommendations for implementing infrastructures.
Respectively, the required claims and claims to grant upon a successful request.
New users that are allowed to add protocols should be given a 'protocol owner' claim, e.g., their user ID.
In case you want to support organizations this could be the ID of the organization they belong to.

| Endpoint | Description | Require | Grant |
| --- | --- | --- | --- |
| `add` | Add a study protocol. | protocol owner: `protocol.ownerId` |  |
| `update` | Store an updated version of a specified study protocol. | protocol owner: `protocol.ownerId` | |
| `getBy` | Find the study protocol with a specified protocol name owned by a specific owner. | protocol owner: `owner.id` | |
| `getAllFor` | Find all study protocols owned by a specific owner. | protocol owner: `owner.id` | |
| `getVersionHistoryFor` | Returns all stored versions for the study protocol with a given name owned by a specific owner. |  protocol owner: `owner.id` | |

[ProtocolFactoryService](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/application/ProtocolFactoryService.kt) provides factory methods to create common study protocols.
To store these protocols, pass the returned protocols to `ProtocolService`.

| Endpoint | Description |
| --- | --- |
| `createCustomProtocol` | Create a study protocol to be deployed to a single device which has its own way of describing study protocols that deviates from the CARP core study protocol model. |