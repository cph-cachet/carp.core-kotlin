# Development checklists

Each subsection in this document contains a common workflow for developers of the CARP Core Framework.
But first, let's introduce some overarching basic concepts.

This codebase follows [domain-driven design (DDD)](https://en.wikipedia.org/wiki/Domain-driven_design).
In the remainder of this document, familiarity with DDD terminology (such as "aggregate root" and "integration event") is assumed. 

Some domain models extend from [`AggregateRoot`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/domain/AggregateRoot.kt). They (1) implement [the snapshot pattern](https://howtodoinjava.com/design-patterns/behavioral/memento-design-pattern/), and (2) keep track of domain events.

1. By calling `getSnapshot()` a [`Snapshot`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/domain/Snapshot.kt) of the object at that specific time can be retrieved.
Snapshots should be immutable and serializable, which allows them to be used as data transfer objects (DTOs) in application services and to be persisted in a database.
2. Domain events keep track of changes to the aggregate root. 
Currently, they are stored as a simple list of `events`. 
These can be handled by calling `consumeEvents`, after which they are erased.
_Note_: depending on the chosen persistence model in an infrastructure, events may go unused, e.g., in case a document-oriented database is used and any update is written by updating the full document.

APIs in subsystems are exposed through interfaces extending from [`ApplicationService`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/services/ApplicationService.kt).
They have an associated [`IntegrationEvent`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/services/IntegrationEvent.kt) which is used to support eventual consistency across application services.
Integration events should be immutable and serializable, which allows them to be sent over implementations of [`EventBus`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/services/EventBus.kt).

## Update existing aggregate root

1. Add or update a domain model field to capture new state (e.g., `Study.name`).
2. If the field is mutable, broadcast a matching [`DomainEvent`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/domain/DomainEvent.kt) on changes:
   - Add/update a corresponding `Event` (e.g., `Study.Event.InternalDescriptionChanged` contains `name`).
   - Call `event()` with the corresponding `Event` whenever the matching field changes.
3. Include/edit the corresponding field in the `Snapshot` for this class (e.g., `StudySnapshot.name`).
Make sure the field, and all its containing fields, are immutable.
4. Update the `fromXYZ` function in the snapshot to correctly initialize the domain model (e.g., `StudySnapshot.fromStudy()`).
5. Update the `fromSnapshot` function in the domain model to correctly initialize the snapshot (e.g., `Study.fromSnapshot()`).
6. Update the `..._fromSnapshot_obtained_by_getSnapshot_is_the_same` unit test to verify whether all fields from/to domain model and snapshot are copied over correctly.
7. Identify affected application services: any service which takes the modified snapshot as input or output, either directly or as a nested object, is affected (e.g., `StudyProtocolSnapshot`).
8. If the snapshot is used in application services, update the corresponding TypeScript declaration in `typescript-declarations`.
You may also need to update serialized JSON in unit tests to make tests pass.
9. For each of the affected application services, [upgrade the application service API version](#upgrade-application-service-api-version).

## Upgrade application service API version

To allow implementing infrastructures to be backwards compatible for callers expecting an older API, CARP uses _versioned_ APIs.
Each application service interface should contain a static `API_VERSION` field with a _major_ and _minor_ version.
Only minor version are backwards compatible.

When an incoming request contains the same major version as the backend but a different minor version, the infrastructure can _migrate_ incoming requests and responses.
This migration is straightforward to wire into the infrastructure when using [CARP's recommended infrastructure helpers](../README.md#infrastructure-helpers),
and relies on the migration being implemented in CARP Core as a JSON transformation between old and new versions of `IntegrationEvent`, RPC `RequestObject`'s, and their return types. 

1. Increment the minor version in the `ApplicationService` interface by 1 (e.g., `StudyService.API_VERSION.minor`).
2. Extend from `ApiMigrator`, specifying the old and new minor version in the constructor, and override the missing methods to implement the migration.
Don't forget to migrate nested objects (e.g., `StudyDetails.protocolSnapshot` needs to be migrated if `StudyProtocolSnapshot` changes).
3. Add the migration to the list of migrations in the corresponding `ApplicationServiceApiMigrator` constructor (e.g., `StudyServiceApiMigrator`).
4. Copy the output of the `OutputTestRequests` unit test (`build\test-requests`) of the relevant application service to the corresponding test resources as indicated by the failing `versioned_test_requests_for_current_api_version_available` test.
These generated test resources will be used to verify the migrations (step 3) of any subsequent API version upgrades.
5. Update the affected JSON schemas. At a minimum you will need to change the request object's API version (e.g., `StudyServiceRequest.json`).
These schemas are useful for non-Kotlin clients.
If you forget to do this, `JsonSchemasTest` will fail; this test validates generated JSON output, known to be correct, using the schemas.

## Add a new sensor data type

Keep in mind that CARP [data types should be device-agnostic](carp-common.md#sensor-data-types).
Therefore, don't include device-specific information in new [`SensorData`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/application/data/Data.kt) types.

Failing tests and static code analysis (`detektPasses`) will guide you to make sure newly introduced data types are immutable, serializable, registered, and tested.
But, below are the necessary steps to follow:

1. Add data type meta data to `CarpDataTypes`, following the template of existing data types.
2. Add a new class extending from `SensorData` (or object in case the measure contains no data) to the `dk.cachet.carp.common.application.data` namespace in the `carp.common` subsystem (e.g., `AngularVelocity`). 
   - Make sure to name the class after the collected _data_, and not the _sensor_ which collects the data (e.g., `AngularVelocity` vs `Gyro`).
   - Add clear KDoc documentation on how the data should be interpreted.
     For data fields, use/document SI units wherever appropriate, and choose sufficiently precise units so that no data is lost when unit conversions from raw data to the `Data` are done.
   - Ensure that the class is immutable (contains no mutable fields) and is a `data class` or `object`.
   - Make the class serializable using `kotlinx.serialization`.
     For basic types, this should be as easy as marking it as `@Serializable`.
   - Specify `@SerialName` using the data type specified in step 1.
3. Register the new data type for polymorphic serialization in [`COMMON_SERIAL_MODULE`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/infrastructure/serialization/Serialization.kt).
4. Add a test instance of the new `Data` type to [`commonInstances`](../carp.common/src/commonTest/kotlin/dk/cachet/carp/common/application/TestInstances.kt).
5. Include the data type [in the README](../docs/carp-common.md#data-types).
6. Update JSON schemas for the new type:
   - Add a new schema in [`rpc/schemas/common/data`](../rpc/schemas/common/data) corresponding to the class name (e.g., `AngularVelocity.json`).
   - Add the new schema as a subtype in [`Data.json`](../rpc/schemas/common/data/Data.json). The existing examples should guide you, but double-check you specified the right data type constant.
   - _Warning_: the presence or validity of this schema [is not yet tested](https://github.com/imotions/carp.core-kotlin/issues/404).
     It is recommended to serialize an instance of the new data type (e.g., by running a slightly modified polymorphic serialization test in `DataSerializationTest`) and [validate the output manually for now](https://www.jsonschemavalidator.net/).
