package dk.cachet.carp.analytics.application.data

import kotlin.test.Test
import kotlin.test.assertFailsWith


/**
 * Tests for [ArtifactReference].
 */
class ArtifactReferenceTest
{
    @Test
    fun cant_initialize_with_empty_uri() = assertInvalid { ArtifactReference( "", MIME, 10, DIGEST ) }

    @Test
    fun cant_initialize_with_empty_mime_type() = assertInvalid { ArtifactReference( URI, "", 10, DIGEST ) }

    @Test
    fun cant_initialize_with_negative_size() = assertInvalid { ArtifactReference( URI, MIME, -1, DIGEST ) }

    @Test
    fun cant_initialize_with_malformed_digest() = assertInvalid { ArtifactReference( URI, MIME, 10, "nope" ) }

    @Test
    fun cant_initialize_with_uppercase_digest() =
        assertInvalid { ArtifactReference( URI, MIME, 10, DIGEST.uppercase() ) }


    private fun assertInvalid( create: () -> ArtifactReference )
    {
        assertFailsWith<IllegalArgumentException> { create() }
    }


    companion object
    {
        private const val URI = "carp://artefacts/run-1/step-1/plot.png"
        private const val MIME = "image/png"
        private const val DIGEST = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }
}
