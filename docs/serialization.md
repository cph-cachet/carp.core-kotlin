# Serialization in CARP

This library supports serialization of CARP domain objects, for both the JVM and JavaScript runtime.
This is achieved by relying on the [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization) library and compiler plugin.
Classes are made serializable by applying a `@Serializable` annotation to the class definition.
Custom serializers for classes and properties can be specified through an optional `KClass` parameter of `@Serializable` which specifies the custom `KSerializer<T>` to use, e.g., `@Serializable( CustomSerializer::class )`.

Most of this is self-explanatory when looking at the codebase. [`StudyProtocolSnapshot`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/StudyProtocolSnapshot.kt) is a good starting point to see how composite objects with complicated hierarchies of inheriting objects, including collections, can be serialized.
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

To facilitate the creation of these custom serializers, the abstract base class `UnknownPolymorphicSerializer<P: Any, W: P>` can be used (suggestions for a better name are still welcome).
This is a _"serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W]."_
A helper function `createUnknownPolymorphicSerializer` simplifies creating these concrete classes by only having to pass a method which constructs the custom wrapper based on incoming JSON data.
For example, the following code creates an `UnknownPolymorphicSerializer` for an abstract `TaskDescriptor` class.

```
object TaskDescriptorSerializer : KSerializer<TaskDescriptor>
    by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomTaskDescriptor( className, json, serializer ) } )
```

The `CustomTaskDescriptor` needs to extend from the base class, and in addition also implement `UnknownPolymorphicWrapper` (simply providing access to class name and original JSON source). The only real work thus lies in extracting the base properties using traditional JSON parsing.
The `kotlinx.serialization` multiplatform JSON parser can be used to this end, as exemplified in [`CustomTaskDescriptor`](../carp.protocols.core/src/commonMain/kotlin/dk/cachet/carp/protocols/domain/tasks/UnknownTaskSerializers.kt).

The custom serializer _should not be specified as the serializer to use on the class it is intended for_. The `UnknownPolymorphicSerializer` relies on the original serializer internaly, so this would create a chicken and egg problem.
Instead, apply the custom serializer wherever the type is used:

```
data class StudyProtocolSnapshot(
    ...
    val tasks: List<@Serializable( TaskDescriptorSerializer::class ) TaskDescriptor>,
    ...
```

## JavaScript compiler plugin limitations

The [serializer for classes with named companion objects cannot be found](https://github.com/Kotlin/kotlinx.serialization/issues/226). **We recommend not to name companion objects.** The compiler plugin adds a `serializer()` method to the companion object, and the JavaScript runtime relies on the default name of companion objects (`Companion`). This is [something I fixed for the JVM runtime](https://github.com/Kotlin/kotlinx.serialization/pull/130), but seems harder to do for the JS runtime.