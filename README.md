# Domain Model and Application Service Definitions for all CARP Subsystems
This project is part of the [CACHET Research Platform (CARP)](https://github.com/cph-cachet/carp.documentation). It contains all domain models and application services [for all CARP subsystems](https://github.com/cph-cachet/carp.documentation/wiki/Repository-design-overview). These represent an open standard and may not have any dependencies on concrete infrastructure.

Currently this project is under development and only contains an initial unstable alpha version of the domain model and applications services of the `carp.protocols` and `carp.deployment` subsystem, and a placeholder for `carp.studies`. Many changes will happen as the rest of the infrastructure is implemented. Once a minimum viable product is completed, a first version will be released and more documentation will be added. 

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
val sensors = phone.SENSOR_MEASURES
val measures = listOf( sensors.geolocation(), sensors.stepcount() )
val startMeasures = IndefiniteTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json = protocol.getSnapshot().toJson()
```

## carp.studies

Manage the recruitment for and lifetime of study deployments, instantiated using a study protocol from `carp.protocols`.

## carp.deployment

A deployment contains common concerns to 'running' a study, i.e., instantiating a study protocol with a specific set of devices and users as specified in the study protocol. A deployment is responsible for managing registration of participant consent, tracking device connection issues, assessing data quality, and negotiating the connection between separate devices. Deployments are managed through the `DeploymentManager` application service:
```
val protocol: StudyProtocol = createSmartphoneStudy()
val manager: DeploymentManager = createDeploymentEndpoint()
val status: DeploymentStatus = manager.createDeployment( protocol.getSnapshot() )
val deploymentId: UUID = UUID( status.deploymentId )
val smartphone = status.registrableDevices.first().device as Smartphone
val registration = smartphone.createRegistration() // Modify to configure.
manager.registerDevice( deploymentId, smartphone.roleName, registration )

// Call from the smartphone to retrieve all the necessary deployment information to start running the study.
val deviceDeployment: DeviceDeployment = manager.getDeploymentFor( deploymentId, smartphone.roleName )
```

## carp.common

Helper classes and base types relied upon by all subsystems. This library does not contain any domain logic.

## carp.test

Helper classes relied upon by test projects of all subsystems. E.g., to disable tests specified in common part of projects for the JavaScript runtime only.

# Setting up using IntelliJ IDEA
- Install Gradle 4.10 (e.g., [using Chocolatey on Windows](https://chocolatey.org/packages/gradle))
- Install the Kotlin plugin (1.3.31-release-IJ2019.1-1) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`
- Open project in IntelliJ (`File->Open`) by selecting the `build.gradle` file in the root directory and point to local gradle distribution in the wizard which appears (this can be changed after in `Settings->Build, Execution, Deployment->Build Tools->Gradle`)

# Multiplatform

This is a multiplatform Kotlin library which targets both the **Java Runtime Environment (JRE)** and **JavaScript (JS)**. However, due to (current) limitations of Kotlin, the JS runtime is missing certain features (as indicated by the ignored tests when tests are run for JS):

- The `Immutable` base class does not enforce immutable implementations of extending classes.

# Gradle tasks

For `carp.core-kotlin`:
- **build**: Builds the full project, for both runtimes.
- **clean jvmTest**: Test the full project using JUnit5. `clean` is optional, but ensures that test results always show up in IntelliJ; when tasks haven't changed it otherwise lists "Test events were not received".
- **jsTest**: Test the full project using Mocha. Test results only show up in the build output and not in IntelliJ.

For `:carp.*.core` libraries:
- **publishSigned**: Prepare all jars to be published to Maven. This includes documentation, sources, and signing.