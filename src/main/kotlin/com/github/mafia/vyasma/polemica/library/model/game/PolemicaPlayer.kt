package com.github.mafia.vyasma.polemica.library.model.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class PolemicaPlayer(
    val position: Position,
    val username: String,
    val role: Role,
    val techs: List<Foul>,
    val fouls: List<Foul>,
    val guess: PolemicaGuess?,
    @JsonDeserialize(using = PolemicaPlayerFieldDeserializer::class)
    val player: PolemicaUser?,
    val disqual: Stage?,
    val award: Double?
)

class PolemicaPlayerFieldDeserializer : JsonDeserializer<PolemicaUser>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): PolemicaUser? {
        val node: JsonNode = parser.codec.readTree(parser)

        return when {
            node.isIntegralNumber -> PolemicaUser(node.asLong(), "")
            node.isObject -> PolemicaUser(node.get("id").asLong(), node.get("username").asText())
            node.isNull -> null
            else -> throw IllegalArgumentException("Unexpected JSON for 'player': $node")
        }
    }
}
