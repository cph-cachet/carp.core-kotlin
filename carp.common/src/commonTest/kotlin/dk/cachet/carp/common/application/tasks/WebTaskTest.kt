package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import kotlin.test.*


/**
 * Tests for [WebTask].
 */
class WebTaskTest
{
    @Test
    fun url_pattern_is_expected_format()
    {
        val id = WebTask.UrlVariable.PARTICIPANT_ID

        assertEquals( "{{PARTICIPANT-ID}}", id.pattern )
    }

    @Test
    fun constructUrl_succeeds()
    {
        val surveyUrl = "http://awesomesurvey.com/42"
        val url =
            "$surveyUrl?" +
            "participantId=${WebTask.UrlVariable.PARTICIPANT_ID.pattern}" +
            "&deploymentId=${WebTask.UrlVariable.DEPLOYMENT_ID.pattern}" +
            "&triggerId=${WebTask.UrlVariable.TRIGGER_ID.pattern}"
        val task = WebTask( "Survey", emptyList(), "Demographics survey", url )

        val participantId = UUID.randomUUID()
        val deploymentId = UUID.randomUUID()
        val triggerId = 42
        val constructedUrl = task.constructUrl( participantId, deploymentId, triggerId )

        assertEquals(
            "$surveyUrl?participantId=$participantId&deploymentId=$deploymentId&triggerId=$triggerId",
            constructedUrl
        )
    }

    @Test
    fun builder_succeeds()
    {
        val surveyUrl = "http://surveymonkey.com/asd7f87sadfjalksjf"
        val surveyDescription = "A very important survey"
        val backgroundMeasures = listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) )
        val webTask = WebTaskBuilder().apply {
            url = surveyUrl
            description = surveyDescription
            measures = backgroundMeasures
        }.build( "Survey" )

        assertEquals( surveyUrl, webTask.url )
        assertEquals( surveyDescription, webTask.description )
        assertEquals( backgroundMeasures, webTask.measures )
    }
}
