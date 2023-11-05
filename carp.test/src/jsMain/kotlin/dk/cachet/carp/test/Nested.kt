package dk.cachet.carp.test


// The Mocha test runtime does not rely on a 'Nested' attribute in order to run tests of nested classes.
@Target( AnnotationTarget.CLASS )
actual annotation class Nested
