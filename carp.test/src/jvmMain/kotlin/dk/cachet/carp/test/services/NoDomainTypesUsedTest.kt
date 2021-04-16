package dk.cachet.carp.test.services

import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaType


private val disallowedMatch = Regex( """dk\.cachet\.carp\..+\.domain\..+""" )

/**
 * Verifies whether no 'domain' types (located in any `dk.cachet.carp.*.domain` namespace)
 * are used in the public interface of the passed types.
 *
 * Application services should only expose types in the `application` namespace
 * which contains all types making up the public interface.
 */
fun verifyNoDomainTypesUsedIn( types: Collection<Class<*>> ) =
    types.map { Reflection.createKotlinClass( it ) }.forEach { verifyNoDomainTypesUsedIn( it ) }

private fun verifyNoDomainTypesUsedIn( klass: KClass<*> )
{
    // Find all used types on the public interface of T.
    val publicMembers = klass.members.filter { it.visibility == KVisibility.PUBLIC }
    val allUsedTypes = publicMembers
        .flatMap { c -> c.parameters.map { it.type }.plus( c.returnType ) }
        .distinct()

    // Verify for each whether the type is defined in a disallowed namespace.
    val typeNames = allUsedTypes.map { it.javaType.typeName }
    for ( type in typeNames )
    {
        assert( !disallowedMatch.matches( type ) )
            { "`$type` is in a disallowed namespace for types exposed on the public interface of `${klass.simpleName}`." }
    }
}
