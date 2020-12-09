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
    fun adding_study_protocol_and_retrieving_it_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )

        repo.add( protocol, "Initial" )
        val retrieved = repo.getBy( owner, "Study" )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun getBy_for_a_specific_protocol_version_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol1 = StudyProtocol( owner, "Study" )
        repo.add( protocol1, "Initial" )

        val protocol2 = StudyProtocol( owner, "Study" )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor( "Device" ) )
        repo.addVersion( protocol2, "Version 2" )

        val protocol3 = StudyProtocol( owner, "Study" )
        protocol3.addMasterDevice( StubMasterDeviceDescriptor( "Other device" ) )
        repo.addVersion( protocol3, "Version 3" )

        val retrievedProtocol = repo.getBy( owner, "Study", "Version 2" )
        assertEquals( protocol2.getSnapshot(), retrievedProtocol.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun cant_add_study_protocol_which_already_exists() = runSuspendTest {
        val repo = createRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, "Initial" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.add( protocol, "Version tag is not checked." )
        }
    }

    @Test
    fun cant_getBy_owner_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( ProtocolOwner(), "Study" )
        }
    }

    @Test
    fun cant_getBy_study_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( owner, "Non-existing study" )
        }
    }

    @Test
    fun cant_getBy_version_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )
        repo.addVersion( protocol, "Update" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( owner, "Study", "Non-existing version" )
        }
    }

    @Test
    fun addVersion_to_study_protocol_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.addVersion( protocol, "New version" )
        val retrieved = repo.getBy( owner, "Study" )
        assertEquals( protocol.getSnapshot(), retrieved.getSnapshot() ) // StudyProtocol does not implement equals, but snapshot does.
    }

    @Test
    fun cant_update_study_protocol_which_does_not_yet_exist() = runSuspendTest {
        val repo = createRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        assertFailsWith<IllegalArgumentException>
        {
            repo.addVersion( protocol, "New version" )
        }
    }

    @Test
    fun getAllFor_owner_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol1 = StudyProtocol( owner, "Study 1" )
        val protocol2 = StudyProtocol( owner, "Study 2" )
        repo.add( protocol1, "Initial" )
        repo.add( protocol2, "Initial" )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.addVersion( protocol2, "Latest should be retrieved" )

        // StudyProtocol does not implement equals, but snapshot does, so compare snapshots.
        val protocols: List<StudyProtocolSnapshot> = repo.getAllFor( owner ).map { it.getSnapshot() }.toList()
        val expected = listOf( protocol1.getSnapshot(), protocol2.getSnapshot() )
        assertEquals( expected.count(), protocols.intersect( expected ).count() )
    }

    @Test
    fun cant_getAllFor_owner_which_does_not_exist() = runSuspendTest {
        val repo = createRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.getAllFor( ProtocolOwner() )
        }
    }

    @Test
    fun getVersionHistoryFor_succeeds() = runSuspendTest {
        val repo = createRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )
        repo.addVersion( protocol, "Version 1" )
        repo.addVersion( protocol, "Version 2" )

        val history: List<ProtocolVersion> = repo.getVersionHistoryFor( owner, "Study" )
        val historyVersions: List<String> = history.map { it.tag }.toList()
        val expectedVersions: List<String> = listOf( "Initial", "Version 1", "Version 2" )
        assertEquals( expectedVersions.count(), historyVersions.intersect( expectedVersions ).count() )
    }
}
