package dk.cachet.carp.protocols.domain.serialization

import kotlinx.serialization.KSerializer


expect object PolymorphicSerializer : KSerializer<Any>