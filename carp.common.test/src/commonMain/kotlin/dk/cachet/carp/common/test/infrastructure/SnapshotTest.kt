package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.common.infrastructure.serialization.JSON
import kotlinx.serialization.KSerializer
import kotlin.test.*


/**
 * Base class to test [Snapshot] implementations.
 */
@Suppress( "FunctionName" )
abstract class SnapshotTest<TObject : AggregateRoot<*, TSnapshot, *>, TSnapshot : Snapshot<TObject>>(
    private val snapshotSerializer: KSerializer<TSnapshot>
)
{
    abstract fun createObject(): TObject
    abstract fun changeSnapshotVersion( toChange: TSnapshot, version: Int ): TSnapshot

    @Test
    fun can_serialize_and_deserialize_snapshot_using_JSON()
    {
        val toSerialize = createObject()
        val snapshot = toSerialize.getSnapshot()

        val serialized = JSON.encodeToString( snapshotSerializer, snapshot )
        val parsed: TSnapshot = JSON.decodeFromString( snapshotSerializer, serialized )
        assertEquals( snapshot, parsed )
    }

    @Test
    fun toObject_contains_fromSnapshotVersion()
    {
        val originalVersion = 10
        val snapshot = changeSnapshotVersion( createObject().getSnapshot(), originalVersion )
        val loadedObject = snapshot.toObject()

        assertEquals( originalVersion, loadedObject.fromSnapshotVersion )
    }
}
