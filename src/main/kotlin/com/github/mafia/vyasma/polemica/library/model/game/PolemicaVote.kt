package com.github.mafia.vyasma.polemica.library.model.game

data class PolemicaVote(
    val day: Int,
    val num: Int,
    val voter: Position,
    val candidate: Position
)
