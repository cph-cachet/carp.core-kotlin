package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.ExtractUniqueKeyMap
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.users.ParticipantAttribute


/**
 * An initially empty configuration to start defining expected participant data to be input by users.
 *
 * Input types of added [ParticipantAttribute]s should be unique.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyParticipantDataConfiguration :
    AbstractMap<InputDataType, ParticipantAttribute>(),
    ParticipantDataConfiguration
{
    private val _expectedParticipantData: ExtractUniqueKeyMap<InputDataType, ParticipantAttribute> =
        ExtractUniqueKeyMap( { attribute -> attribute.inputType } )
        {
            key -> IllegalArgumentException( "Input type \"$key\" is not unique within participant data configuration." )
        }

    override val entries: Set<Map.Entry<InputDataType, ParticipantAttribute>>
        get() = _expectedParticipantData.entries

    override val expectedParticipantData: Set<ParticipantAttribute>
        get() = _expectedParticipantData.values.toSet()


    override fun addExpectedParticipantData( attribute: ParticipantAttribute ) =
        _expectedParticipantData.tryAddIfKeyIsNew( attribute )

    override fun removeExpectedParticipantData( attribute: ParticipantAttribute ): Boolean =
        _expectedParticipantData.remove( attribute )
}
