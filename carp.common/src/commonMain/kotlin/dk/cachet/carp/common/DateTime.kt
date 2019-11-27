package dk.cachet.carp.common


expect class DateTime
{
    companion object {
        fun now(): DateTime
    }
}
