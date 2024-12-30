package com.github.mafia.vyasma.polemica.library.model.game

data class PolemicaBonus(
    val position: Position,
    val text: String,
    val scores: Double,
    val basic: Boolean
)
