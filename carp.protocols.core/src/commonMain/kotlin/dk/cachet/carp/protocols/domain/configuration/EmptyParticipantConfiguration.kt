package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining expected participant and data to be input by users.
 *
 * Input types of added [ParticipantAttribute]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyParticipantConfiguration : ParticipantConfiguration
{
    private val _expectedParticipantData: ExtractUniqueKeyMap<InputDataType, ParticipantAttribute> =
        ExtractUniqueKeyMap( { attribute -> attribute.inputDataType } )
        {
            key -> IllegalArgumentException( "Input type \"$key\" is not unique within participant configuration." )
        }

    override val expectedParticipantData: Set<ParticipantAttribute>
        get() = _expectedParticipantData.values.toSet()

    private val _participantRoles: ExtractUniqueKeyMap<String, ParticipantRole> =
        ExtractUniqueKeyMap( { role -> role.role } )
        {
            key -> IllegalArgumentException( "Role name \"$key\" is not unique within participant configuration." )
        }

    override val participantRoles: Set<ParticipantRole>
        get() = _participantRoles.values.toSet()


    override fun addExpectedParticipantData( attribute: ParticipantAttribute ) =
        _expectedParticipantData.tryAddIfKeyIsNew( attribute )

    override fun removeExpectedParticipantData( attribute: ParticipantAttribute ): Boolean =
        _expectedParticipantData.remove( attribute )

    override fun addParticipantRole( role: ParticipantRole ): Boolean =
        _participantRoles.tryAddIfKeyIsNew( role )

    override fun removeParticipantRole( role: ParticipantRole ): Boolean =
        _participantRoles.remove( role )
}
