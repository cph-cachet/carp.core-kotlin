package dk.cachet.carp.test.services

import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure


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

    val verifiedTypes: MutableSet<KType> = mutableSetOf()
    allUsedTypes.forEach { verifyType( it, klass, verifiedTypes ) }
}

private fun verifyType( type: KType, usedInInterface: KClass<*>, verifiedTypes: MutableSet<KType> )
{
    // Early out in case the type has already been verified.
    if ( verifiedTypes.contains( type ) ) return

    val typeName = type.toString()

    // Early out for base types.
    if ( typeName.startsWith( "kotlin" ) || typeName.startsWith( "java" ) )
    {
        verifiedTypes.add( type )
        return
    }

    // Verify whether the type is defined in a disallowed namespace.
    assert( !disallowedMatch.matches( typeName ) )
        { "`$type` is in a disallowed namespace for types exposed on the public interface of `${usedInInterface.simpleName}`." }
    verifiedTypes.add( type )

    // Recursive verification of all public properties.
    type.jvmErasure
        .memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        .map { it.returnType }
        .forEach { verifyType( it, usedInInterface, verifiedTypes ) }
}
