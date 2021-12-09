package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.test.runSuspendTest
import kotlinx.datetime.Instant
import kotlin.test.*


/**
 * Tests for implementations of [StudyProtocolRepository].
 */
interface StudyProtocolRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): StudyProtocolRepository


    @Test
    fun add_protocol_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Name" )

        repo.add( protocol, ProtocolVersion( "Initial" ) )
        val retrieved = repo.getBy( protocol.id )

        assertNotNull( retrieved )
        assertNotSame( protocol, retrieved )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun add_fails_for_existing_protocol() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, ProtocolVersion( "Initial" ) )

        val differentVersion = ProtocolVersion( "Version doesn't determine identity." )
        assertFailsWith<IllegalArgumentException> { repo.add( protocol, differentVersion ) }
    }

    @Test
    fun addVersion_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Name" )
        repo.add( protocol, ProtocolVersion( "Initial" ) )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        val newVersion = ProtocolVersion( "New version" )
        repo.addVersion( protocol, newVersion )

        val retrieved = repo.getBy( protocol.id, newVersion.tag )
        assertNotNull( retrieved )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun addVersion_fails_for_protocol_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        assertFailsWith<IllegalArgumentException> { repo.addVersion( protocol, ProtocolVersion( "New version" ) ) }
    }

    @Test
    fun addVersion_fails_for_version_which_is_already_in_use() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        val version = ProtocolVersion( "Version" )
        repo.add( protocol, version )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        assertFailsWith<IllegalArgumentException> { repo.addVersion( protocol, version ) }
    }

    @Test
    fun replace_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        val version = ProtocolVersion( "Version" )
        repo.add( protocol, version )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.replace( protocol, version )

        val retrieved = repo.getBy( protocol.id, "Version" )
        assertNotNull( retrieved )
        assertNotSame( protocol, retrieved )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() )
    }

    @Test
    fun replace_fails_for_protocol_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study")
        assertFailsWith<IllegalArgumentException> { repo.replace( protocol, ProtocolVersion( "Version" )) }
    }

    @Test
    fun getBy_gets_latest_when_version_not_specified() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val name = "Study"

        val protocol = StudyProtocol( owner, name )
        val initialVersion = ProtocolVersion( "Initial", Instant.fromEpochMilliseconds( 0 ) )
        repo.add( protocol, initialVersion )

        val protocol2 = StudyProtocol.fromSnapshot( protocol.getSnapshot() )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor() )
        val newVersion = ProtocolVersion( "New version", Instant.fromEpochMilliseconds( 1 ) )
        repo.addVersion( protocol2, newVersion )

        val retrieved = repo.getBy( protocol2.id )
        assertNotNull( retrieved )
        assertNotSame( protocol2, retrieved )
        assertEquals( protocol2.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun getBy_for_a_specific_version_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val name = "Study"

        val protocol1 = StudyProtocol( owner, name )
        val protocol1Snapshot = protocol1.getSnapshot()
        repo.add( protocol1, ProtocolVersion( "Initial" ) )

        val protocol2 = StudyProtocol.fromSnapshot( protocol1Snapshot )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor( "Device" ) )
        val version2 = ProtocolVersion( "Version 2" )
        repo.addVersion( protocol2, version2 )

        val protocol3 = StudyProtocol.fromSnapshot( protocol1Snapshot )
        protocol3.addMasterDevice( StubMasterDeviceDescriptor( "Other device" ) )
        repo.addVersion( protocol3, ProtocolVersion( "Version 3" ) )

        val retrieved = repo.getBy( protocol2.id, version2.tag )
        assertNotNull ( retrieved )
        assertNotSame( protocol2, retrieved )
        assertEquals( protocol2.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun getBy_returns_null_for_id_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getBy( unknownId, "Study" ) )
    }

    @Test
    fun getBy_returns_null_for_version_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, ProtocolVersion( "Initial" ) )
        repo.addVersion( protocol, ProtocolVersion( "Update" ) )

        assertNull( repo.getBy( protocol.id, "Non-existing version" ) )
    }

    @Test
    fun getAllFor_owner_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()

        val protocol1 = StudyProtocol( owner, "Study 1" )
        repo.add( protocol1, ProtocolVersion( "Initial" ) )

        val protocol2 = StudyProtocol( owner, "Study 2" )
        repo.add( protocol2, ProtocolVersion( "Initial", Instant.fromEpochMilliseconds( 0 ) ) )
        val protocol2Latest = StudyProtocol.fromSnapshot( protocol2.getSnapshot() )
        protocol2Latest.addMasterDevice( StubMasterDeviceDescriptor() )
        val later = Instant.fromEpochMilliseconds( 1 )
        repo.addVersion( protocol2Latest, ProtocolVersion( "Latest should be retrieved", later ) )

        val protocols: Sequence<StudyProtocol> = repo.getAllFor( owner.id )

        // StudyProtocol does not implement equals, but snapshot does, so compare snapshots.
        val snapshots: Set<StudyProtocolSnapshot> = protocols.map { it.getSnapshot() }.toSet()
        val expected = setOf( protocol1.getSnapshot(), protocol2Latest.getSnapshot() )
        assertEquals( expected, snapshots )
    }

    @Test
    fun getAllFor_is_empty_when_no_protocols_are_stored_for_owner() = runSuspendTest {
        val repo = createRepository()

        val unknown = UUID.randomUUID()
        val protocols = repo.getAllFor( unknown )
        assertTrue( protocols.count() == 0 )
    }

    @Test
    fun getVersionHistoryFor_succeeds() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        val initialVersion = ProtocolVersion( "Initial" )
        repo.add( protocol, initialVersion )
        val version1 = ProtocolVersion( "Version 1" )
        repo.addVersion( protocol, version1 )
        val version2 = ProtocolVersion( "Version 2" )
        repo.addVersion( protocol, version2 )

        val history: Set<ProtocolVersion> = repo.getVersionHistoryFor( protocol.id ).toSet()
        val expectedVersions = setOf( initialVersion, version1, version2 )
        assertEquals( expectedVersions, history )
    }

    @Test
    fun getVersionHistoryFor_fails_when_protocol_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { repo.getVersionHistoryFor( unknownId ) }
    }
}
