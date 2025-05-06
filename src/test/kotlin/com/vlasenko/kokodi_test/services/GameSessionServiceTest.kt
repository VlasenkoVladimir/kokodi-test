package com.vlasenko.kokodi_test.services

import com.vlasenko.kokodi_test.domain.Card
import com.vlasenko.kokodi_test.domain.GameSession
import com.vlasenko.kokodi_test.domain.User
import com.vlasenko.kokodi_test.domain.enums.CardType
import com.vlasenko.kokodi_test.domain.enums.GameStatus
import com.vlasenko.kokodi_test.repository.CardRepository
import com.vlasenko.kokodi_test.repository.GameSessionRepository
import com.vlasenko.kokodi_test.repository.TurnRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class GameSessionServiceTest(

    @Mock
    var userService: UserService,

    @Mock
    var gameSessionRepository: GameSessionRepository,

    @Mock
    var cardRepository: CardRepository,

    @Mock
    var turnRepository: TurnRepository
) {

    var deckMultiplier: Int = 25

    @InjectMocks
    var gameSessionService: GameSessionService = GameSessionService(
        userService,
        gameSessionRepository,
        cardRepository,
        turnRepository,
        deckMultiplier
    )

    @Test
    @DisplayName("Create new game test")
    fun createNew() {

        val savedGameSession = GameSession(
            88L,
            GameStatus.WAIT_FOR_PLAYERS,
            ArrayList(),
            0,
            ArrayList(),
            HashMap(),
            0,
            0
        )

        val user = User()
        user.id = 111L

        whenever(userService.getCurrentUserFromContext()).doReturn(user)
        whenever(gameSessionRepository.save(any())).doReturn(savedGameSession)

        val captor = ArgumentCaptor.forClass(GameSession::class.java)
        val testGameSessionId = gameSessionService.createNew()
        verify(gameSessionRepository).save(captor.capture())
        val capturedSession = captor.value

        assertEquals(88L, testGameSessionId)
        assertEquals(GameStatus.WAIT_FOR_PLAYERS, capturedSession.status)
        assertEquals(true, capturedSession.deck.isEmpty())
        assertEquals(0, capturedSession.turnPointer)
        assertEquals(1, capturedSession.players.size)
        assertEquals(111L, capturedSession.players[0])
        assertEquals(true, capturedSession.scores.isEmpty())
        assertEquals(0L, capturedSession.activeUserId)
        assertEquals(0, capturedSession.skipCounter)
    }

    @Test
    @DisplayName("Join game test")
    fun join() {

        val existedGameSession = GameSession(
            88L,
            GameStatus.WAIT_FOR_PLAYERS,
            ArrayList(),
            0,
            mutableListOf(111L),
            HashMap(),
            0,
            0
        )
        val captor = ArgumentCaptor.forClass(GameSession::class.java)

        val user = User()
        user.id = 222L

        whenever(userService.getCurrentUserFromContext()).doReturn(user)
        whenever(gameSessionRepository.findById(eq(88L))).doReturn(Optional.of(existedGameSession))
        whenever(gameSessionRepository.save(captor.capture())).thenReturn(existedGameSession)

        val testGameSessionId = gameSessionService.join(88L)
        verify(gameSessionRepository).save(captor.capture())
        val capturedSession = captor.value

        assertEquals(88L, testGameSessionId)
        assertEquals(GameStatus.WAIT_FOR_PLAYERS, capturedSession.status)
        assertEquals(true, capturedSession.deck.isEmpty())
        assertEquals(0, capturedSession.turnPointer)
        assertEquals(2, capturedSession.players.size)
        assertEquals(111L, capturedSession.players[0])
        assertEquals(222L, capturedSession.players[1])
        assertEquals(true, capturedSession.scores.isEmpty())
        assertEquals(0L, capturedSession.activeUserId)
        assertEquals(0, capturedSession.skipCounter)
    }

    @Test
    @DisplayName("Start game test")
    fun start() {

        val scores = HashMap<Long, Int>()
        scores.put(111L,0)
        scores.put(222L,0)

        whenever(cardRepository.findAll()).doReturn(listOf(
            Card(1, "Healing Potion", value = 5, type = CardType.POINTS),
            Card(2, "Black Lotus", value = 15, type = CardType.POINTS),
            Card(3, "DoubleDown", value = 2, type = CardType.ACTION),
            Card(4, "Steal", value = 3, type = CardType.ACTION),
            Card(5, "Block", value = 1, type = CardType.ACTION))
        )

        val existedGameSession = GameSession(
            88L,
            GameStatus.WAIT_FOR_PLAYERS,
            gameSessionService.getNewDeck(2),
            0,
            mutableListOf(111L, 222L),
            scores,
            0,
            0
        )

        val captor = ArgumentCaptor.forClass(GameSession::class.java)

        val user = User()
        user.id = 222L

        whenever(userService.getCurrentUserFromContext()).doReturn(user)
        whenever(gameSessionRepository.findById(eq(88L))).doReturn(Optional.of(existedGameSession))
        whenever(gameSessionRepository.save(captor.capture())).thenReturn(existedGameSession)

        val testGameSessionDTO = gameSessionService.start(88L)
        verify(gameSessionRepository).save(captor.capture())
        val capturedSession = captor.value

        assertEquals(88L, testGameSessionDTO.id)
        assertEquals(GameStatus.IN_PROGRESS, capturedSession.status)
        assertEquals(deckMultiplier * 2, capturedSession.deck.size)
        assertEquals(0, capturedSession.turnPointer)
        assertEquals(2, capturedSession.players.size)
        assertEquals(111L, capturedSession.players[0])
        assertEquals(222L, capturedSession.players[1])
        assertEquals(0, capturedSession.scores[111L])
        assertEquals(0, capturedSession.scores[222L])
        assertEquals(111L, capturedSession.activeUserId)
        assertEquals(0, capturedSession.skipCounter)
    }

    @Test
    @DisplayName("Make turn test")
    fun turn() {

        val scores = HashMap<Long, Int>()
        scores.put(111L,0)
        scores.put(222L,0)

        whenever(cardRepository.findAll()).doReturn(listOf(
            Card(1, "Black Lotus", value = 15, type = CardType.POINTS),
            Card(2, "Black Lotus", value = 15, type = CardType.POINTS))
        )

        val existedGameSession = GameSession(
            88L,
            GameStatus.IN_PROGRESS,
            gameSessionService.getNewDeck(2),
            0,
            mutableListOf(111L, 222L),
            scores,
            111L,
            0
        )

        val captor = ArgumentCaptor.forClass(GameSession::class.java)

        val user = User.Builder()
            .withName("test")
            .withUsername("testUsername")
            .withPassword("password")
            .build()
        user.id = 111L

        whenever(userService.getCurrentUserFromContext()).doReturn(user)
        whenever(gameSessionRepository.findById(eq(88L))).doReturn(Optional.of(existedGameSession))
        whenever(gameSessionRepository.save(captor.capture())).thenReturn(existedGameSession)

        val testGameSessionDTO = gameSessionService.turn(88L)

        verify(gameSessionRepository).save(captor.capture())
        val capturedSession = captor.value

        assertEquals(88L, testGameSessionDTO.id)
        assertEquals(GameStatus.IN_PROGRESS, capturedSession.status)
        assertEquals((deckMultiplier * 2), capturedSession.deck.size)
        assertEquals(1, capturedSession.turnPointer)
        assertEquals(2, capturedSession.players.size)
        assertEquals(111L, capturedSession.players[0])
        assertEquals(222L, capturedSession.players[1])
        assertEquals(15, capturedSession.scores[111L])
        assertEquals(0, capturedSession.scores[222L])
        assertEquals(222L, capturedSession.activeUserId)
        assertEquals(0, capturedSession.skipCounter)
    }
}