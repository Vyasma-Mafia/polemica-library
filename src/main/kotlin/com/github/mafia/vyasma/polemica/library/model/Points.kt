package com.github.mafia.vyasma.polemica.library.model

data class PolemicaGamePlayersPoints(
    val success: Boolean,
    val players: List<PlayerPoints>
)

data class PlayerPoints(
    val position: Int,
    val points: Double
)
