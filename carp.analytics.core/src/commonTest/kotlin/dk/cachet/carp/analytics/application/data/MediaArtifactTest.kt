package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.ApplicationData
import kotlin.test.Test
import kotlin.test.assertFailsWith


/**
 * Tests for the [MediaArtifact] leaves.
 */
class MediaArtifactTest
{
    @Test
    fun image_width_needs_to_be_positive() = assertInvalid { ImageArtifact( testReference(), 0, 100 ) }

    @Test
    fun image_height_needs_to_be_positive() = assertInvalid { ImageArtifact( testReference(), 100, 0 ) }

    @Test
    fun plot_spec_needs_a_format() =
        assertInvalid { PlotArtifact( testReference(), ApplicationData( """{"mark":"line"}""" ) ) }

    private fun assertInvalid( create: () -> MediaArtifact )
    {
        assertFailsWith<IllegalArgumentException> { create() }
    }
}
