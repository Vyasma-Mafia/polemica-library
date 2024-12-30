package com.github.mafia.vyasma.polemica.library.utils

import com.github.mafia.vyasma.polemica.library.model.game.Role

fun Role.isRed(): Boolean {
    return when (this) {
        Role.SHERIFF, Role.PEACE -> true
        Role.DON, Role.MAFIA -> false
    }
}

fun Role.isBlack() = isRed().not()
