package com.vlasenko.kokodi_test.services

import com.vlasenko.kokodi_test.domain.Card
import com.vlasenko.kokodi_test.domain.GameSession
import com.vlasenko.kokodi_test.domain.Turn
import com.vlasenko.kokodi_test.domain.enums.CardType
import com.vlasenko.kokodi_test.domain.enums.GameStatus
import com.vlasenko.kokodi_test.dto.GameSessionStatus
import com.vlasenko.kokodi_test.dto.TurnStatus
import com.vlasenko.kokodi_test.exceptions.GameSessionException
import com.vlasenko.kokodi_test.repository.CardRepository
import com.vlasenko.kokodi_test.repository.GameSessionRepository
import com.vlasenko.kokodi_test.repository.TurnRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * GameSession service
 */

@Service
class GameSessionService(

    private val userService: UserService,

    private val gameSessionRepository: GameSessionRepository,

    private val cardRepository: CardRepository,

    private val turnRepository: TurnRepository,

    @Value("\${app.deck_multiplier}")
    private val deckMultiplier: Int
) {

    /**
     * Create and return new GameSession with initializer id as 1st player
     */
    fun createNew(): Long {

        val firstPlayerId = userService.getCurrentUserFromContext().id

        val gameSession = GameSession(
            null,
            GameStatus.WAIT_FOR_PLAYERS,
            ArrayList(),
            0,
            mutableListOf(firstPlayerId!!),
            HashMap()
        )
        return gameSessionRepository.save(gameSession).id!!
    }

    /**
     * Add player to existed GameSession with status WAIT_FOR_PLAYERS
     */
    fun join(gameSessionId: Long): Long {
        require(gameSessionId > 0) { "gameSessionId must be positive: $gameSessionId is not." }

        val playerId = userService.getCurrentUserFromContext().id
        val gameSession = getGameSession(gameSessionId)

        if (gameSession.status == GameStatus.WAIT_FOR_PLAYERS && gameSession.players.size < 4 && playerId !in gameSession.players) {
            gameSession.players.add(playerId!!)
        }

        return gameSessionRepository.save(gameSession).id!!
    }

    /**
     * Start existed GameSession with 2-4 players or throw GameSessionException
     */
    fun start(gameSessionId: Long): TurnStatus {
        require(gameSessionId > 0) { "SessionId must be positive: $gameSessionId is not." }

        val playerId = userService.getCurrentUserFromContext().id
        val gameSession = getGameSession(gameSessionId)

        if (playerId !in gameSession.players) throw GameSessionException("Not your game")
        if (gameSession.status != GameStatus.WAIT_FOR_PLAYERS)
            throw GameSessionException("Can't start game session with status not WAIT_FOR_PLAYERS")
        if (gameSession.players.size < 2) throw GameSessionException("Need more players")

        gameSession.status = GameStatus.IN_PROGRESS
        gameSession.activeUserId = gameSession.players[0]
        gameSession.deck = getNewDeck(gameSession.players.size)
        gameSession.players.forEach { gameSession.scores.put(it, 0) }

        return gameSessionRepository.save(gameSession).toTurnStatus()
    }

    /**
     * Make turn
     */
    fun turn(gameSessionId: Long): TurnStatus {
        require(gameSessionId > 0) { "gameSessionId must be positive: $gameSessionId is not." }

        val playerId = userService.getCurrentUserFromContext().id
        val gameSession = getGameSession(gameSessionId)

        if (gameSession.status == GameStatus.FINISHED) {
            throw GameSessionException("Game is finished.")
        } else {
            if (playerId !in gameSession.players) throw GameSessionException("Not your game.")
            if (gameSession.activeUserId != playerId) throw GameSessionException("Not your turn.")

            if (gameSession.skipCounter > 0) {
                gameSession.skipCounter--

                turnRepository.save(
                    Turn(
                        null,
                        gameSessionId,
                        gameSession.turnPointer,
                        gameSession.skipCounter,
                        SecurityContextHolder.getContext().authentication.name,
                        null,
                        null,
                        null,
                        null
                    )
                )

                return gameSessionRepository.save(gameSession).toTurnStatus()
            }
        }

        if (gameSession.deck.isEmpty()) {
            throw GameSessionException("Deck is empty. Game cannot continue.")
        }

        val actualCard = gameSession.deck[gameSession.turnPointer]

        if (actualCard.type == CardType.POINTS) {
            gameSession.scores[playerId] = gameSession.scores[playerId]!!.plus(actualCard.value)

            checkWinCondition(gameSession)
        } else {
            when (actualCard.name) {

                "DoubleDown" -> {
                    gameSession.scores[playerId]?.times(2)
                    checkWinCondition(gameSession)
                }

                "Steal" -> {
                    var stealPosition = gameSession.players.indexOf(playerId) + 1

                    if (stealPosition > gameSession.players.lastIndex) stealPosition = 0

                    val robbedPlayerIndex = gameSession.players[stealPosition]
                    val robbedPlayerScores = gameSession.scores[robbedPlayerIndex]
                    val newRobbedPlayerScores = (robbedPlayerScores!! - actualCard.value)

                    if (newRobbedPlayerScores < 0) {
                        gameSession.scores[playerId] = gameSession.scores[playerId]!! + actualCard.value + newRobbedPlayerScores
                        gameSession.scores[robbedPlayerIndex] = 0
                    } else {
                        gameSession.scores[playerId] = gameSession.scores[playerId]!! + actualCard.value
                        gameSession.scores[robbedPlayerIndex] = newRobbedPlayerScores - actualCard.value
                    }

                    checkWinCondition(gameSession)
                }

                "Double Block" -> {
                    gameSession.skipCounter = actualCard.value
                }

                "Block" -> {
                    gameSession.skipCounter = actualCard.value
                }

                "COVID-2019" -> {
                    gameSession.scores.replaceAll { _, value ->
                        (value - actualCard.value).coerceAtLeast(0)
                    }
                }

                else -> {
                    throw GameSessionException("Unknown action card")
                }
            }
        }

        passTheMoveToNextPlayer(gameSession)

        turnRepository.save(
            Turn(
                null,
                gameSessionId,
                gameSession.turnPointer,
                gameSession.skipCounter,
                userService.getCurrentUserFromContext().username!!,
                actualCard.name,
                actualCard.type.toString(),
                actualCard.value,
                gameSession.scores
            )
        )

        return gameSessionRepository.save(gameSession).toTurnStatus()
    }

    /**
     * Generate new deck
     */
    fun getNewDeck(numberOfPlayers: Int): MutableList<Card> {
        require(numberOfPlayers > 1) { "Number of players must be more than 1" }

        val cards = cardRepository.findAll()
        if (cards.isEmpty()) throw GameSessionException("No cards in DB.")

        val deckSize = numberOfPlayers * deckMultiplier

        return getRandomDeck(cards, deckSize)
    }

    /**
     * Shuffle deck
     */
    fun getRandomDeck(cards: List<Card>, size: Int): MutableList<Card> {
        require(cards.isNotEmpty()) { "Card list can't be empty" }

        return if (size <= cards.size) {
            cards.shuffled(Random).take(size).toMutableList()
        } else {
            MutableList(size) { cards.random(Random) }
        }
    }

    /**
     * Get actual GameSession or throw GameSessionException
     */
    fun getGameSession(gameSessionId: Long): GameSession = gameSessionRepository.findById(gameSessionId)
        .orElseThrow { GameSessionException("Game session with ID: $gameSessionId not found") }

    /**
     *Get GameSession of actual GameSession
     */
    fun getGameSessionStatus(gameSessionId: Long): GameSessionStatus = getGameSession(gameSessionId).toGameSessionStatus()

    /**
     * Pass the move to the next player
     */
    fun passTheMoveToNextPlayer(gameSession: GameSession): GameSession {

        if (gameSession.status == GameStatus.FINISHED) return gameSession

        if (gameSession.turnPointer == gameSession.deck.lastIndex) {
            gameSession.status = GameStatus.FINISHED
            return gameSession
        } else {
            gameSession.turnPointer = gameSession.turnPointer + 1
        }

        val currentPlayerIndex = gameSession.players.indexOf(gameSession.activeUserId)

        if (currentPlayerIndex == gameSession.players.lastIndex) {
            gameSession.activeUserId = gameSession.players.first()
        } else {
            gameSession.activeUserId = gameSession.players[currentPlayerIndex + 1]
        }

        return gameSession
    }

    /**
     * Check win conditions
     */
    private fun checkWinCondition(gameSession: GameSession): GameSession {

        gameSession.scores.forEach { _, value ->
            if (value >= 30) gameSession.status = GameStatus.FINISHED
        }

        return gameSession
    }
}