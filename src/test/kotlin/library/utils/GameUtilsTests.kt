package com.github.mafia.vyasma.polemica.library.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGame
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaVote
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

    @Test
    fun testNoBreakWithSpecialZeroVoting() {
        val resource = javaClass.classLoader.getResource("games/334187.json")
        val game: PolemicaGame = objectMapper.readValue(File(resource.path))

        assertEquals(0, game.getFinalVotes(null).filter { it.day == 1 }.size)
    }

    @Test
    fun testGetVotingParticipants() {
        val resource = javaClass.classLoader.getResource("games/333915.json")
        val game: PolemicaGame = objectMapper.readValue(File(resource.path))

        // Тестирование метода

        // День 2, Раунд 1 - должны участвовать игроки 4, 5, 6 (выставленные в круге)
        val participants1 = game.getVotingParticipants(day = 2, round = 1)
        kotlin.test.assertEquals(
            setOf(Position.THREE, Position.FOUR, Position.FIVE, Position.SIX, Position.EIGHT),
            participants1.toSet(),
            "В первом раунде должны участвовать игроки 4, 5 и 6"
        )

        // День 2, Раунд 2 - должны участвовать игроки 4, 5, 6 (все получили по 3 голоса в раунде 1)
        val participants2 = game.getVotingParticipants(day = 2, round = 2)
        kotlin.test.assertEquals(
            setOf(Position.FOUR, Position.FIVE, Position.SIX),
            participants2.toSet(),
            "Во втором раунде должны участвовать те же игроки 4, 5 и 6"
        )
    }
}
