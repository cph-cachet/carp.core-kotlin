# Domain Model and Application Service Definitions for all CARP Subsystems

[![Publish snapshots status](https://github.com/cph-cachet/carp.core-kotlin/workflows/Publish%20snapshots/badge.svg?branch=develop)](https://github.com/cph-cachet/carp.core-kotlin/actions?query=workflow%3A%22Publish+snapshots%22) 

This project is part of the [CACHET Research Platform (CARP)](https://github.com/cph-cachet/carp.documentation)—an infrastructure supporting researchers in defining, deploying, and monitoring research studies involving distributed data collection. Following [domain-driven design](https://en.wikipedia.org/wiki/Domain-driven_design), this project contains all domain models and application services for all CARP subsystems (depicted below), not having any dependencies on concrete infrastructure. As such, this project defines an open standard for distributed data collection, [available for Kotlin, the Java runtime, and JavaScript](#multiplatform).

![Subsystem decomposition](https://i.imgur.com/qexzTej.png) 

Currently this project contains an unstable (not backwards compatible) alpha version of the domain model and applications services of the `carp.protocols`, `carp.deployment`, `carp.client`, and `carp.studies` subsystems. Many changes will happen as the rest of the infrastructure is implemented.

In case you want to contribute, please follow our [contribution guidelines](https://github.com/cph-cachet/carp.core-kotlin/blob/develop/CONTRIBUTING.md). 

## carp.protocols [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.protocols/carp.protocols.core)](https://mvnrepository.com/artifact/dk.cachet.carp.protocols) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.protocols/carp.protocols.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/protocols/)

Specify study protocols which can be deployed to one or more stationary or mobile devices. Using this library, CARP-compatible studies can be defined:
```
// Create a new study protocol.
val owner = ProtocolOwner()
val protocol = StudyProtocol( owner, "Example study" )

// Define which devices are used for data collection.
val phone = Smartphone( "Patient's phone" )
protocol.addMasterDevice( phone )

// Define what needs to be measured, on which device, when.
val measures = listOf( Smartphone.geolocation(), Smartphone.stepcount() )
val startMeasures = ConcurrentTask( "Start measures", measures )
protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

// JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
val json = protocol.getSnapshot().toJson()
```

## carp.studies [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.studies/carp.studies.core)](https://mvnrepository.com/artifact/dk.cachet.carp.studies) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.studies/carp.studies.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/studies/)

Manage the recruitment for and lifetime of study deployments, instantiated using a study protocol from `carp.protocols`.

## carp.deployment [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.deployment/carp.deployment.core)](https://mvnrepository.com/artifact/dk.cachet.carp.deployment) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.deployment/carp.deployment.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/deployment/)

The deployment subsystem contains common concerns to 'running' a study, i.e., instantiating a study protocol with a specific set of devices and users as specified in the study protocol. A study deployment is responsible for managing registration of participant consent, tracking device connection issues, assessing data quality, and negotiating the connection between separate devices. Study deployments are managed through the `DeploymentService` application service:
```
val protocol: StudyProtocol = createSmartphoneStudy()
val deploymentService: DeploymentService = createDeploymentEndpoint()
val status: StudyDeploymentStatus = deploymentService.createStudyDeployment( protocol.getSnapshot() )
val smartphone = status.devicesStatus.first().device as Smartphone
val registration = smartphone.createRegistration {
    // Device-specific registration options can be accessed from here.
    // Depending on the device type, different options are available.
    // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
    deviceId = "xxxxxxxxx"
}
deploymentService.registerDevice( status.studyDeploymentId, smartphone.roleName, registration )

// Call from the smartphone to retrieve all the necessary information to start running the study on this device.
val deviceDeployment: MasterDeviceDeployment
    = deploymentService.getDeviceDeploymentFor( status.studyDeploymentId, smartphone.roleName )
```

## carp.client [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.client/carp.client.core)](https://mvnrepository.com/artifact/dk.cachet.carp.client) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.client/carp.client.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/client/)

Manage the runtime logic for studies on client devices. For example, the following initializes a smartphone client:

```
val deploymentService = createDeploymentEndpoint()
val clientManager: SmartphoneManager = createSmartphoneManager( deploymentService )
// Parameters to pass to 'addStudy' are provided by the study service (carp.studies).
val runtime: StudyRuntime = clientManager.addStudy( studyDeploymentId, "Patient's phone" )

// Suppose a deployment also depends on incoming data from a "Clinician's phone"; deployment cannot complete yet.
var isDeployed = runtime.isDeployed // False, since awaiting initialization of clinician's phone.

// After the clinician's phone has been initialized, attempt deployment again.
val status: StudyRuntime.DeploymentState = runtime.tryDeployment()
isDeployed = status.isDeployed // True once dependent clients have been registered.
```

## carp.common [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.common/carp.common)](https://mvnrepository.com/artifact/dk.cachet.carp.common) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.common/carp.common?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/common/)


Helper classes and base types relied upon by all subsystems. This library does not contain any domain logic.

## carp.test [![Maven Central](https://img.shields.io/maven-central/v/dk.cachet.carp.test/carp.test)](https://mvnrepository.com/artifact/dk.cachet.carp.test) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.test/carp.test?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/test/)

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
