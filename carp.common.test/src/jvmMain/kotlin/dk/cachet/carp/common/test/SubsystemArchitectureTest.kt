package dk.cachet.carp.common.test

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.common.test.infrastructure.SnapshotTest
import org.reflections.Reflections
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.test.*


@Suppress( "FunctionName", "UnnecessaryAbstractClass" )
abstract class SubsystemArchitectureTest( private val namespace: String )
{
    private val reflections = Reflections( namespace )

    /**
     * Verifies whether no 'domain' types (located in any `dk.cachet.carp.*.domain` namespace)
     * are used in the public interface of application services.
     *
     * Application services should only expose types in the `application` namespace
     * which contains all types making up the public interface.
     */
    @Test
    fun no_domain_objects_used_in_application_service_interfaces()
    {
        // Find all application services and integration events in this subsystem.
        val typesToCheck = reflections.getSubTypesOf( ApplicationService::class.java )
            .plus( reflections.getSubTypesOf( IntegrationEvent::class.java ) )

        // Check for each whether any of its members or containing members is in a wrong namespace.
        val verifiedTypes: MutableSet<KType> = mutableSetOf()
        typesToCheck.map { Reflection.createKotlinClass( it ) }.forEach { klass: KClass<*> ->
            val publicMembers = klass.members.filter { it.visibility == KVisibility.PUBLIC }
            val allUsedTypes = publicMembers
                .flatMap { c -> c.parameters.map { it.type }.plus( c.returnType ) }
                .distinct()

            allUsedTypes.forEach { verifyNoDomainTypesUsedIn( it, klass, verifiedTypes ) }
        }
    }

    private val disallowedMatch = Regex( """dk\.cachet\.carp\..+\.domain\..+""" )

    private fun verifyNoDomainTypesUsedIn( type: KType, usedInKlass: KClass<*>, verifiedTypes: MutableSet<KType> )
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
        { "`$type` is in a disallowed namespace for types exposed on the public interface of `${usedInKlass.simpleName}`." }
        verifiedTypes.add( type )

        // Recursive verification of all public properties.
        type.jvmErasure
            .memberProperties.filter { it.visibility == KVisibility.PUBLIC }
            .map { it.returnType }
            .forEach { verifyNoDomainTypesUsedIn( it, usedInKlass, verifiedTypes ) }
    }

    @Test
    fun snapshot_test_provided_for_each_snapshot()
    {
        // Find all snapshot classes.
        val snapshotClasses = reflections.getSubTypesOf( Snapshot::class.java ).map { it.name }

        // Find all tested snapshot classes.
        val snapshotTests = reflections.getSubTypesOf( SnapshotTest::class.java )
        val testedSnapshotClasses = snapshotTests.map { snapshotTest ->
            val changeSnapshotMethod = SnapshotTest<*, *>::changeSnapshotVersion.name
            snapshotTest.methods
                // Filter out abstract overload to find concrete snapshot return type.
                .first { it.name == changeSnapshotMethod && it.returnType.name != Snapshot::class.qualifiedName }
                .returnType.name
        }

        assertEquals( snapshotClasses.toSet(), testedSnapshotClasses.toSet() )
    }
}
