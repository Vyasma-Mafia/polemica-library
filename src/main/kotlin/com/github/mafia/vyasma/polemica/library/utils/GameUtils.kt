package com.github.mafia.vyasma.polemica.library.utils

import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGame
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGameResult
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaGuess
import com.github.mafia.vyasma.polemica.library.model.game.PolemicaPlayer
import com.github.mafia.vyasma.polemica.library.model.game.Position
import com.github.mafia.vyasma.polemica.library.model.game.Role
import com.github.mafia.vyasma.polemica.library.model.game.StageType
import com.github.mafia.vyasma.polemica.library.model.game.ZeroVoting

fun PolemicaGame.getRealComKiller(): Position? {
    if (getFirstKilled()?.let { this.getRole(it) } != Role.SHERIFF) {
        return null
    }
    return this.comKiller ?: this.getDon()
}

fun PolemicaGame.getFirstKilled() =
    this.getKilled(null).filter { it.position != null }.minByOrNull { it.night }?.position

fun PolemicaGame.getKilled(beforeGamePhase: GamePhase? = null): List<KilledPlayer> {
    // all shots victims in nights equal
    return this.shots
        ?.filter { it.night < (beforeGamePhase?.num ?: Int.MAX_VALUE) }
        ?.groupBy { it.night }
        ?.map { (night, shots) ->
            val candidates = shots.map { it.victim }.toSet()

            if (candidates.size == 1) {
                if (beforeGamePhase == null) {
                    if (playersOnTable(
                            GamePhase(
                                night,
                                Phase.NIGHT
                            )
                        ).filter { getRole(it).isBlack() }.size <= shots.size
                    ) {
                        KilledPlayer(candidates.first(), night)
                    } else {
                        KilledPlayer(null, night)
                    }
                } else {
                    KilledPlayer(candidates.first(), night)
                }
            } else {
                KilledPlayer(null, night)
            }
        } ?: listOf()
}

fun PolemicaGame.getDon(): Position {
    return this.players!!.find { it.role == Role.DON }!!.position
}

fun PolemicaGame.getSheriff(): Position {
    return this.players!!.find { it.role == Role.SHERIFF }!!.position
}

fun PolemicaGame.getRole(position: Position): Role {
    return this.players!!.find { it.position == position }!!.role
}

fun PolemicaGame.getFinalVotes(beforeGamePhase: GamePhase?): List<FinalVote> {
    return this.votes
        ?.filter { it.day - 1 < (beforeGamePhase?.num ?: Int.MAX_VALUE) }
        ?.groupBy { it.day }?.map { (day, votes) ->
        if (stage?.day == day && stage.type == StageType.VOTING) {
            return@map emptyList()
        }
        val votesNumMax = votes.mapNotNull { it.num }.max()
        if (votesNumMax == 0) {
            return@map emptyList()
        }
        val realVotes = votes.filter { it.num == votesNumMax }
        val votingResult = realVotes.groupBy { it.candidate } // candidate -> [voters]
        if (votingResult.isEmpty()) {
            emptyList()
        } else if (votingResult.values.count { it.size == votingResult.values.maxOf { it.size } } > 1 && votingResult.size > 1) { // попил
            val convicted = votingResult.keys.toList()
            val expelVoters = votes.filter { it.num == null }
            val notExpelVotersSize = realVotes.size - expelVoters.size
            val expelled = expelVoters.size > notExpelVotersSize
            expelVoters.map { FinalVote(day, it.voter, convicted, expelled) }
        } else {
            val convicted = votingResult.maxBy { it.value.size }.key
            if (day == 1 && zeroVoting?.withBreak() == false) {
                return@map emptyList()
            }
            realVotes
                .map { FinalVote(day, it.voter, listOf(it.candidate), it.candidate == convicted) }
        }
    }?.flatten() ?: listOf()
}

/**
 * Определяет участников голосования по номеру дня и раунда
 * @param day номер дня
 * @param round номер раунда голосования
 * @return список позиций игроков, участвующих в голосовании
 */
fun PolemicaGame.getVotingParticipants(day: Int, round: Int): List<Position> {
    val votes = this.votes ?: return emptyList()

    // Проверка валидности раунда
    if (round <= 0) {
        return emptyList()
    }

    // Для первого раунда - это игроки, выставленные в круге (num=0)
    if (round == 1) {
        return votes
            .filter { it.day == day && it.num == 0 }
            .map { it.candidate }
            .distinct()
    }

    // Для последующих раундов - анализируем результаты предыдущего раунда
    val previousRoundVotes = votes.filter { it.day == day && it.num == round - 1 }

    // Если нет голосов в предыдущем раунде, возвращаем пустой список
    if (previousRoundVotes.isEmpty()) {
        return emptyList()
    }

    // Подсчет голосов за каждого кандидата
    val voteCounts = previousRoundVotes
        .groupBy { it.candidate }
        .mapValues { it.value.size }

    // Находим максимальное количество голосов
    val maxVotes = voteCounts.values.maxOrNull() ?: return emptyList()

    // Получаем кандидатов с максимальным числом голосов
    val topCandidates = voteCounts
        .filter { it.value == maxVotes }
        .keys
        .toList()

    // Если несколько кандидатов с максимальным числом голосов, они участвуют в следующем раунде
    return topCandidates
}


