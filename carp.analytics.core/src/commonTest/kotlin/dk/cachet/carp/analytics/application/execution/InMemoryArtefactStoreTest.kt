package dk.cachet.carp.analytics.application.execution

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Clock

/**
 * Extends the abstract [ArtefactStoreTest] to run the shared contract tests.
 */

class InMemoryArtefactStoreTest : ArtefactStoreTest()
{
    override fun createArtefactStore(): ArtefactStore = InMemoryArtefactStore(clock = Clock.System)

    @Test
    fun `concrete artefact store is creatable and not null`()
    {
        val store = createArtefactStore()
        assertNotNull(store)
    }
}

