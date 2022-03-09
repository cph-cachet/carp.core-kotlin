package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [ProtocolService].
 */
interface ProtocolServiceTest
{
    /**
     * Create a study service to be used in the tests.
     */
    fun createService(): ProtocolService


    @Test
    fun add_protocol_and_retrieving_it_succeeds() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        service.add( snapshot, "Initial" )
        val retrieved = service.getBy( protocol.id, "Initial" )
        assertEquals( snapshot, retrieved )
    }

    @Test
    fun add_fails_when_protocol_already_exists() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol().getSnapshot()
        service.add( protocol )

        assertFailsWith<IllegalArgumentException> { service.add( protocol ) }
    }

    @Test
    fun addVersion_succeeds() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        service.add( protocol.getSnapshot() )

        modifyProtocol( protocol )
        val version2Snapshot = protocol.getSnapshot()
        service.addVersion( version2Snapshot, "Version 2" )

        val retrieved = service.getBy( protocol.id, "Version 2" )
        assertEquals( version2Snapshot, retrieved )
    }

    @Test
    fun addVersion_fails_when_protocol_does_not_exist() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()

        assertFailsWith<IllegalArgumentException> { service.addVersion( protocol.getSnapshot() ) }
    }

    @Test
    fun addVersion_fails_when_version_tag_in_use() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        service.add( protocol.getSnapshot(), "In use" )
        modifyProtocol( protocol )

        val newSnapshot = protocol.getSnapshot()
        assertFailsWith<IllegalArgumentException> { service.addVersion( newSnapshot, "In use" ) }
    }

    @Test
    fun add_and_addVersion_fail_when_protocol_is_invalid() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        service.add( protocol.getSnapshot() )

        val invalidSnapshot = protocol.getSnapshot().copy(
            taskControls = setOf(
                TaskControl( 0, "Non-existing", "Not a device", TaskControl.Control.Start )
            )
        )

        assertFailsWith<IllegalArgumentException> { service.add( invalidSnapshot ) }
        assertFailsWith<IllegalArgumentException> { service.addVersion( invalidSnapshot, "New version" ) }
    }

    @Test
    fun updateParticipantDataConfiguration_replaces_existing_attributes() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type" ) )
        protocol.addExpectedParticipantData( attribute )
        val version = "Version"
        service.add( protocol.getSnapshot(), version )

        val newAttribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "other_type" ) )
        val updated = service.updateParticipantDataConfiguration( protocol.id, version, setOf( newAttribute ) )
        val retrieved = service.getBy( protocol.id, version )

        val updateIsStored = updated == retrieved
        assertTrue( updateIsStored )
        val updatedProtocol = StudyProtocol.fromSnapshot( retrieved )
        assertEquals( setOf( newAttribute ), updatedProtocol.expectedParticipantData )
    }

    @Test
    fun updateParticipantDataConfiguration_fails_for_unknown_protocol() = runTest {
        val service = createService()

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type" ) )
        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException>
        {
            service.updateParticipantDataConfiguration( unknownId, "Unknown version", setOf( attribute ) )
        }
    }

    @Test
    fun getBy_returns_latest_version_when_no_version_specified() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        service.add( protocol.getSnapshot(), "In use" )
        modifyProtocol( protocol )
        val lastSnapshot = protocol.getSnapshot()
        service.addVersion( lastSnapshot, "Version 2" )

        val retrieved = service.getBy( protocol.id )
        assertEquals( lastSnapshot, retrieved )
    }

    @Test
    fun getBy_fails_for_nonexisting_protocol() = runTest {
        val service = createService()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.getBy( unknownId, "Nope" ) }
    }

    @Test
    fun getAllForOwner_returns_last_version_of_each_protocol() = runTest {
        val service = createService()
        val protocol1 = createEmptyProtocol( "Protocol 1" )
        service.add( protocol1.getSnapshot() )
        val protocol2 = createEmptyProtocol( "Protocol 2" )
        service.add( protocol2.getSnapshot() )
        modifyProtocol( protocol2 )
        service.addVersion( protocol2.getSnapshot(), "Version 2" )

        val ownerId = protocol1.ownerId // Also owner of protocol2; `createEmptyProtocol` has a fixed owner.
        val protocols = service.getAllForOwner( ownerId )
        assertEquals( setOf( protocol1.getSnapshot(), protocol2.getSnapshot() ), protocols.toSet() )
    }

    @Test
    fun getAllForOwner_returns_empty_list_when_none_found() = runTest {
        val service = createService()

        val unknownId = UUID.randomUUID()
        assertTrue( service.getAllForOwner( unknownId ).isEmpty() )
    }

    @Test
    fun getVersionHistoryFor_succeeds() = runTest {
        val service = createService()
        val protocol = createEmptyProtocol()
        service.add( protocol.getSnapshot(), "1" )
        modifyProtocol( protocol )
        service.addVersion( protocol.getSnapshot(), "2" )

        val history = service.getVersionHistoryFor( protocol.id )
        assertEquals( setOf( "1", "2" ), history.map { it.tag }.toSet() )
    }

    @Test
    fun getVersionHistoryFor_fails_when_protocol_not_found() = runTest {
        val service = createService()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.getVersionHistoryFor( unknownId ) }
    }

    private fun modifyProtocol( protocol: StudyProtocol ): StudyProtocol =
        protocol.apply { addPrimaryDevice( StubPrimaryDeviceConfiguration() ) }
}
