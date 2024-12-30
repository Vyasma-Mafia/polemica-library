package com.github.mafia.vyasma.polemica.library.model.game

data class PolemicaGuess(
    val civs: List<Position>?,
    val mafs: List<Position>?,
    val vice: Position?
) {
}
