package com.github.mafia.vyasma.polemica.library.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PolemicaClientImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var polemicaClient: PolemicaClientImpl
    private val objectMapper = ObjectMapper().findAndRegisterModules().registerKotlinModule()

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        val baseUrl = mockWebServer.url("/").toString().removeSuffix("/")
        polemicaClient = PolemicaClientImpl(
            polemicaBaseUrl = baseUrl,
            polemicaUsername = "testuser",
            polemicaPassword = "testpass",
            objectMapper = objectMapper
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test token refresh on 401 response`() {
        // Первый запрос на логин для получения начального токена
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "initial_token"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Первый запрос к API - возвращает 401
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": "Unauthorized"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Второй запрос на логин для обновления токена
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "refreshed_token"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Повторный запрос к API с новым токеном - успешный
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id": 1, "name": "Competition 1"}]""")
                .addHeader("Content-Type", "application/json")
        )

        // Выполняем запрос
        val competitions = polemicaClient.getCompetitions()

        // Проверяем результат
        assertNotNull(competitions)
        assertEquals(1, competitions.size)
        assertEquals(1L, competitions[0].id)
        assertEquals("Competition 1", competitions[0].name)

        // Проверяем количество запросов
        assertEquals(4, mockWebServer.requestCount, "Должно быть 4 запроса: login, API (401), refresh login, API (200)")

        // Проверяем последовательность запросов
        val request1 = mockWebServer.takeRequest()
        assertEquals("/v1/auth/login", request1.path)
        
        val request2 = mockWebServer.takeRequest()
        assertEquals("/v1/competitions", request2.path)
        assertEquals("Bearer initial_token", request2.getHeader("Authorization"))
        
        val request3 = mockWebServer.takeRequest()
        assertEquals("/v1/auth/login", request3.path)
        
        val request4 = mockWebServer.takeRequest()
        assertEquals("/v1/competitions", request4.path)
        assertEquals("Bearer refreshed_token", request4.getHeader("Authorization"))
    }

    @Test
    fun `test multiple 401 responses with token refresh`() {
        // Первый логин
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "token1"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Первый запрос - 401
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": "Unauthorized"}""")
        )

        // Обновление токена
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "token2"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Повторный запрос - успех
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id": 1, "name": "Competition 1"}]""")
                .addHeader("Content-Type", "application/json")
        )

        val competitions = polemicaClient.getCompetitions()
        
        assertNotNull(competitions)
        assertEquals(1, competitions.size)
        assertEquals(4, mockWebServer.requestCount)
    }

    @Test
    fun `test initial token acquisition`() {
        // Первый логин
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "initial_token"}""")
                .addHeader("Content-Type", "application/json")
        )

        // Успешный запрос
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id": 1, "name": "Competition 1"}]""")
                .addHeader("Content-Type", "application/json")
        )

        val competitions = polemicaClient.getCompetitions()
        
        assertNotNull(competitions)
        assertEquals(2, mockWebServer.requestCount, "Должно быть 2 запроса: login и API")
        
        val loginRequest = mockWebServer.takeRequest()
        assertEquals("/v1/auth/login", loginRequest.path)
        
        val apiRequest = mockWebServer.takeRequest()
        assertEquals("/v1/competitions", apiRequest.path)
        assertEquals("Bearer initial_token", apiRequest.getHeader("Authorization"))
    }

    @Test
    fun `test get match with version parameter`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "initial_token"}""")
                .addHeader("Content-Type", "application/json")
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id": 536971,
                      "name": "Test table",
                      "master": 11,
                      "referee": { "id": 12, "username": "Ref" },
                      "scoringVersion": "4",
                      "scoringType": 1,
                      "version": 4,
                      "zeroVoting": null,
                      "tags": [],
                      "players": [],
                      "checks": [],
                      "shots": [],
                      "stage": null,
                      "votes": [],
                      "comKiller": null,
                      "bonuses": [],
                      "started": "2025-04-17T14:00:00",
                      "stop": null,
                      "isLive": false,
                      "result": null,
                      "num": 1,
                      "table": 1,
                      "phase": 1,
                      "factor": 1.0
                    }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        val match = polemicaClient.getMatch(PolemicaClient.PolemicaMatchId(matchId = 536971, version = 4))

        assertNotNull(match)
        assertEquals(536971L, match.id)
        assertEquals("Test table", match.name)
        assertEquals(2, mockWebServer.requestCount)

        val loginRequest = mockWebServer.takeRequest()
        assertEquals("/v1/auth/login", loginRequest.path)

        val matchRequest = mockWebServer.takeRequest()
        assertEquals("/v1/matches/536971?version=4", matchRequest.path)
        assertEquals("Bearer initial_token", matchRequest.getHeader("Authorization"))
    }

    @Test
    fun `test get profile games without authorization header`() {
        val profileBaseUrl = mockWebServer.url("/").toString().removeSuffix("/")
        polemicaClient = PolemicaClientImpl(
            polemicaBaseUrl = profileBaseUrl,
            polemicaUsername = "testuser",
            polemicaPassword = "testpass",
            objectMapper = objectMapper,
            profileSiteBaseUrl = profileBaseUrl
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "rows": [
                        {
                          "id": 340997,
                          "type": "match",
                          "game_mode": { "value": "competition", "title": "Турнир" },
                          "date_start": "2025-04-17 14:00:00",
                          "date_ends": null,
                          "duration": null,
                          "points": 0.3,
                          "sp": null,
                          "role": { "type": "civilian", "title": "Мирный" },
                          "result": { "title": "Поражение", "code": "fail" },
                          "mmr": null
                        }
                      ],
                      "totalCount": 13
                    }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        val profileGames = polemicaClient.getProfileGames(userId = 76666, page = 1, limit = 201)

        assertNotNull(profileGames)
        assertEquals(13L, profileGames.totalCount)
        assertEquals(1, profileGames.rows.size)
        assertEquals(340997L, profileGames.rows[0].id)
        assertEquals("competition", profileGames.rows[0].gameMode?.value)
        assertNull(profileGames.rows[0].mmr)
        assertEquals(1, mockWebServer.requestCount)

        val profileRequest = mockWebServer.takeRequest()
        assertEquals("/profile/default/get-games?userId=76666&page=1&limit=201", profileRequest.path)
        assertNull(profileRequest.getHeader("Authorization"))
    }

    @Test
    fun `test get profile games deserializes mmr object`() {
        val profileBaseUrl = mockWebServer.url("/").toString().removeSuffix("/")
        polemicaClient = PolemicaClientImpl(
            polemicaBaseUrl = profileBaseUrl,
            polemicaUsername = "testuser",
            polemicaPassword = "testpass",
            objectMapper = objectMapper,
            profileSiteBaseUrl = profileBaseUrl
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "rows": [
                        {
                          "id": 1,
                          "type": "match",
                          "game_mode": null,
                          "date_start": null,
                          "date_ends": null,
                          "duration": null,
                          "points": null,
                          "sp": null,
                          "role": null,
                          "result": null,
                          "mmr": { "value": 1234.5 }
                        }
                      ],
                      "totalCount": 1
                    }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        val profileGames = polemicaClient.getProfileGames(userId = 1, page = 1, limit = 10)

        assertEquals(1234.5, profileGames.rows[0].mmr)
    }
}
