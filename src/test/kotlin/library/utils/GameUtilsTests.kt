package com.github.mafia.vyasma.polemica.library.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGame
import com.github.mafia.vyasma.polemica.library.model.game.Position
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PolemicaGameTest {

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())

    @Test
    fun testGetKilledMissWithoutBlackShot() {
        // Загружаем тестовый JSON из ресурсов
        val resource = javaClass.classLoader.getResource("games/328635_without_black_shot.json")
        val game: PolemicaGame = objectMapper.readValue(File(resource.path))

        val killed = game.getKilled()
        Assertions.assertTrue(killed.none { it.position != null })
    }

    @Test
    fun testGetKilledShot() {
        // Загружаем тестовый JSON из ресурсов
        val resource = javaClass.classLoader.getResource("games/328635_with_shot.json")
        val game: PolemicaGame = objectMapper.readValue(File(resource.path))

        val killed = game.getKilled().mapNotNull { it.position }
        assertEquals(killed.size, 1)
        assertEquals(killed[0], Position.TEN)
    }

    @Test
    fun testGetKilledShotWithVotedBlack() {
        // Загружаем тестовый JSON из ресурсов
        val resource = javaClass.classLoader.getResource("games/328635_with_voted_black.json")
        val game: PolemicaGame = objectMapper.readValue(File(resource.path))

        val killed = game.getKilled().mapNotNull { it.position }
        assertEquals(killed.size, 1)
        assertEquals(killed[0], Position.NINE)
    }
}
