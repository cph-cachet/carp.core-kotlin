@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.application


actual object DefaultUUIDFactory : UUIDFactory
{
    actual override fun randomUUID(): UUID = UUID( java.util.UUID.randomUUID().toString() )
}
