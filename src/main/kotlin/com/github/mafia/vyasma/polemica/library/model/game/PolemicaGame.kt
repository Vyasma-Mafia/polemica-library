package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class PolemicaGame(
    val id: Long,
    val master: Long,
    val referee: PolemicaUser,
    val scoringVersion: String?,
    val scoringType: Int,
    val version: Int,
    val zeroVoting: String?,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val tags: List<String>,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val players: List<PolemicaPlayer>,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val checks: List<PolemicaCheck>,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val shots: List<PolemicaShot>,
    val stage: Stage,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val votes: List<PolemicaVote>,
    val comKiller: Position?,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val bonuses: List<PolemicaBonus>,
    val started: LocalDateTime,
    val stop: Stage?,
    val isLive: Boolean?,
    val result: PolemicaGameResult?
)
