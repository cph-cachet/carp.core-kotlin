package dk.cachet.carp.common.application.services


/**
 * Specifies the API version of an [ApplicationService].
 * Within [major] versions, [minor] versions are backwards compatible.
 *
 * E.g. a 2.0 request will work on a 2.1 hosted service, but not on 1.0 or 3.0.
 */
@Target( AnnotationTarget.CLASS )
@Retention( AnnotationRetention.RUNTIME )
annotation class ApiVersion( val major: Int, val minor: Int )
{
    companion object
    {
        fun toString( version: ApiVersion ) = "${version.major}.${version.minor}"

        /**
         * Determines whether [version] is more recent than [otherVersion].
         */
        fun isMoreRecent( version: ApiVersion, otherVersion: ApiVersion ): Boolean =
            if ( version.major > otherVersion.major ) true
            else version.major == otherVersion.major && version.minor > otherVersion.minor
    }
}
