package com.github.mafia.vyasma.polemica.library.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mafia.vyasma.polemica.library.model.PlayerPoints
import org.apache.commons.text.StringEscapeUtils
import org.springframework.web.client.RestTemplate
import java.util.regex.Pattern

class GamePointsService(private val restTemplate: RestTemplate) {

    fun fetchPlayerStats(id: Long): List<PlayerPoints> {
        // Получаем HTML страницу
        val response = restTemplate.getForObject("https://polemicagame.com/match/$id", String::class.java)
            ?: throw RuntimeException("Не удалось получить данные с сайта")

        // Извлекаем JSON из атрибута :game-data
        val pattern = Pattern.compile(":game-data='(.*?)'")
        val matcher = pattern.matcher(response)

        if (!matcher.find()) {
            throw RuntimeException("Не удалось найти данные игры в HTML")
        }

        // Разэкранируем HTML-сущности и парсим JSON
        val gameDataJson = StringEscapeUtils.unescapeHtml4(matcher.group(1))
        val objectMapper = ObjectMapper()
        val gameData = objectMapper.readTree(gameDataJson)

        // Извлекаем информацию о каждом игроке
        return gameData.path("players").mapNotNull { player ->
            try {
                PlayerPoints(
                    position = player.path("tablePosition").asInt(),
                    points = player.path("points").asDouble()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
