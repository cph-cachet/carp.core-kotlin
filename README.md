# Domain Model and Application Service Definitions for all CARP Subsystems

[![Publish snapshots status](https://github.com/cph-cachet/carp.core-kotlin/workflows/Publish%20snapshots/badge.svg?branch=develop)](https://github.com/cph-cachet/carp.core-kotlin/actions?query=workflow%3A%22Publish+snapshots%22) 

This project is part of the [CACHET Research Platform (CARP)](http://carp.cachet.dk/)—an infrastructure supporting researchers in defining, deploying, and monitoring research studies involving data collection on multiple devices at multiple locations.
Following [domain-driven design](https://en.wikipedia.org/wiki/Domain-driven_design), this project contains all domain models and application services for all CARP subsystems ([depicted below](#architecture)), not having any dependencies on concrete infrastructure.
As such, this project defines an **open standard for distributed data collection**, [available for Kotlin, the Java runtime, and JavaScript](#usage), which others can build upon to create their own infrastructure. 

Two key **design goals** differentiate this project from similar projects:
 
 - **Modularity**: Whether you want to set up your own entire infrastructure, customize part of it but integrate with externally hosted CARP webservices, or simply create an app which collects data locally on a smartphone,  you can 'pick and choose' the subsystems you are interested in and how to deploy them.
 Reference implementations [will be made available](https://github.com/cph-cachet/) (and hosted) by CACHET in the future. 
 - **Extensibility**: Where industry standards exist (such as [Open mHealth](https://www.openmhealth.org/)), the goal is to include them in CARP.
 However, we recognize that many study-specific requirements will always require parts of the infrastructure to be modified.
 Rather than having to fork the entire project and set up your own hosting, domain objects in the protocols subsystem [can be extended to describe study-specific requirements](docs/carp-protocols.md#extending-domain-objects).
 Your custom client (e.g., smartphone application) will need to know how to interpret them, but these additions _are transparent to, and compatible with, the rest of the framework_ when using the provided [built-in serializers](#serialization). 

## Table of Contents

- [Architecture](#architecture)
  - [Protocols](docs/carp-protocols.md)
    - [Domain objects](docs/carp-protocols.md#domain-objects)
    - [Built-in types](docs/carp-protocols.md#built-in-types)
    - [Extending domain objects](docs/carp-protocols.md#extending-domain-objects)
    - [Application service](docs/carp-protocols.md#application-service)
  - [Studies](docs/carp-studies.md)
    - [Application service](docs/carp-studies.md#application-service)
  - [Deployment](docs/carp-deployment.md)
    - [Study and device deployment state](docs/carp-deployment.md#study-and-device-deployment-state)
    - [Application service](docs/carp-deployment.md#application-service)
- [Infrastructure helpers](#infrastructure-helpers)
  - [Serialization](#serialization)
  - [Request objects](#request-objects)
  - [Authorization](#authorization)
  - [Stub classes](#stub-classes)
- [Usage](#usage)
  - [Example](#example)
- [Building the project](#building-the-project)
  - [Gradle tasks](#gradle-tasks)

## Architecture

![Subsystem decomposition](https://i.imgur.com/qexzTej.png)

- [**Protocols**](docs/carp-protocols.md): Implements open standards which can describe a study protocol—how a study should be run. Essentially, this subsystem has no _technical_ dependencies on any particular sensor technology or application as it merely describes why, when, and what data should be collected.

  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.protocols/carp.protocols.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.protocols) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.protocols/carp.protocols.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/protocols/)

- **Studies**: Supports management of research studies, including the recruitment of participants and assigning metadata (e.g., contact information). This subsystem maps pseudonymized data (managed by the 'deployment' subsystem) to actual participants.

  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.studies/carp.studies.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.studies) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.studies/carp.studies.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/studies/)

- **Deployment**: Maps the information specified in a study protocol to runtime configurations used by the 'client' subystem to run the protocol on concrete devices (e.g., a smartphone) and allow researchers to monitor their state. To start collecting data, participants need to be invited, devices need to be registered, and consent needs to be given to collect the requested data.

  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.deployment/carp.deployment.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.deployment) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.deployment/carp.deployment.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/deployment/)

- **Client**: The runtime which performs the actual data collection on a device (e.g., desktop computer or smartphone). This subsystem contains reusable components which understand the runtime configuration derived from a study protocol by the ‘deployment’ subsystem. Integrations with sensors are loaded through a 'data collection' plug-in system to decouple sensing—not part of core⁠—from sensing logic.

   [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.client/carp.client.core/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.client) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.client/carp.client.core?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/client/)

- **Resources**: Contains a simple file store for resources (such as images, videos, and text documents) which can be referenced from within study protocols to be used during a study.
- **Data**: Contains all pseudonymized data. In combination with the original study protocol, the full provenance of the data (when/why it was collected) is known.
- **Analysis**: An analysis subsystem sits in between the data store and 'studies' subsystem, enabling common data analytics but also offering anonimity-preserving features such as k-anonymity.
- **Supporting subystems**:
   - **carp.common**: Helper classes and base types relied upon by all subsystems. This library does not contain any domain logic.
   
     [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.common/carp.common/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.common) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.common/carp.common?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/common/)
     
   - **carp.test**: Helper classes relied upon by test projects of all subsystems. E.g., to disable tests specified in common part of projects for the JavaScript runtime only.
   
     [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dk.cachet.carp.test/carp.test/badge.svg?color=orange)](https://mvnrepository.com/artifact/dk.cachet.carp.test) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/dk.cachet.carp.test/carp.test?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/test/)
     
   - **carp.detekt**: Includes static code analysis extensions for [detekt](https://github.com/arturbosch/detekt), used when building this project to ensure conventions are followed.

Not all subsystems are implemented yet. Currently this project contains an unstable (not backwards compatible) alpha version of the protocols, deployment, client, and studies subsystems. Many changes will happen as the rest of the infrastructure is implemented.

## Infrastructure helpers

Even though this library does not contain dependencies on concrete infrastructure, it does provide building blocks which greatly facilitate hosting the application services defined in this library as a distributed service and consuming them.
You are not required to use these, but they remove some of the boilerplate code you would otherwise have to write.   

### Serialization

To facilitate easy exchange of requests across the different subsystems, all objects that are passed through application services are serializable to JSON using built-in serializers.
This works for both the Java runtime and JavaScript, which is achieved by relying on the [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization) library and compiler plugin.

In addition, domain objects which need to be persisted (aggregate roots) implement [the snapshot pattern](https://howtodoinjava.com/design-patterns/behavioral/memento-design-pattern/).
All snapshots are fully serializable to JSON, making it straightforward to store them in a document store.
But, if you prefer to use a relational database instead, you can call `consumeEvents()` to get all the modifications since the object was last stored.

Lastly, custom serializers to the default ones generated by `kotlinx.serialization` are provided for [extendable types used in study protocols]((docs/carp-protocols.md#extending-domain-objects)) (e.g., `DeviceDescriptor`).
These 'magic' serializers support deserializing extending types which are unknown at runtime, allowing you to access the base properties seamlessly.
Using the built-in serializers thus allows you to handle incoming requests and persistence of extending types you do not have available at compile time.
They are used by default in all objects that need to be serialized for data transfer or snapshot storage.
It is therefore recommended to use built-in serializers to store and transfer any objects containing study protocol information to get this type of extensibility for free.
More detailed information on how this works can be found in [the documentation on serialization for CARP developers](docs/serialization.md).

### Request objects

To help implementing remote procedure calls (RPCs), each application service has matching polymorphic serializable 'request objects'.
For example, the deployment subsystem has a sealed class [`DeploymentServiceRequest`](carp.deployment.core/src/commonMain/kotlin/dk/cachet/carp/deployment/infrastructure/DeploymentServiceRequest.kt) and each subclass represents a request to `DeploymentService`.
Using these objects, all requests to a single application service can be handled by one endpoint using type checking.
We recommend [using a when expression](https://kotlinlang.org/docs/reference/sealed-classes.html) so that the compiler can verify whether you have handled all requests.

In addition, each request object implements [`ServiceInvoker`](carp.common/src/commonMain/kotlin/dk/cachet/carp/common/ddd/ServiceInvoker.kt) which allows calling the matching application service by passing it as an argument to `invokeOn`.
This allows a centralized implementation for any incoming request object to an application service.
However, in practice you might want to perform additional actions depending on specific requests, e.g., [authorization which is currently not part of core](#authorization).

### Authorization

Currently, this library does not contain support for authorization.
Authorization needs to be implemented by concrete infrastructure.
However, CARP is designed with claim-based authorization in mind, and the documentation of application services in each of the subsystems describes a recommended implementation.
 
In a future release we might pass authorization as a dependent service to application services.

### Stub classes

Stub classes are available for the abstract domain objects of the protocols subsystem.
These can be used to write unit tests in which you are not interested in testing the behavior of specific devices, triggers, etc., but rather how they are referenced from within a study protocol or deployment.

In addition, `String` manipulation functions are available to convert type names of protocol domain objects within a JSON string to 'unknown' type names. This supports testing deserialization of domain objects unknown at runtime, e.g., as defined in an application-specific client. See [the section on serialization](#serialization) for more details.

## Usage

This is a [multiplatform Kotlin library](https://kotlinlang.org/docs/reference/multiplatform.html) which targets both the **Java Runtime Environment (JRE)** and **JavaScript (JS)**.
Since this project _does not contain any infrastructure_, you need to include dependencies to the subsystems you want to implement infrastructure for and implement all application services, e.g. as a web service. We recommend reading the Kotlin documentation to see how to consume multiplatform libraries.

As this project progresses, we intend to include [native targets](https://kotlinlang.org/docs/reference/native-overview.html) as well, starting with iOS.

### Example

The following shows how the subystems intereact to create a study protocol, instantiate it as a study, and deploy it to a client.

**carp.protocols**: Example study protocol definition to collect GPS and stepcount on a smartphone which can be serialized to JSON:

```kotlin
// Create a new study protocol.
val owner = ProtocolOwner()
val protocol = StudyProtocol( owner, "Track patient movement" )

// Define which devices are used for data collection.
val phone = Smartphone( "Patient's phone" )
{
    // Configure device-specific options, e.g., frequency to collect data at.
    samplingConfiguration {
        geolocation { interval = TimeSpan.fromMinutes( 15.0 ) }
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

**carp.studies**: Example creation of a study based on a study protocol, and adding and deploying a single participant:

```kotlin
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

**carp.deployment**: Most calls to this subsystem are abstracted away by the 'studies' and 'client' subsystems, so you wouldn't call its endpoints directly. Example code which is called when a study is created and accessed by a client:

```kotlin
val deploymentService: DeploymentService = createDeploymentEndpoint()
val trackPatientStudy: StudyProtocol = createExampleProtocol()
val patientPhone: Smartphone = trackPatientStudy.masterDevices.first() as Smartphone // "Patient's phone"

// This is called by `StudyService` when deploying a participant group.
var status: StudyDeploymentStatus = deploymentService.createStudyDeployment( trackPatientStudy.getSnapshot() )
val studyDeploymentId = status.studyDeploymentId

// What comes after is similar to what is called by the client in `carp.client`:
// - Register the device to be deployed.
val registration = patientPhone.createRegistration()
status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

// - Retrieve information on what to run and indicate the device is ready to collect the requested data.
val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
{
    val deploymentInformation: MasterDeviceDeployment =
        deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
    val deploymentDate: DateTime = deploymentInformation.lastUpdateDate // To verify correct deployment.
    deploymentService.deploymentSuccessful( studyDeploymentId, patientPhone.roleName, deploymentDate )
}

// Now that all devices have been registered and deployed, the deployment is ready.
status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
val isReady = status is StudyDeploymentStatus.DeploymentReady // True.
```

**carp.client**: Example initialization of a smartphone client for the participant that got invited to the study in the 'studies' code sample above:

```kotlin
val deploymentService = createDeploymentEndpoint()

// Retrieve invitation to participate in the study using a specific device.
val account: Account = getLoggedInUser()
val invitation: ActiveParticipationInvitation =
	deploymentService.getActiveParticipationInvitations( account.id ).first()
val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
val deviceToUse: String = invitation.devices.first().deviceRoleName // This matches "Patient's phone".

// Create a study runtime for the study.
val clientRepository = createRepository()
val client = SmartphoneClient( clientRepository, deploymentService )
client.configure {
    // Device-specific registration options can be accessed from here.
    // Depending on the device type, different options are available.
    // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
    deviceId = "xxxxxxxxx"
}
val runtime: StudyRuntimeStatus = client.addStudy( studyDeploymentId, deviceToUse )
var isDeployed = runtime.isDeployed // True, because there are no dependent devices.

// Suppose a deployment also depends on a "Clinician's phone" to be registered; deployment cannot complete yet.
// After the clinician's phone has been registered, attempt deployment again.
isDeployed = client.tryDeployment( runtime.id ) // True once dependent clients have been registered.
```

## Building the project

In case you want to contribute, please follow our [contribution guidelines](https://github.com/cph-cachet/carp.core-kotlin/blob/develop/CONTRIBUTING.md).

We recommend using IntelliJ IDEA 2020, as this is the development environment we use and is therefore fully tested.

- Open the project folder in IntelliJ 2020.
- Install the Kotlin plugin (1.3.72-release-IJ2020.1-5) for IntelliJ IDEA: `Tools->Kotlin->Configure Kotlin Plugin Updates`
- To build/test/publish, click "Edit Configurations" to add configurations for [the included Gradle tasks](#gradle-tasks), or run them from the Gradle tool window.

### Gradle tasks

For `carp.core-kotlin`:
- **build**: Builds the full project, for both runtimes, including running unit tests and code analysis.
- **cleanAllTests jvmTest**: Test the full project using JUnit5. `cleanAllTests` is optional, but ensures that test results always show up in IntelliJ; when tasks haven't changed it otherwise lists "Test events were not received".
- **jsTest**: Test the full project using Mocha. Test results only show up in the build output and not in IntelliJ.
- **verifyTsDeclarations**: Verify whether the TypeScript declarations of all modules, defined in `typescript-declarations`, match the compiled JS sources and work at runtime.
- **detekt**: Run code analysis, ignoring failures. Output will still be successful in case code smells are detected. Apply `--rerun-tasks` to always see output, even when ran before.
- **detektPasses**:: Run code analysis, but do not ignore failures. Output will list failure in case code smells are detected.
- **publishSigned**: Publish all projects to Maven using the version number specified in `ext.globalVersion`. This includes documentation, sources, and signing. For this to work you need to configure a `publish.properties` file with a signing signature and repository user in the project root folder. See main `build.gradle` for details.
- **publishSnapshot**: Publish a snapshot build for all projects to Maven, substituting the suffix of the version specified in `ext.globalVersion` with `-SNAPSHOT`.
