package dk.cachet.carp.analytics.application.data

import kotlin.test.Test
import kotlin.test.assertFailsWith


/**
 * Tests for the [AnalyticResult] leaves.
 */
class AnalyticResultTest
{
    @Test
    fun computed_feature_needs_a_name() = assertInvalid { ComputedFeature( " ", 72.4 ) }

    @Test
    fun summary_statistic_needs_a_name() = assertInvalid { SummaryStatistic( " ", 72.4, 60 ) }

    @Test
    fun summary_statistic_needs_observations() = assertInvalid { SummaryStatistic( "mean", 72.4, 0 ) }

    @Test
    fun classification_needs_a_label() = assertInvalid { Classification( " " ) }

    @Test
    fun classification_confidence_cant_exceed_one() = assertInvalid { Classification( "sleep", 1.5 ) }

    @Test
    fun classification_confidence_cant_be_negative() = assertInvalid { Classification( "sleep", -0.1 ) }

    @Test
    fun feature_table_needs_columns() = assertInvalid { FeatureTable( testReference(), emptyList() ) }

    @Test
    fun feature_table_column_names_cant_be_blank() =
        assertInvalid { FeatureTable( testReference(), listOf( "hr", " " ) ) }

    @Test
    fun feature_table_row_count_cant_be_negative() =
        assertInvalid { FeatureTable( testReference(), listOf( "hr" ), -1 ) }


    private fun assertInvalid( create: () -> AnalyticResult )
    {
        assertFailsWith<IllegalArgumentException> { create() }
    }
}
