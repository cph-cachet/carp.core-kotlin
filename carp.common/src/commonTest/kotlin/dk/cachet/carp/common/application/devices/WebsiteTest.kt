package dk.cachet.carp.common.application.devices

import kotlin.test.Test
import kotlin.test.assertEquals


class WebsiteTest
{
    @Test
    fun registration_builder_sets_properties()
    {
        val expectedUrl = "https://www.example.com"
        val expectedUserAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

        val registration = WebsiteDeviceRegistrationBuilder().apply {
            url = expectedUrl
            userAgent = expectedUserAgent
        }.build()

        assertEquals( expectedUrl, registration.url )
        assertEquals( expectedUserAgent, registration.userAgent )
    }
}
