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

class PolemicaClientImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var polemicaClient: PolemicaClientImpl
    private val objectMapper = ObjectMapper().registerKotlinModule()

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
}
