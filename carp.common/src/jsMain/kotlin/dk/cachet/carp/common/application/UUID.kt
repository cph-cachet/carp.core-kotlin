@file:Suppress("MatchingDeclarationName")

package dk.cachet.carp.common.application


actual object DefaultUUIDFactory : UUIDFactory
{
    private const val base = 16

    actual override fun randomUUID(): UUID
    {
        // It does not seem like JS can generate true UUIDs, but this is a best effort:
        // https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
        // Regardless, we do not need to support UUID generation in JS, as this is used as client-side code.
        // The only real reason for providing a best effort implementation here is to be able to run unit tests.
        val uuidString = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace( Regex( "[xy]" ) ) { match ->
            val random = js("Math.random() * $base | 0")
            val char = if ( match.value == "x" ) random else js( "random & 0x3 | 0x8" )
            char.toString( base ) as CharSequence
        }

        return UUID( uuidString )
    }
}
