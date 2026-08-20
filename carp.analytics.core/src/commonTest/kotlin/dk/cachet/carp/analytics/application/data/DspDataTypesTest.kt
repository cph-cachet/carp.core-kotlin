package dk.cachet.carp.analytics.application.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Tests for [DspDataTypes].
 */
class DspDataTypesTest
{
    @Test
    fun all_types_are_enumerable()
    {
        assertTrue( DspDataTypes.isNotEmpty() )
        assertEquals( DspDataTypes.size, DspDataTypes.keys.size )
    }

    @Test
    fun all_types_are_in_the_dsp_namespace()
    {
        assertTrue(
            DspDataTypes.keys.all { it.toString().startsWith( "${DspDataTypes.DSP_NAMESPACE}." ) },
            "Every DSP data type needs to be prefix-filterable on the DSP namespace."
        )
    }

    @Test
    fun branch_namespaces_are_nested_in_the_dsp_namespace()
    {
        val branches = listOf(
            DspDataTypes.ANALYTIC_NAMESPACE,
            DspDataTypes.MEDIA_NAMESPACE,
            DspDataTypes.CONTROL_NAMESPACE
        )
        assertTrue( branches.all { it.startsWith( "${DspDataTypes.DSP_NAMESPACE}." ) } )
    }

    @Test
    fun typesIn_only_returns_types_below_the_namespace()
    {
        assertEquals(DspDataTypes.typesIn( DspDataTypes.DSP_NAMESPACE ), DspDataTypes.values.toList())

        // The generic artefact has no branch, and no branch leaves exist yet.
        assertEquals(DspDataTypes.typesIn( DspDataTypes.ANALYTIC_NAMESPACE ), emptyList())
        assertEquals(DspDataTypes.typesIn( DspDataTypes.MEDIA_NAMESPACE ), emptyList())
        assertEquals(DspDataTypes.typesIn( DspDataTypes.CONTROL_NAMESPACE ), emptyList())
    }
}
