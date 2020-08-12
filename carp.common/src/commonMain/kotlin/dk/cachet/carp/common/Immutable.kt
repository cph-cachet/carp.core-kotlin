package dk.cachet.carp.common


/**
 * Specifies that the class this annotation is applied to, and all extending classes, should be immutable.
 * Immutable types may not contain mutable properties or properties of mutable types.
 */
@Target( AnnotationTarget.CLASS )
annotation class Immutable
