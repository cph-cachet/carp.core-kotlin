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
val measures = listOf( Smartphone.geolocation(), Smartphone.stepcount() )
val startMeasures = ConcurrentTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json = protocol.getSnapshot().toJson()
```

## carp.studies

Manage the recruitment for and lifetime of study deployments, instantiated using a study protocol from `carp.protocols`.

## carp.deployment

The deployment subsystem contains common concerns to 'running' a study, i.e., instantiating a study protocol with a specific set of devices and users as specified in the study protocol. A study deployment is responsible for managing registration of participant consent, tracking device connection issues, assessing data quality, and negotiating the connection between separate devices. Study deployments are managed through the `DeploymentManager` application service:
```
val protocol: StudyProtocol = createSmartphoneStudy()
val manager: DeploymentManager = createDeploymentEndpoint()
val status: StudyDeploymentStatus = manager.createStudyDeployment( protocol.getSnapshot() )
val smartphone = status.registrableDevices.first().device as Smartphone
val registration = smartphone.createRegistration {
    // Device-specific registration options can be accessed from here.
    // Depending on the device type, different options are available.
    // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
    deviceId = "xxxxxxxxx"
}
manager.registerDevice( status.studyDeploymentId, smartphone.roleName, registration )

// Call from the smartphone to retrieve all the necessary information to start running the study on this device.
val deviceDeployment: MasterDeviceDeployment
    = manager.getDeviceDeploymentFor( status.studyDeploymentId, smartphone.roleName )
```

## carp.common

Helper classes and base types relied upon by all subsystems. This library does not contain any domain logic.

## carp.test

Helper classes relied upon by test projects of all subsystems. E.g., to disable tests specified in common part of projects for the JavaScript runtime only.

# Setting up using IntelliJ IDEA 2019
- Open the project folder in IntelliJ 2019.
- Install the Kotlin plugin (1.3.41-release-IJ2019.1-1) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`

# Multiplatform

This is a multiplatform Kotlin library which targets both the **Java Runtime Environment (JRE)** and **JavaScript (JS)**. However, due to (current) limitations of Kotlin, the JS runtime is missing certain features (as indicated by the ignored tests when tests are run for JS):

- The `Immutable` base class does not enforce immutable implementations of extending classes.

# Gradle tasks

For `carp.core-kotlin`:
- **build**: Builds the full project, for both runtimes.
- **cleanAllTests jvmTest**: Test the full project using JUnit5. `cleanAllTests` is optional, but ensures that test results always show up in IntelliJ; when tasks haven't changed it otherwise lists "Test events were not received".
- **jsTest**: Test the full project using Mocha. Test results only show up in the build output and not in IntelliJ.
- **publishSigned**: Publish all projects to Maven using the version number specified in `ext.globalVersion`. This includes documentation, sources, and signing. For this to work you need to configure a `publish.properties` file with a signing signature and repository user in the project root folder. See main `build.gradle` for details.
- **publishSnapshot**: Publish a snapshot build for all projects to Maven, substituting the suffix of the version specified in `ext.globalVersion` with `-SNAPSHOT`.