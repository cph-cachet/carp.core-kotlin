package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.junit.Assert.*


/**
 * Tests for implementations of [StudyProtocolRepository].
 */
interface StudyProtocolRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createStudyProtocolRepository(): StudyProtocolRepository


    @Test
    fun `adding study protocol and retrieving it succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )

        repo.add( protocol, "Initial" )
        val retrieved = repo.getBy( owner, "Study" )
        assertEquals( protocol, retrieved )
    }

    @Test
    fun `getBy for a specific protocol version succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol1 = StudyProtocol( owner, "Study" )
        repo.add( protocol1, "Initial" )

        val protocol2 = StudyProtocol( owner, "Study" )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor( "Device" ) )
        repo.update( protocol2, "Version 2" )

        val protocol3 = StudyProtocol( owner, "Study" )
        protocol3.addMasterDevice( StubMasterDeviceDescriptor( "Other device" ) )
        repo.update( protocol3, "Version 3" )

        val retrievedProtocol = repo.getBy( owner, "Study", "Version 2" )
        assertEquals( protocol2, retrievedProtocol )
    }

    @Test
    fun `can't add study protocol which already exists`()
    {
        val repo = createStudyProtocolRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        repo.add( protocol, "Initial" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.add( protocol, "Version tag is not checked." )
        }
    }

    @Test
    fun `can't getBy owner which does not exist`()
    {
        val repo = createStudyProtocolRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( ProtocolOwner(), "Study" )
        }
    }

    @Test
    fun `can't getBy study which does not exist`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( owner, "Non-existing study" )
        }
    }

    @Test
    fun `can't getBy version which does not exist`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )
        repo.update( protocol, "Update" )

        assertFailsWith<IllegalArgumentException>
        {
            repo.getBy( owner, "Study", "Non-existing version" )
        }
    }

    @Test
    fun `update study protocol succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.update( protocol, "New version" )
        val retrieved = repo.getBy( owner, "Study" )
        assertEquals( protocol, retrieved )
    }

    @Test
    fun `can't update study protocol which does not yet exist`()
    {
        val repo = createStudyProtocolRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        assertFailsWith<IllegalArgumentException>
        {
            repo.update( protocol, "New version" )
        }
    }

    @Test
    fun `getAllFor owner succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol1 = StudyProtocol( owner, "Study 1" )
        val protocol2 = StudyProtocol( owner, "Study 2" )
        repo.add( protocol1, "Initial" )
        repo.add( protocol2, "Initial" )
        protocol2.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.update( protocol2, "Latest should be retrieved" )

        val protocols: List<StudyProtocol> = repo.getAllFor( owner ).toList()
        val expected = listOf( protocol1, protocol2 )
        assertEquals( expected.count(), protocols.intersect( expected ).count() )
    }

    @Test
    fun `can't getAllFor owner which does not exist`()
    {
        val repo = createStudyProtocolRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.getAllFor( ProtocolOwner() )
        }
    }

    @Test
    fun `getVersionHistoryFor succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol, "Initial" )
        repo.update( protocol, "Version 1" )
        repo.update( protocol, "Version 2" )

        val history: List<ProtocolVersion> = repo.getVersionHistoryFor( owner, "Study" )
        val historyVersions: List<String> = history.map { it -> it.tag }.toList()
        val expectedVersions: List<String> = listOf( "Initial", "Version 1", "Version 2" )
        assertEquals( expectedVersions.count(), historyVersions.intersect( expectedVersions ).count() )
    }
}