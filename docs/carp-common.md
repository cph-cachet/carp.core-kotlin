# carp.common [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.common/carp.common/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.common) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.common/carp.common?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/common/)

Implements helper classes and base types relied upon by all subsystems.
Primarily, this contains the [built-in types](#built-in-types) used to [define study protocols](carp-protocols.md#domain-objects)
which subsequently get passed to the deployments and clients subsystem.

## Built-in types

### Data types

`DataType`s are identified by a given _name_ within a _namespace_ and prescribe the data contained within each data point when measured.
When a data type describes data over the course of a time interval, the time interval is stored within the header (shared by all data types) and not in data-type specific data.

All of the built-in data types belong to the namespace: **dk.cachet.carp**.

| Name | Description |
| --- | --- |
| [freeformtext](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/FreeFormText.kt) | Text of which the interpretation is left up to the specific application. |
| [geolocation](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Geolocation.kt) | Geographic location data, representing longitude and latitude. |
| [ecg](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/ECG.kt) | Electrocardiogram data of a single lead. |
| [heartrate](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/HeartRate.kt) | Number of heart contractions (beats) per minute. |
| [rrinterval](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/RRInterval.kt) | The time interval between two consecutive heartbeats (R-R interval). |
| [sensorskincontact](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/SensorSkinContact.kt) | Whether a sensor requiring contact with skin is making proper contact at a specific point in time. |
| [stepcount](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/StepCount.kt) | The number of steps a participant has taken in a specified time interval. |
| [acceleration](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Acceleration.kt) | Acceleration along perpendicular x, y, and z axes. |
| [signalstrength](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/SignalStrength.kt) | The received signal strength of a wireless device. |
| [triggeredtask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/TriggeredTask.kt) | A task which was started or stopped by a trigger, referring to identifiers in the study protocol. |

### Device descriptors

| Class | Master | Description |
| --- | :---: | --- |
| [Smartphone](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/Smartphone.kt) | Yes | An internet-connected phone with built-in sensors. |
| [AltBeacon](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/AltBeacon.kt) | | A beacon meeting the open AltBeacon standard. |
| [BLEHeartRateDevice](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/BLEHeartRateDevice.kt) | | A Bluetooth device which implements a Heart Rate service. |
| [CustomProtocolDevice](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/devices/CustomProtocolDevice.kt) | Yes | A master device which uses a single `CustomProtocolTask` to determine how to run a study on the device. |

### Sampling schemes

Supports specifying the sampling scheme for a [`DataType`](#data-types), including possible options, defaults, and constraints.

Some sampling schemes support specifying a different sampling configuration depending on how much battery is left,
indicated by the "Battery-aware" column.
These extend from [BatteryAwareSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/BatteryAwareSampling.kt).

| Class | Battery-aware | Description |
| --- | :---: | --- |
| [AdaptiveGranularitySampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/AdaptiveGranularitySampling.kt) | Yes | Specify a desired level of granularity at which to measure depending on the battery level. |
| [GranularitySampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/GranularitySampling.kt) | | Specify a desired level of granularity, corresponding to expected degrees of power consumption. |
| [IntervalSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/IntervalSampling.kt) | | Specify a time interval in between subsequent measurements. |
| [NoOptionsSampling](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/sampling/NoOptionsSampling.kt) | | Does not allow any sampling configuration. |

### Tasks

| Class | Description |
| --- | --- |
| [BackgroundTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/BackgroundTask.kt) | Specifies that all containing measures and/or output should immediately start running in the background once triggered. |
| [CustomProtocolTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/CustomProtocolTask.kt) | Contains a definition on how to run tasks, measures, and triggers which differs from the CARP domain model. |
| [WebTask](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/tasks/WebTask.kt) | Redirects to a web page which contains the task which needs to be performed. |

### Triggers

| Class | Description |
| --- | --- |
| [ElapsedTimeTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ElapsedTimeTrigger.kt) | Triggers after a specified amount of time has elapsed since the start of a study deployment. |
| [ScheduledTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ScheduledTrigger.kt) | Trigger using a recurring schedule starting on the date that the study starts, specified using [the iCalendar recurrence rule standard](https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html). |
| [ManualTrigger](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/triggers/ManualTrigger.kt) | Initiated by a user, i.e., the user decides when to start a task. |
