@file:Suppress( "MagicNumber" )

import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import kotlin.js.JsExport


/**
 * Refers to types in CARP common that aren't JS exported.
 * Referring to them here guarantees that they are included in `$crossModule$` of generated JS sources.
 * This way, custom TypeScript declarations augmentations can access them.
 */
@JsExport
class CarpCommonExport
{
    // data.input.elements
    val inputElement = InputElement::class
    fun inputElementMembers( inputElement: InputElement<*> ) =
        object
        {
            val prompt = inputElement.prompt
        }

    // users
    val expectedParticipantDataCompanion = ExpectedParticipantData.Companion::class
    val assignedTo = AssignedTo::class
    val all = AssignedTo.All::class
    val roles = AssignedTo.Roles::class
    val participantAttribute = ParticipantAttribute::class
    fun participantAttributeMembers( attribute: ParticipantAttribute ) =
        object
        {
            val inputDataType = attribute.inputDataType
            val inputElement = attribute.getInputElement( CarpInputDataTypes )
            val isValidInput = attribute.isValidInput( CarpInputDataTypes, 42 )
            val inputToData = attribute.inputToData( CarpInputDataTypes, Text( "Prompt" ) )
        }
    val defaultParticipantAttribute = ParticipantAttribute.DefaultParticipantAttribute::class
    val customParticipantAttribute = ParticipantAttribute.CustomParticipantAttribute::class
}
