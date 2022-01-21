package dk.cachet.carp.rpc

import kotlin.test.*


internal val exampleApplicationServiceRequests: Map<ApplicationServiceInfo, List<ExampleRequest>> =
    applicationServices.associateWith { generateExampleRequests( it.serviceKlass, it.requestObjectKlass ) }


class GenerateExampleRequestsTest
{
    @Test
    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    fun generateExampleRequests_always_generates_same_JSON()
    {
        applicationServices.forEach { service ->
            val firstRun =
                exampleApplicationServiceRequests[ service ]!!.associateBy { it.method }
            val secondRun =
                generateExampleRequests( service.serviceKlass, service.requestObjectKlass ).associateBy { it.method }

            firstRun.forEach { (method, firstExample) ->
                val secondExample = secondRun[ method ]
                assertNotNull( secondExample )

                assertTrue(
                    firstExample.requestObject == secondExample.requestObject,
                    "The request example generated for \"$method\" isn't deterministic."
                )
                assertTrue(
                    firstExample.response == secondExample.response,
                    "The response example generated for \"$method\" isn't deterministic."
                )
            }
        }
    }
}