fun ZeroVoting.withBreak() = when (this) {
    ZeroVoting.RESPEECH, ZeroVoting.LIFT_ONLY, ZeroVoting.NONE -> false
}

fun PolemicaGame.playersOnTable(beforeGamePhase: GamePhase? = null): List<Position> {
    return this.players!!.map { it.position }
        .minus(this.getKickedFromTable(beforeGamePhase).map { it.position }.toSet())
}

fun PolemicaGame.playersWithRoles(roles: List<Role>): List<Position> {
    return this.players!!.filter { it.role in roles }.map { it.position }
}

fun PolemicaGame.isRedWin() = result == PolemicaGameResult.RED_WIN
fun PolemicaGame.isBlackWin() = result == PolemicaGameResult.BLACK_WIN

fun PolemicaGame.getBlacksOnTable(): Set<Position> {
    val playersOnTable = playersOnTable().toSet()
    val blacks = playersWithRoles(listOf(Role.MAFIA, Role.DON)).toSet()
    val blackOnTable = playersOnTable.intersect(blacks)
    return blackOnTable
}

fun PolemicaGame.getKickedFromTable(beforeGamePhase: GamePhase? = null): List<KickedPlayer> {
    val killedPlayers = getKilled(beforeGamePhase)
        .filter { it.position != null }
        .map { KickedPlayer(it.position!!, GamePhase(it.night, Phase.NIGHT), KickReason.KILL) }
    val votedPlayers = getFinalVotes(beforeGamePhase)
        .filter { it.expelled }
        .flatMap { finalVote ->
            finalVote.convicted.map {
                KickedPlayer(
                    it,
                    GamePhase(finalVote.day, Phase.DAY),
                    KickReason.VOTING
                )
            }
        }
    val disqualed = players!!.filter { it.disqual != null }
        .filter { it.disqual!!.day <= (beforeGamePhase?.num ?: Int.MAX_VALUE) }
        .map { KickedPlayer(it.position, GamePhase(it.disqual!!.day, Phase.NIGHT), KickReason.DISQUAL) }
    return (killedPlayers + votedPlayers + disqualed).toSet().sortedBy { it.gamePhase }
}

fun PolemicaGame.getCriticDay(): Int? {
    val lastDay = this.stage!!.day + 1
    var red = 7
    var black = 3
    val kickedPlayer = getKickedFromTable().groupBy { it.gamePhase.num }
    for (day in 2..lastDay) {
        for (kick in kickedPlayer[day - 1] ?: listOf()) {
            if (getRole(kick.position).isRed()) {
                red -= 1
            } else {
                black -= 1
            }
        }
        if (red <= black + 2) {
            return day
        }
    }
    return null
}

fun PolemicaGame.getPlayerNumStarted(day: Int): Position {
    if (day == 1) {
        return Position.ONE
    }
    val playersOnTable = playersOnTable(GamePhase(day, Phase.DAY))
    val previousSpeechStarter = getPlayerNumStarted(day - 1)
    if (playersOnTable.any { it > previousSpeechStarter }) {
        return playersOnTable.filter { it > previousSpeechStarter }.min()
    }
    return playersOnTable.min()
}

fun PolemicaGame.getVoteCandidatesOrder(day: Int): List<Position> {
    val allVotes = votes ?: return emptyList()
    val votesByDay = allVotes.filter { it.day == day }

    val firstSpeech = getPlayerNumStarted(day)
    val speechOrder =
        Position.entries.filter { it >= firstSpeech }.sorted() +
            Position.entries.filter { it < firstSpeech }.sorted()

    return votesByDay.takeWhile { it.num == 0 }
        .sortedBy { speechOrder.indexOf(it.voter) }
        .map { it.candidate }
}


inline fun PolemicaGame.check(f: InPolemicaGameContext.() -> Int): Int {
    val context = InPolemicaGameContext(this)
    return try {
        f(context)
    } catch (e: CheckException) {
        0
    }
}

data class InPolemicaGameContext(val game: PolemicaGame) {
    fun Position.role(): Role {
        return game.getRole(this@role)
    }

    fun Position.guess(): PolemicaGuess? {
        return game.players!!.find { it.position == this@guess }?.guess
    }

    fun Position.player(): PolemicaPlayer {
        return game.players!!.find { it.position == this@player }!!
    }

    fun assert(boolean: Boolean) {
        if (boolean) {
            return
        }
        throw CheckException
    }

    fun assert(block: () -> Boolean) {
        assert(block())
    }
}

internal object CheckException : Exception() {
    private fun readResolve(): Any = CheckException
    override fun fillInStackTrace(): Throwable {
        return this
    }
}

data class KilledPlayer(val position: Position?, val night: Int)
data class FinalVote(val day: Int, val position: Position, val convicted: List<Position>, val expelled: Boolean)
data class GamePhase(val num: Int, val phase: Phase) : Comparable<GamePhase> {
    override fun compareTo(other: GamePhase): Int {
        if (num < other.num) return -1
        if (num > other.num) return 1
        return phase.compareTo(other.phase)
    }
}

data class KickedPlayer(val position: Position, val gamePhase: GamePhase, val reason: KickReason)

enum class Phase : Comparable<Phase> {
    DAY, NIGHT
}

enum class KickReason {
    VOTING, KILL, DISQUAL
}
