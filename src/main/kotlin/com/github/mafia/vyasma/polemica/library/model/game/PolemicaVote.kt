package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class PolemicaVote(
    val day: Int,
    val num: Int?,
    val voter: Position,
    /** API may send `0` for abstain / no candidate in some rows (e.g. split-vote bookkeeping). */
    @JsonDeserialize(using = VoteCandidateDeserializer::class)
    val candidate: Position?,
)

class VoteCandidateDeserializer : JsonDeserializer<Position?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Position? {
        val v = p.intValue
        if (v == 0) return null
        return Position.entries.firstOrNull { it.value == v }
            ?: throw IllegalArgumentException("Invalid position value: $v")
    }
}
