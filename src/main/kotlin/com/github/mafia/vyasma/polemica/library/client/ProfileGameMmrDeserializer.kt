package com.github.mafia.vyasma.polemica.library.client

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Публичный профиль иногда отдаёт [mmr] числом, null или вложенным объектом (например с полями value/current).
 */
class ProfileGameMmrDeserializer : JsonDeserializer<Double?>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double? {
        return when (p.currentToken()) {
            JsonToken.VALUE_NULL -> null
            JsonToken.VALUE_NUMBER_FLOAT, JsonToken.VALUE_NUMBER_INT -> p.doubleValue
            JsonToken.START_OBJECT -> {
                val node = ctxt.readTree(p)
                if (node is ObjectNode) extractDouble(node) else null
            }
            else -> null
        }
    }

    private fun extractDouble(node: ObjectNode): Double? {
        for (key in PREFERRED_KEYS) {
            val child = node.get(key) ?: continue
            if (child.isNumber) return child.asDouble()
        }
        val it = node.fields()
        while (it.hasNext()) {
            val (_, v) = it.next()
            if (v.isNumber) return v.asDouble()
        }
        return null
    }

    companion object {
        private val PREFERRED_KEYS = listOf(
            "value", "mmr", "current", "after", "before", "total", "delta", "change"
        )
    }
}
