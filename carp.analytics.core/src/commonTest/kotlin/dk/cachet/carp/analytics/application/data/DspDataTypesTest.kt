package dk.cachet.carp.analytics.application.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Tests for [DspDataTypes].
 */
class DspDataTypesTest
{
    private val branches = listOf(
        DspDataTypes.ANALYTIC_NAMESPACE,
        DspDataTypes.MEDIA_NAMESPACE,
        DspDataTypes.CONTROL_NAMESPACE
    )


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
        assertTrue( branches.all { it.startsWith( "${DspDataTypes.DSP_NAMESPACE}." ) } )
    }

    @Test
    fun typesIn_returns_every_type_for_the_dsp_namespace()
    {
        assertEquals(DspDataTypes.typesIn( DspDataTypes.DSP_NAMESPACE ), DspDataTypes.values.toList())
    }

    @Test
    fun typesIn_only_returns_types_below_the_namespace()
    {
        branches.forEach { branch ->
            val types = DspDataTypes.typesIn( branch )
            assertTrue( types.isNotEmpty(), "\"$branch\" has no types." )
            assertTrue( types.all { it.type.toString().startsWith( "$branch." ) } )
        }
    }

    @Test
    fun branches_do_not_overlap_and_only_the_generic_has_none()
    {
        val inBranches = branches.flatMap { DspDataTypes.typesIn( it ) }
        assertEquals( inBranches.size, inBranches.toSet().size, "A type appears in more than one branch." )

        val withoutBranch = DspDataTypes.values.toList() - inBranches.toSet()
        assertEquals( listOf( DspDataTypes.GENERIC_ARTIFACT ), withoutBranch )
    }
}
