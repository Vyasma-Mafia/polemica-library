package com.github.mafia.vyasma.polemica.library.utils

import com.github.mafia.vyasma.polemica.library.client.PolemicaClient

fun compare(a: PolemicaClient.CompetitionPlayerResult, b: PolemicaClient.CompetitionPlayerResult): Int {
    if (a.sumScore().round() != b.sumScore().round()) {
        return a.sumScore().compareTo(b.sumScore())
    } else if (a.sumAward().round() != b.sumAward().round()) {
        return a.sumAward().compareTo(b.sumAward())
    } else if (a.winAsDonOrSher() != b.winAsDonOrSher()) {
        return a.winAsDonOrSher().compareTo(b.winAsDonOrSher())
    } else if (a.firstNightKills() != b.firstNightKills()) {
        return a.firstNightKills().compareTo(b.firstNightKills())
    } else if (a.sumQuessScore().round() != b.sumQuessScore().round()) {
        return a.sumQuessScore().compareTo(b.sumQuessScore())
    }
    return 0
}

fun PolemicaClient.CompetitionPlayerResult.sumScore(): Double {
    return metrics.com.totalScores + metrics.maf.totalScores + metrics.civ.totalScores + metrics.don.totalScores
}

fun PolemicaClient.CompetitionPlayerResult.sumAward(): Double {
    return metrics.com.totalAwards + metrics.maf.totalAwards + metrics.civ.totalAwards + metrics.don.totalAwards
}

fun PolemicaClient.CompetitionPlayerResult.winAsDonOrSher(): Long {
    return metrics.com.wins + metrics.don.wins
}

fun PolemicaClient.CompetitionPlayerResult.firstNightKills(): Long {
    return metrics.com.firstShot + metrics.maf.firstShot + metrics.civ.firstShot + metrics.don.firstShot
}

fun PolemicaClient.CompetitionPlayerResult.sumQuessScore(): Double {
    return metrics.com.guessScore + metrics.maf.guessScore + metrics.civ.guessScore + metrics.don.guessScore
}

private fun Double.round() = Math.round(this * 10000.0) / 10000.0
