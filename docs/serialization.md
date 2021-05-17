# Serialization in CARP

This library supports serialization of CARP domain objects, for both the JVM and JavaScript runtime.
This is achieved by relying on the [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization) library and compiler plugin.
Classes are made serializable by applying a `@Serializable` annotation to the class definition.
Custom serializers for classes and properties can be specified through an optional `KClass` parameter of `@Serializable` which specifies the custom `KSerializer<T>` to use, e.g., `@Serializable( CustomSerializer::class )`.

Most of this is self-explanatory when looking at the codebase. [`StudyProtocolSnapshot`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/application/StudyProtocolSnapshot.kt) is a good starting point to see how composite objects with complicated hierarchies of inheriting objects, including collections, can be serialized.
But, what follows are pointers on how to use this library specific to CARP, which might be useful **in case you need to understand the codebase, extend on base types, or introduce new ones**.

## UnknownPolymorphicSerializer: (De)serializing unknown types

A core design principle of our architecture is to be extensible.
Many of the (domain model) classes contained in this library can be—and are meant to be—extended by external users.
The question then arises how we should 'load' such custom classes when they are sent to our infrastructure which is unaware about them.

To enable this, we rely on **a custom serializer for each of the base classes which are designed for extension**.
When encountering unknown types, these serializers extract the _base properties_ they do know about, and 'wrap' the unknown type in a custom type definition deriving from the same base class, thereby providing transparent access to base properties without having to know the concrete type (the type does not need to be 'loaded' at runtime).
When serializing unknown types, the original serialized form is output again.
Thus, the **'wrapper' is only ever present while the custom domain objects are held in memory on a runtime which does not have the concrete type available**.
We envision this will greatly facilitate dealing with (or simply ignoring) such objects, making the codebase more stable and maintainable.

Serializing unknown types is currently only supported when using Json.
Other formats, such as ProtoBuf or CBOR, will throw a `SerializationException` when trying to serialize types which are not registered for polymorphic serialization.

To facilitate the creation of these custom serializers, the abstract base class `UnknownPolymorphicSerializer<P: Any, W: P>` can be used (suggestions for a better name are still welcome).
This is a _"serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W]."_
A helper function `createUnknownPolymorphicSerializer` simplifies creating these concrete classes by only having to pass a method which constructs the custom wrapper based on incoming JSON data.
For example, the following code creates an `UnknownPolymorphicSerializer` for an abstract `TaskDescriptor` class.

```
object TaskDescriptorSerializer : KSerializer<TaskDescriptor>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) } )
```

The custom wrapper needs to:

 1. Extend from the base class. E.g., `TaskDescriptor`.
 2. Implement `UnknownPolymorphicWrapper`, which simply provides access to the class name and original JSON source.
 3. Apply `@Serializable` to use the custom serializer. E.g, `@Serializable( TaskDescriptorSerializer::class )`.

The only real work lies in extracting the base properties using traditional JSON parsing.
This can easily be done using a serializer of an intermediate concrete type, as exemplified in [`CustomTaskDescriptor`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/infrastructure/serialization/UnknownTaskSerializers.kt).

The custom serializer should be configured as the `default` serializer for the expected base type in the `SerializersModule`,
and the wrapper should be registered as a subclass.
For example, `TaskDescriptorSerializer` is the `UnknownPolymorphicSerializer` for the `Taskdescriptor` base class,
which is registered [in the common subsystem `SerializersModule`](../carp.common/src/commonMain/kotlin/dk/cachet/carp/common/infrastructure/serialization/Serialization.kt):
```
polymorphic( TaskDescriptor::class )
{
    subclass( ConcurrentTask::class )
    ...
    subclass( CustomTaskDescriptor::class )
    default { TaskDescriptorSerializer }
}
```

## JavaScript compiler plugin limitations

The [serializer for classes with named companion objects cannot be found](https://github.com/Kotlin/kotlinx.serialization/issues/226). **We recommend not to name companion objects.** The compiler plugin adds a `serializer()` method to the companion object, and the JavaScript runtime relies on the default name of companion objects (`Companion`). This is [something I fixed for the JVM runtime](https://github.com/Kotlin/kotlinx.serialization/pull/130), but seems harder to do for the JS runtime.