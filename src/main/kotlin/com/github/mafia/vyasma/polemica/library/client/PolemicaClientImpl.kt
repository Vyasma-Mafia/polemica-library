package com.github.mafia.vyasma.polemica.library.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGame
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PolemicaClientImpl(
    private val polemicaBaseUrl: String,
    private val polemicaUsername: String,
    private val polemicaPassword: String,
    private val objectMapper: ObjectMapper
) : PolemicaClient {

    private var polemicaToken: String? = null
    private val lock = ReentrantLock()
    val webClient = WebClient.builder()
        .codecs { it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper)) }
        .codecs { it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper)) }
        .baseUrl(polemicaBaseUrl)
        .filter { request, next ->
        if (isAuthRequest(request)) {
            next.exchange(request)
        } else {
            addAuthorizationHeader(request, next)
        }
    }.build()

    override fun getGameFromClub(clubGameId: PolemicaClient.PolemicaClubGameId): PolemicaGame {
        return webClient.get()
            .uri(
                "/v1/clubs/${clubGameId.clubId}/games/${clubGameId.gameId}" + getVersionQueryParam(clubGameId.version)
            )
            .retrieve()
            .bodyToMono(PolemicaGame::class.java)
            .block() ?: throw RuntimeException("Get game from club error")
    }

    private fun getVersionQueryParam(version: Long?) = if (version != null) "?version=$version" else ""

    override fun getGamesFromClub(
        clubId: Long,
        offset: Long,
        limit: Long
    ): List<PolemicaClient.PolemicaClubGameReference> {
        return webClient.get()
            .uri("/v1/clubs/${clubId}/games?offset=${offset}&limit=${limit}")
            .retrieve()
            .bodyToFlux(PolemicaClient.PolemicaClubGameReference::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get games from club error")
    }

    override fun getCompetitions(): List<PolemicaClient.PolemicaCompetition> {
        return webClient.get()
            .uri("/v1/competitions")
            .retrieve()
            .bodyToFlux(PolemicaClient.PolemicaCompetition::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get competitions error")
    }

    override fun getCompetition(id: Long): PolemicaClient.PolemicaCompetition {
        return webClient.get()
            .uri("/v1/competitions/${id}")
            .retrieve()
            .bodyToMono(PolemicaClient.PolemicaCompetition::class.java)
            .block() ?: throw RuntimeException("Get competition error")
    }

    override fun getGamesFromCompetition(id: Long): List<PolemicaClient.PolemicaTournamentGameReference> {
        return webClient.get()
            .uri("/v1/competitions/${id}/games")
            .retrieve()
            .bodyToFlux(PolemicaClient.PolemicaTournamentGameReference::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get games from competition error")
    }

    override fun getGameFromCompetition(polemicaCompetitionGameId: PolemicaClient.PolemicaCompetitionGameId): PolemicaGame {
        return webClient.get()
            .uri(
                "/v1/competitions/${polemicaCompetitionGameId.competitionId}/games/${polemicaCompetitionGameId.gameId}"
                    + getVersionQueryParam(polemicaCompetitionGameId.version)
            )
            .retrieve()
            .bodyToMono(PolemicaGame::class.java)
            .block() ?: throw RuntimeException("Get game from competition error")
    }

    override fun getCompetitionMembers(id: Long): List<PolemicaClient.PolemicaCompetitionMember> {
        return webClient.get()
            .uri("/v1/competitions/${id}/members")
            .retrieve()
            .bodyToFlux(PolemicaClient.PolemicaCompetitionMember::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get competitions error")
    }

    override fun getCompetitionAdmins(id: Long): List<PolemicaClient.PolemicaCompetitionAdmin> {
        return webClient.get()
            .uri("/v1/competitions/${id}/admins")
            .retrieve()
            .bodyToFlux(PolemicaClient.PolemicaCompetitionAdmin::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get competitions error")
    }


    override fun getCompetitionResultMetrics(
        id: Long,
        scoringType: Int?
    ): List<PolemicaClient.CompetitionPlayerResult> {
        return webClient.get()
            .uri { it.path("/v1/competitions/${id}/metrics").queryParam("scoringType", scoringType).build() }
            .retrieve()
            .bodyToFlux(PolemicaClient.CompetitionPlayerResult::class.java)
            .collectList()
            .block() ?: throw RuntimeException("Get competitions error")
    }

    override fun postGameToCompetition(
        competitionId: Long,
        game: PolemicaGame
    ) {
        webClient.post()
            .uri("/v1/competitions/${competitionId}/games")
            .header("Content-Type", "application/json")
            .bodyValue(game)
            .retrieve()
            .toBodilessEntity()
            .block() ?: throw RuntimeException("Get game from competition error")
    }

    private fun addAuthorizationHeader(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val token = getValidToken()
        val filteredRequest = ClientRequest.from(request)
            .header("Authorization", "Bearer $token")
            .build()

        return next.exchange(filteredRequest).onErrorResume(WebClientResponseException.Unauthorized::class.java) {
            lock.withLock {
                refreshAuthToken()
            }
            val retryRequest = ClientRequest.from(request)
                .header("Authorization", "Bearer ${polemicaToken!!}")
                .build()
            next.exchange(retryRequest)
        }
    }

    private fun isAuthRequest(request: ClientRequest): Boolean {
        return request.url().path.startsWith("/v1/auth/login")
    }

    private fun getValidToken(): String {
        if (polemicaToken.isNullOrEmpty()) {
            refreshAuthToken()
        }
        return polemicaToken!!
    }

    @Synchronized
    fun refreshAuthToken() {
        val authData = AuthData(polemicaUsername, polemicaPassword)
        polemicaToken = webClient.post()
            .uri("/v1/auth/login")
            .bodyValue(authData)
            .retrieve()
            .bodyToMono(TokenData::class.java)
            .block()
            ?.token ?: throw RuntimeException("Failed to refresh auth token")
    }

    private data class AuthData(val username: String, val password: String)
    private data class TokenData(@JsonProperty("access_token") val token: String)
}
