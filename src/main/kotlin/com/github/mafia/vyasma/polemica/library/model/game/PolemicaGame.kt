package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
    val tags: List<String>?,
    val players: List<PolemicaPlayer>?,
    val checks: List<PolemicaCheck>?,
    val shots: List<PolemicaShot>?,
    val stage: Stage?,
    val votes: List<PolemicaVote>?,
    val comKiller: Position?,
    val bonuses: List<PolemicaBonus>?,
    val started: LocalDateTime,
    val stop: Stage?,
    val isLive: Boolean?,
    val result: PolemicaGameResult?,
    val num: Int?,
    val table: Int?,
    val phase: Int?,
    val factor: Double?
)
