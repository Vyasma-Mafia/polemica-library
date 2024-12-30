package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.StringEnumSerializer

@JsonIgnoreProperties(ignoreUnknown = true)
data class Stage(
    val type: StageType,
    val day: Int,
    val player: Int
) {
}

@JsonSerialize(using = StringEnumSerializer::class)
@JsonDeserialize(using = StageTypeDeserializer::class)
enum class StageType(override val value: String) : StringEnum {
    DEALING("dealing"),
    SPEECH("speech"),
    VOTING("voting"),
    VOTED("voted"),
    SHOOTING("shooting"),
    SHOOTED("shooted"),
    RESPEECH("reSpeech"),
    LIFT("lift"),
    DON_CHECK("donCheck"),
    COM_CHECK("comCheck"),
    COM_KILL("comKill"),
    GAME_OVER("gameOver")
}

class StageTypeDeserializer : StringEnumDeserializer<StageType>(StageType.entries.toTypedArray())

