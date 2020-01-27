package dk.cachet.carp.common


expect class DateTime
{
    companion object
    {
        fun now(): DateTime
    }

    /**
     * Output as ISO 8601 UTC date and time in extended format with day and second precision.
     * E.g., "2020-01-01T12:00:00Z"
     */
    override fun toString(): String
}
