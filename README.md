# Domain Model and Application Service Definitions for all CARP Subsystems

[![Publish snapshots status](https://github.com/cph-cachet/carp.core-kotlin/workflows/Publish%20snapshots/badge.svg?branch=develop)](https://github.com/cph-cachet/carp.core-kotlin/actions?query=workflow%3A%22Publish+snapshots%22) 

This project is part of the [CACHET Research Platform (CARP)](http://carp.cachet.dk/)—an infrastructure supporting researchers in defining, deploying, and monitoring research studies involving distributed data collection. Following [domain-driven design](https://en.wikipedia.org/wiki/Domain-driven_design), this project contains all domain models and application services for all CARP subsystems (depicted below), not having any dependencies on concrete infrastructure. As such, this project defines an open standard for distributed data collection, [available for Kotlin, the Java runtime, and JavaScript](#multiplatform).

![Subsystem decomposition](https://i.imgur.com/qexzTej.png) 

Currently this project contains an unstable (not backwards compatible) alpha version of the domain model and applications services of the `carp.protocols`, `carp.deployment`, `carp.client`, and `carp.studies` subsystems. Many changes will happen as the rest of the infrastructure is implemented.

In case you want to contribute, please follow our [contribution guidelines](https://github.com/cph-cachet/carp.core-kotlin/blob/develop/CONTRIBUTING.md). 

## carp.protocols [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.protocols/carp.protocols.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.protocols) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.protocols/carp.protocols.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/protocols/)

Specify study protocols which can be deployed to one or more stationary or mobile devices. Using this library, CARP-compatible studies can be defined:

```
// Create a new study protocol.
val owner = ProtocolOwner()
val protocol = StudyProtocol( owner, "Track patient movement" )

// Define which devices are used for data collection.
val phone = Smartphone( "Patient's phone" )
{
    // Configure device-specific options, e.g., frequency to collect data at.
    samplingConfiguration {
        stepcount { interval = TimeSpan.fromMinutes( 15.0 ) }
    }
}
protocol.addMasterDevice( phone )

// Define what needs to be measured, on which device, when.
val measures: List<Measure> = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepcount() )
val startMeasures = ConcurrentTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json: String = protocol.getSnapshot().toJson()
```

## carp.studies [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.studies/carp.studies.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.studies) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.studies/carp.studies.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/studies/)

Manage the recruitment for and lifetime of study deployments, instantiated using a study protocol from `carp.protocols`. Studies are managed through the `StudyService` application service:

```
val studyService: StudyService = createStudiesEndpoint()

// Create a new study.
val studyOwner = StudyOwner()
var studyStatus: StudyStatus = studyService.createStudy( studyOwner, "Example study" )
val studyId: UUID = studyStatus.studyId

// Let the study use the protocol from the 'carp.protocols' example above.
val trackPatientStudy: StudyProtocol = createExampleProtocol()
val patientPhone: AnyMasterDeviceDescriptor = trackPatientStudy.masterDevices.first() // "Patient's phone"
val protocolSnapshot: StudyProtocolSnapshot = trackPatientStudy.getSnapshot()
studyStatus = studyService.setProtocol( studyId, protocolSnapshot )

// Add a participant.
val email = EmailAddress( "participant@email.com" )
val participant: Participant = studyService.addParticipant( studyId, email )

// Once all necessary study options have been configured, the study can go live.
if ( studyStatus is StudyStatus.Configuring && studyStatus.canGoLive )
{
    studyStatus = studyService.goLive( studyId )
}

// Once the study is live, you can 'deploy' it to participant's devices. They will be invited.
if ( studyStatus.canDeployToParticipants )
{
    // Create a 'participant group' with a single participant, using the "Patient's phone".
    val participation = AssignParticipantDevices( participant.id, setOf( patientPhone.roleName ) )
    val participantGroup = setOf( participation )

    val groupStatus: ParticipantGroupStatus = studyService.deployParticipantGroup( studyId, participantGroup )
    val isInvited = groupStatus.studyDeploymentStatus is StudyDeploymentStatus.Invited // True.
}
```

## carp.deployment [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.deployment/carp.deployment.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.deployment) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.deployment/carp.deployment.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/deployment/)

The deployment subsystem contains common concerns to 'running' a study. A study deployment is responsible for managing registration of participant consent, tracking device connection issues, assessing data quality, and negotiating the connection between separate devices. Most calls to this subsystem are abstracted away by `carp.studies` and `carp.client`, so usually you won't call its endpoints directly. Study deployments are managed through the `DeploymentService` application service:

```
val deploymentService: DeploymentService = createDeploymentEndpoint()
val trackPatientStudy: StudyProtocol = createExampleProtocol()
val patientPhone: Smartphone = trackPatientStudy.masterDevices.first() as Smartphone // "Patient's phone"

// This is called by `StudyService` when deploying a participant group.
var status: StudyDeploymentStatus = deploymentService.createStudyDeployment( trackPatientStudy.getSnapshot() )
val studyDeploymentId = status.studyDeploymentId

// What comes after is called by `ClientManager` in `carp.client`:
// - Register the device to be deployed.
val registration = patientPhone.createRegistration {
    // Device-specific registration options can be accessed from here.
    // Depending on the device type, different options are available.
    // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
    deviceId = "xxxxxxxxx"
}
status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

// - Retrieve information on what to run and indicate the device is ready to collect the requested data.
val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
{
    val deploymentInformation: MasterDeviceDeployment =
        deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
    val deploymentChecksum: Int = deploymentInformation.getChecksum() // To verify correct deployment.
    deploymentService.deploymentSuccessful( studyDeploymentId, patientPhone.roleName, deploymentChecksum )
}

// Now that all devices have been registered and deployed, the deployment is ready.
status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
val isReady = status is StudyDeploymentStatus.DeploymentReady // True.
```

## carp.client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.client/carp.client.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.client) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.client/carp.client.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/client/)

Manage the runtime logic for studies on client devices. For example, the following initializes a the smartphone client of a participant that got invited to the example study above:

```
val deploymentService = createDeploymentEndpoint()

// Retrieve invitation to participate in the study using a specific device.
val account: Account = getLoggedInUser()
val invitation: ParticipationInvitation = deploymentService.getParticipationInvitations( account.id ).first()
val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
val deviceToUse: String = invitation.deviceRoleNames.first() // This matches "Patient's phone".

// Create a study runtime for the study.
val clientManager: SmartphoneManager = createSmartphoneManager( deploymentService )
val runtime: StudyRuntime = clientManager.addStudy( studyDeploymentId, deviceToUse )
var isDeployed = runtime.isDeployed // True, because there are no dependent devices.

// Suppose a deployment also depends on a "Clinician's phone" to be registered; deployment cannot complete yet.
// After the clinician's phone has been registered, attempt deployment again.
isDeployed = runtime.tryDeployment() // True once dependent clients have been registered.
```

## carp.common [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.common/carp.common/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.common) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.common/carp.common?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/common/)

Helper classes and base types relied upon by all subsystems. This library does not contain any domain logic.

## carp.test [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.test/carp.test/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.test) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.test/carp.test?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/test/)

Helper classes relied upon by test projects of all subsystems. E.g., to disable tests specified in common part of projects for the JavaScript runtime only.

## carp.detekt

Includes static code analysis extensions for [detekt](https://github.com/arturbosch/detekt), used when building this project to ensure conventions are followed.

# Multiplatform
<a name="multiplatform"></a>

This is a [multiplatform Kotlin library](https://kotlinlang.org/docs/reference/multiplatform.html) which targets both the **Java Runtime Environment (JRE)** and **JavaScript (JS)**. However, due to (current) limitations of Kotlin, the JS runtime is missing certain features (as indicated by the ignored tests when tests are run for JS):

- The `Immutable` base class does not enforce immutable implementations of extending classes.

As this project progresses, we intend to include [native targets](https://kotlinlang.org/docs/reference/native-overview.html) as well, starting with iOS.

# Setting up using IntelliJ IDEA 2020
- Open the project folder in IntelliJ 2020.
- Install the Kotlin plugin (1.3.72-release-IJ2020.1-1) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`
- To build/test/publish, click "Edit Configurations" to add configurations for [the included Gradle tasks](#gradle-tasks), or run them from the Gradle tool window.

# Gradle tasks
<a name="gradle-tasks"></a>

For `carp.core-kotlin`:
- **build**: Builds the full project, for both runtimes, including running unit tests and code analysis.
- **cleanAllTests jvmTest**: Test the full project using JUnit5. `cleanAllTests` is optional, but ensures that test results always show up in IntelliJ; when tasks haven't changed it otherwise lists "Test events were not received".
- **jsTest**: Test the full project using Mocha. Test results only show up in the build output and not in IntelliJ.
- **verifyTsDeclarations**: Verify whether the TypeScript declarations of all modules, defined in `typescript-declarations`, match the compiled JS sources and work at runtime.
- **detekt**: Run code analysis, ignoring failures. Output will still be successful in case code smells are detected. Apply `--rerun-tasks` to always see output, even when ran before.
- **detektPasses**:: Run code analysis, but do not ignore failures. Output will list failure in case code smells are detected.
- **publishSigned**: Publish all projects to Maven using the version number specified in `ext.globalVersion`. This includes documentation, sources, and signing. For this to work you need to configure a `publish.properties` file with a signing signature and repository user in the project root folder. See main `build.gradle` for details.
- **publishSnapshot**: Publish a snapshot build for all projects to Maven, substituting the suffix of the version specified in `ext.globalVersion` with `-SNAPSHOT`.
