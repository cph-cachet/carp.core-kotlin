# Domain Model and Application Service Definitions for all CARP Subsystems
This project is part of the [CACHET Research Platform (CARP)](https://github.com/cph-cachet/carp.documentation). It contains all domain models and application services [for all CARP subsystems](https://github.com/cph-cachet/carp.documentation/wiki/Repository-design-overview). These represent an open standard and may not have any dependencies on concrete infrastructure.

Currently this project is under development and only contains an initial unstable alpha version of the domain model and applications services of the `carp.protocols` subsystem. Many changes will happen as the rest of the infrastructure is implemented. Once a minimum viable product is completed, a first version will be released and more documentation will be added. 

## carp.protocols

Specify study protocols which can be deployed to one or more stationary or mobile devices. Using this library, CARP-compatible studies can be defined:
```
// Create a new study protocol.
val owner = ProtocolOwner()
val protocol = StudyProtocol( owner, "Example study" )

// Define which devices are used for data collection.
val phone = Smartphone( "Patient phone" )
protocol.addMasterDevice( phone )

// Define what needs to be measured, on which device, when.
val measures = listOf( GpsMeasure(), StepCountMeasure() )
val startMeasures = IndefiniteTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json = protocol.getSnapshot().toJson()
```

# Setting up using IntelliJ IDEA
- Install Gradle 4.9 (e.g., [using Chocolatey on Windows](https://chocolatey.org/packages/gradle))
- Install the Kotlin plugin (1.2.61-release-IJ2018.2-1) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`
- Install the [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization#working-in-intellij-idea) IDE [plugin for the matching Kotlin version (0.6.1)](https://teamcity.jetbrains.com/viewLog.html?buildId=lastPinned&buildTypeId=KotlinTools_KotlinxSerialization_KotlinCompilerWithSerializationPlugin&tab=artifacts&guest=1)
- Open project in IntelliJ (`File->Open`) by selecting the `build.gradle` file in the root directory and point to local gradle distribution in the wizard which appears (this can be changed after in `Settings->Build, Execution, Deployment->Build Tools->Gradle`)

# Multiplatform

This is a multiplatform Kotlin library which targets both the **Java Runtime Environment (JRE)** and **JavaScript (JS)**. However, due to (current) limitations of Kotlin, the JS runtime is missing certain features (as indicated by the ignored tests when tests are run for JS):

- The `Immutable` base class does not enforce immutable implementations of extending classes.
- (De)serialization does not work.

The use of this library on a JS runtime is thus somewhat restricted (e.g., `protocol.getSnapshot().toJson()` won't work), but all other functionality works as expected.

# Gradle tasks

For `carp.core-kotlin`:
- **build**: Builds the full project, for both runtimes.

For `:carp.protocols.core:carp.protocols.core-jvm`:
- **test**: Test compiled Java classes using JUnit.
- **publishSigned**: Prepare all jars to be published to Maven. This includes documentation, sources, and signing.

For `:carp.protocols.core:carp.protocols.core-js`:
- **test**: Test compiled JavaScript sources using mocha.
