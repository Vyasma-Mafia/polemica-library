package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class PolemicaGame(
    val id: Long?,
    val name: String?,
    val master: Long,
    /** May be absent or null for some API payloads (e.g. in-progress or legacy games). */
    val referee: PolemicaUser?,
    val scoringVersion: String?,
    val scoringType: Int,
    val version: Int,
    val zeroVoting: ZeroVoting?,
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
