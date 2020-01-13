# Contributing to CARP

Thank you for considering contributing to CARP! We use [the built-in issue tracker of GitHub](https://github.com/cph-cachet/carp.core-kotlin/issues) for feature requests, bug reports, and general questions. Please label your issue correspondingly; the label descriptions provide guidance as to which labels to apply.

## Release workflow

There are two permanent branches in this repository:
- **develop**: contains the latest code which compiles and passes all tests. Pushing to this branch results in ['SNAPSHOT' publications to Sonatype Nexus Repository](https://oss.sonatype.org/content/repositories/snapshots/dk/cachet/carp/). You can see whether the last snapshot was published successfully on the corresponding badge in [the main README](https://github.com/cph-cachet/carp.core-kotlin#domain-model-and-application-service-definitions-for-all-carp-subsystems).
- **master**: contains the last release, as listed in [releases](https://github.com/cph-cachet/carp.core-kotlin/releases). Pushing to this branch results in [versioned Maven releases](https://mvnrepository.com/artifact/dk.cachet.carp). Only maintainers of this repository can complete pull requests to _master_, and thus publish releases to Maven. As a maintainer, make sure you [apply proper semantic versioning](https://semver.org/) when incrementing version numbers.

## Submitting changes

We welcome contributions on the **develop** branch. Pull requests on this branch will trigger a full build and test run which needs to pass in order for the pull request to be considered. In addition, a check shows whether your changes comply with our [coding conventions](#coding-conventions). We encourage you to resolve any code smells identified by [detekt][detekt] (visible in the output), which is ran by default as part of the build. You can also run detekt separately through `gradle detekt`.

Whenever adding new functionality or addressing bug fixes, try to include a unit test covering it using `kotlin.test` in `commonTest`. Unit test namespaces and classes reflect those in the main source code, so it should be self-evident where to find them/place them. Lastly, `carp.test` contains functionality to help write unit tests which might be useful and is imported in all test projects.

## Coding conventions
<a name="coding-conventions"><a/>

This project follows the [standard Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html), except for spacing of parentheses and braces:

- Spaces need to be added in all parentheses, except for those of higher-order functions.
- Curly braces of _multi-line_ blocks need to be placed on separate lines (with the exception of trailing lambda arguments), aligned with the start of the definition the block is associated with (e.g., class, function, object literal, if, or return).

```
// Correct spacing of parentheses and curly braces.
if ( true )
{
    val answer = 42
}

// Parameter parentheses of higher-order functions do not take spaces.
val higherOrder: (Int, Int) -> Int = { a: Int, _: Int -> a }

// Curly braces of trailing lambda argument defined on multiple lines are not placed on separate lines.
fun test( list: List<Int> )
{
    list.forEach {
        val answer = it }
}
```

These guidelines are checked by [detekt][detekt] when building the project locally, and when submitting a pull request to the develop branch. For more examples, you can [look at the unit tests of the custom style rules applied by detekt](https://github.com/cph-cachet/carp.core-kotlin/tree/develop/carp.detekt/src/test/kotlin/dk/cachet/carp/detekt/extensions/rules).

We decided on these guidelines since research has shown that code which resembles natural language more (using spacing), leads to code which is easier to read (i.e., [faster](http://www.cs.loyola.edu/~binkley/papers/icpc09-clouds.pdf) and [requiring less eye fixations](https://ieeexplore.ieee.org/document/5521745?tp=&arnumber=5521745&url=http:%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D5521745)).

 [detekt]: https://github.com/arturbosch/detekt
