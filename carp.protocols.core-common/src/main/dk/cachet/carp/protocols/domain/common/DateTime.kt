package dk.cachet.carp.protocols.domain.common


expect class DateTime
{
    companion object {
        fun now(): DateTime
    }
}