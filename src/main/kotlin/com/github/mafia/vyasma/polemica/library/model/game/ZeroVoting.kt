package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumSerializer
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnumSerializer

@JsonSerialize(using = StringEnumSerializer::class)
@JsonDeserialize(using = ZeroVotingDeserializer::class)
enum class ZeroVoting(
    override val value: String
) : StringEnum {
    RESPEECH("respeech"),
    LIFT_ONLY("liftOnly"),
    NONE("none")
}

class ZeroVotingDeserializer :
    StringEnumDeserializer<ZeroVoting>(ZeroVoting.entries.toTypedArray())
