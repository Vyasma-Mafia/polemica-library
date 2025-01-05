package com.github.mafia.vyasma.polemica.library.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGame
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGameResult
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaUser
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnum
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumDeserializer
import com.github.mafia.vyasma.polemica.library.utils.enums.IntEnumSerializer
import java.time.LocalDateTime

interface PolemicaClient {

    fun getGameFromClub(clubGameId: PolemicaClubGameId): PolemicaGame
    fun getGamesFromClub(clubId: Long, offset: Long, limit: Long): List<PolemicaClubGameReference>
    fun getCompetitions(): List<PolemicaCompetition>
    fun getGamesFromCompetition(id: Long): List<PolemicaTournamentGameReference>
    fun getGameFromCompetition(polemicaCompetitionGameId: PolemicaCompetitionGameId): PolemicaGame
    fun getCompetitionMembers(id: Long): List<PolemicaCompetitionMember>
    fun getCompetitionResultMetrics(id: Long): List<CompetitionPlayerResult>

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CompetitionPlayerResult(
        val id: Long,
        val username: String,
        val metrics: ResultMetrics
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ResultMetrics(
        val don: Metric,
        val maf: Metric,
        val com: Metric,
        val civ: Metric
    )

    data class Metric(
        val games: Long,
        val wins: Long,
        val totalScores: Double,
        val totalAwards: Double,
        val guessScore: Double,
        val fpr: Long,
        val fouls: Long,
        val ciScores: Double
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PolemicaCompetitionMember(
        val registration: LocalDateTime,
        val status: PolemicaCompetitionMemberStatus,
        val player: PolemicaUser
    )

    @JsonSerialize(using = IntEnumSerializer::class)
    @JsonDeserialize(using = RoleDeserializer::class)
    enum class PolemicaCompetitionMemberStatus(override val value: Int) : IntEnum {
        APPROVED(1),
        NON_APPROVED(2);
    }

    class RoleDeserializer :
        IntEnumDeserializer<PolemicaCompetitionMemberStatus>(PolemicaCompetitionMemberStatus.entries.toTypedArray())


    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PolemicaCompetition(
        val id: Long,
        val name: String,
        val startDate: LocalDateTime?,
        val endDate: LocalDateTime?,
        val region: String?,
        val city: String?,
        val description: String?,
        val link: String?,
        val scoringType: Int?,
        val scoringVersion: String?,
        val showRating: Int?,
        val memberCount: Int?,
        val rating: Int?,
        val phoneRequired: Boolean?,
        val winScores: Int?,
        val hasScores: Boolean?
    )

    data class PolemicaClubGameId(val clubId: Long, val gameId: Long, val version: Long? = null)
    data class PolemicaCompetitionGameId(val competitionId: Long, val gameId: Long, val version: Long? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PolemicaClubGameReference(
        val id: Long,
        val started: LocalDateTime,
        val result: PolemicaGameResult?,
        val referee: PolemicaUser,
        val version: Long?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PolemicaTournamentGameReference(
        val id: Long,
        val num: Long,
        val table: Long,
        val started: LocalDateTime,
        val result: PolemicaGameResult?,
        val referee: PolemicaUser,
        val version: Long?
    )
}
