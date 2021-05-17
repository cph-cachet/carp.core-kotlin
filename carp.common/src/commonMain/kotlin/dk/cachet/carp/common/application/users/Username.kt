package dk.cachet.carp.common.application.users

import kotlinx.serialization.Serializable


/**
 * A unique name which identifies an [Account].
 */
@Serializable
data class Username( val name: String )
