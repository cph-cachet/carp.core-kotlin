package bhrp.studyprotocol.domain

import bhrp.studyprotocol.domain.devices.StubMasterDeviceDescriptor
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


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

        repo.add( protocol )
        val retrieved = repo.findBy( owner, "Study" )
        assertEquals( protocol, retrieved )
    }

    @Test
    fun `can't add study protocol which already exists`()
    {
        val repo = createStudyProtocolRepository()
        val protocol = StudyProtocol( ProtocolOwner(), "Study")
        repo.add( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.add( protocol )
        }
    }

    @Test
    fun `can't findBy owner which does not exist`()
    {
        val repo = createStudyProtocolRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.findBy( ProtocolOwner(), "Study" )
        }
    }

    @Test
    fun `can't findBy study which does not exist`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            repo.findBy( owner, "Non-existing study" )
        }
    }

    @Test
    fun `save study protocol succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study" )
        repo.add( protocol )

        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        repo.save( protocol )
        val retrieved = repo.findBy( owner, "Study" )
        assertEquals( protocol, retrieved )
    }

    @Test
    fun `can't save study protocol which does not yet exist`()
    {
        val repo = createStudyProtocolRepository()

        val protocol = StudyProtocol( ProtocolOwner(), "Study" )
        assertFailsWith<IllegalArgumentException>
        {
            repo.save( protocol )
        }
    }

    @Test
    fun `findAllFor owner succeeds`()
    {
        val repo = createStudyProtocolRepository()
        val owner = ProtocolOwner()
        val protocol1 = StudyProtocol( owner, "Study 1" )
        val protocol2 = StudyProtocol( owner, "Study 2" )
        repo.add( protocol1 )
        repo.add( protocol2 )

        val protocols: List<StudyProtocol> = repo.findAllFor( owner ).toList()
        val expected = listOf( protocol1, protocol2 )
        assertEquals( expected.count(), protocols.intersect( expected ).count() )
    }

    @Test
    fun `can't findAllFor owner which does not exist`()
    {
        val repo = createStudyProtocolRepository()

        assertFailsWith<IllegalArgumentException>
        {
            repo.findAllFor( ProtocolOwner() )
        }
    }
}