package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.test.runSuspendTest
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
        val owner = ProtocolOwner()
        val name = "Study"
        val protocol = StudyProtocol( owner, name )

        repo.add( protocol, "Initial" )
        val retrieved = repo.getBy( owner, name )

        assertNotNull( retrieved )
        assertNotSame( protocol, retrieved )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun add_fails_for_existing_protocol() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, "Initial" )

        assertFailsWith<IllegalArgumentException> { repo.add( protocol, "Version doesn't determine identity." ) }
    }

    @Test
    fun addVersion_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val name = "Study"
        val protocol = StudyProtocol( owner, name )
        repo.add( protocol, "Initial" )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.addVersion( protocol, "New version" )

        val retrieved = repo.getBy( owner, name, "New version" )
        assertNotNull( retrieved )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun addVersion_fails_for_protocol_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        assertFailsWith<IllegalArgumentException> { repo.addVersion( protocol, "New version" ) }
    }

    @Test
    fun addVersion_fails_for_version_which_is_already_in_use() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, "Version" )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        assertFailsWith<IllegalArgumentException> { repo.addVersion( protocol, "Version" ) }
    }

    @Test
    fun getBy_gets_latest_when_version_not_specified() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val name = "Study"

        val protocol = StudyProtocol( owner, name )
        repo.add( protocol, "Initial" )

        val protocol2 = StudyProtocol( owner, name )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.addVersion( protocol2, "New version" )

        val retrieved = repo.getBy( owner, name )
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
        repo.add( protocol1, "Initial" )

        val protocol2 = StudyProtocol( owner, name )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor( "Device" ) )
        repo.addVersion( protocol2, "Version 2" )

        val protocol3 = StudyProtocol( owner, name )
        protocol3.addMasterDevice( StubMasterDeviceDescriptor( "Other device" ) )
        repo.addVersion( protocol3, "Version 3" )

        val retrieved = repo.getBy( owner, name, "Version 2" )
        assertNotNull ( retrieved )
        assertNotSame( protocol2, retrieved )
        assertEquals( protocol2.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun getBy_returns_null_for_owner_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        assertNull( repo.getBy( ProtocolOwner(), "Study" ) )
    }

    @Test
    fun getBy_returns_null_for_name_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )

        assertNull( repo.getBy( owner, "Non-existing study" ) )
    }

    @Test
    fun getBy_returns_null_for_version_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )
        repo.addVersion( protocol, "Update" )

        assertNull( repo.getBy( owner, "Study", "Non-existing version" ) )
    }

    @Test
    fun getAllFor_owner_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()

        val protocol1 = StudyProtocol( owner, "Study 1" )
        repo.add( protocol1, "Initial" )

        val protocol2 = StudyProtocol( owner, "Study 2" )
        repo.add( protocol2, "Initial" )
        val protocol2Latest = StudyProtocol( owner, "Study 2" )
        protocol2Latest.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.addVersion( protocol2Latest, "Latest should be retrieved" )

        val protocols: Sequence<StudyProtocol> = repo.getAllFor( owner )

        // StudyProtocol does not implement equals, but snapshot does, so compare snapshots.
        val snapshots: Set<StudyProtocolSnapshot> = protocols.map { it.getSnapshot() }.toSet()
        val expected = setOf( protocol1.getSnapshot(), protocol2Latest.getSnapshot() )
        assertEquals( expected, snapshots )
    }

    @Test
    fun getAllFor_is_empty_when_no_protocols_are_stored_for_owner() = runSuspendTest {
        val repo = createRepository()

        val protocols = repo.getAllFor( ProtocolOwner() )
        assertTrue( protocols.count() == 0 )
    }

    @Test
    fun getVersionHistoryFor_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val name = "Study"
        val protocol = StudyProtocol( owner, name )
        repo.add( protocol, "Initial" )
        repo.addVersion( protocol, "Version 1" )
        repo.addVersion( protocol, "Version 2" )

        val history: List<ProtocolVersion> = repo.getVersionHistoryFor( owner, name )

        val historyVersions: Set<String> = history.map { it.tag }.toSet()
        val expectedVersions = setOf( "Initial", "Version 1", "Version 2" )
        assertEquals( expectedVersions, historyVersions )
    }

    @Test
    fun getVersionHistoryFor_fails_when_protocol_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        val unknownOwner = ProtocolOwner()
        assertFailsWith<IllegalArgumentException> { repo.getVersionHistoryFor( unknownOwner, "Unknown" ) }
    }
}
