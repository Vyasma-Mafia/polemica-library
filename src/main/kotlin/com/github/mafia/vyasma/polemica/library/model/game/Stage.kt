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
    val player: Int?,
    val voting: Int?
) : Comparable<Stage> {
    override fun compareTo(other: Stage): Int = when {
        day < other.day -> -1
        day > other.day -> 1
        (player ?: 0) < (other.player ?: 0) -> -1
        (player ?: 0) > (other.player ?: 0) -> 1
        else -> type.compareTo(other.type)
    }
}

@JsonSerialize(using = StringEnumSerializer::class)
@JsonDeserialize(using = StageTypeDeserializer::class)
enum class StageType(override val value: String) : StringEnum {
    DEALING("dealing"),
    BRIEFING("briefing"),
    COM_INTRO("comIntro"),
    SPEECH("speech"),
    VOTING("voting"),
    VOTED("voted"),
    SHOOTING("shooting"),
    SHOOTED("shooted"),
    RESPEECH("reSpeech"),
    LIFT("lift"),
    DON_CHECK("donCheck"),
    COM_CHECK("comCheck"),
    GUESS("guess"),
    COM_KILL("comKill"),
    GAME_OVER("gameOver")
}

class StageTypeDeserializer : StringEnumDeserializer<StageType>(StageType.entries.toTypedArray())

