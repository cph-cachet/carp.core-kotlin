package dk.cachet.carp.analytics.application.data


/**
 * An [ArtifactReference] to use where the test doesn't care what it points at.
 */
internal fun testReference() = ArtifactReference(
    "carp://artefacts/run-1/step-1/output.bin",
    "application/octet-stream",
    1024,
    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
)
