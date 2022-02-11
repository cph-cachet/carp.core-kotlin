package dk.cachet.carp.common.application.services

import dk.cachet.carp.common.infrastructure.serialization.createCarpStringPrimitiveSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


/**
 * Specifies the API version of an [ApplicationService].
 * Within [major] versions, [minor] versions are backwards compatible.
 *
 * E.g. a 2.0 request will work on a 2.1 hosted service, but not on 1.0 or 3.0.
 */
@Serializable( ApiVersionSerializer::class )
class ApiVersion( val major: Int, val minor: Int )
{
    init
    {
        require( major >= 0 && minor >= 0 ) { "Major and minor number must be positive." }
    }


    companion object
    {
        /**
         * Initializes an [ApiVersion] instance based on a string representation formatted as "<major>.<minor>".
         */
        fun fromString( apiVersion: String ): ApiVersion
        {
            require( ApiVersionRegex.matches( apiVersion ) ) { "Invalid API version string representation." }

            val (major, minor) = apiVersion.split( '.' )
            return ApiVersion( major.toInt(), minor.toInt() )
        }
    }


    /**
     * Determines whether this version is more recent than [otherVersion].
     */
    fun isMoreRecent( otherVersion: ApiVersion ): Boolean =
        if ( major > otherVersion.major ) true
        else major == otherVersion.major && minor > otherVersion.minor

    override fun toString(): String = "$major.$minor"
}


/**
 * Regular expression to match [ApiVersion] represented as a string in the format "<major>.<minor>".
 */
val ApiVersionRegex = Regex( """(\d)\.(\d)""" )


object ApiVersionSerializer : KSerializer<ApiVersion> by
    createCarpStringPrimitiveSerializer( { version -> ApiVersion.fromString( version ) } )
