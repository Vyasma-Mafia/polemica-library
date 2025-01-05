package com.github.mafia.vyasma.polemica.library.utils

import com.github.mafia.vyasma.polemica.library.client.PolemicaClient
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaUser

object MetricsUtils {
    fun getRating(value: List<PolemicaClient.CompetitionPlayerResult>): List<PolemicaUser> {
        return value.sortedBy { it.metrics.civ.totalScores + it.metrics.don.totalScores + it.metrics.maf.totalScores + it.metrics.com.totalScores }
            .map { PolemicaUser(it.id, it.username) }
    }
}
