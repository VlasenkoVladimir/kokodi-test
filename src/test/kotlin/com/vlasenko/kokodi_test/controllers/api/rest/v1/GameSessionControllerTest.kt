package com.vlasenko.kokodi_test.controllers.api.rest.v1

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vlasenko.kokodi_test.domain.GameSession
import com.vlasenko.kokodi_test.domain.User
import com.vlasenko.kokodi_test.domain.enums.CardType
import com.vlasenko.kokodi_test.domain.enums.GameStatus
import com.vlasenko.kokodi_test.repository.GameSessionRepository
import com.vlasenko.kokodi_test.repository.UserRepository
import com.vlasenko.kokodi_test.services.GameSessionService
import com.vlasenko.kokodi_test.services.JwtService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameSessionControllerTest(

    @Autowired val passwordEncoder: PasswordEncoder,

    @Autowired val jwtService: JwtService
) {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var gameSessionRepository: GameSessionRepository

    @Autowired
    private lateinit var gameSessionService: GameSessionService

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    private lateinit var token: String
    private lateinit var jwtAuth: RequestPostProcessor

    @BeforeTest
    fun setup() {

        val firstUser = userRepository.save(
            User
                .Builder()
                .withName("user")
                .withUsername("user")
                .withPassword(passwordEncoder.encode("password"))
                .build()
        )

        val secondUser = userRepository.save(
            User
                .Builder()
                .withName("user2")
                .withUsername("user2")
                .withPassword(passwordEncoder.encode("password2"))
                .build()
        )

        token = jwtService.generateToken(firstUser) ?: error("Token generation failed")

        jwtAuth = RequestPostProcessor {
            it.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
            it
        }

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @AfterTest
    fun clearRepo() {

        userRepository.deleteAll()
        gameSessionRepository.deleteAll()
    }

    @Test
    @DisplayName("Create new game test")
    fun create() {

        mockMvc.post("/games") {
            with(jwtAuth)
        }.andExpect {
            status { isCreated() }
            content { string("1") }
        }
    }


    @Test
    @DisplayName("Join to existed game test")
    fun join() {

        val gameSession = GameSession(
            null,
            GameStatus.WAIT_FOR_PLAYERS,
            ArrayList(),
            0,
            mutableListOf(2L),
            HashMap()
        )
        val gameId = gameSessionRepository.save(gameSession).id

        mockMvc.post("/games/$gameId/join")
        {
            with(jwtAuth)
        }
            .andExpect {
                status { isOk() }
                content { gameId }
            }.andReturn()
    }

    @Test
    @DisplayName("Start an existed game test")
    fun start() {

        val firstUserId = userRepository.findByUsername("user").get().id
        val secondUserId = userRepository.findByUsername("user2").get().id

        val gameSession = GameSession(
            null,
            GameStatus.WAIT_FOR_PLAYERS,
            ArrayList(),
            0,
            mutableListOf(firstUserId!!, secondUserId!!),
            mutableMapOf(firstUserId to 0, secondUserId to 0),
            0,
            0
        )

        val gameId = gameSessionRepository.save(gameSession).id

        mockMvc.post("/games/$gameId/start")
        {
            with(jwtAuth)
        }
            .andExpect {
                status { isOk() }
                content { MediaType.APPLICATION_JSON }
                jsonPath("\$.id") { value(gameId) }
                jsonPath("\$.status") { value(GameStatus.IN_PROGRESS.toString()) }
                jsonPath("\$.turnPointer") { value(0) }
                jsonPath("\$.players.[0]") { value(firstUserId) }
                jsonPath("\$.players.[1]") { value(secondUserId) }
                jsonPath("\$.scores['$firstUserId']") { value(0) }
                jsonPath("\$.scores['$secondUserId']") { value(0) }
                jsonPath("\$.activeUserId") { value(firstUserId) }
                jsonPath("\$.skipCounter") { value(0) }
            }.andReturn()
    }

    @Test
    @DisplayName("Make turn existed game test")
    fun turn() {

        val firstUserId = userRepository.findByUsername("user").get().id
        val secondUserId = userRepository.findByUsername("user2").get().id

        var pointsFirstPlayer = 14
        var pointsSecondPlayer = 1
        val turnPointer = 13
        var block = 0

        val gameSession = GameSession(
            null,
            GameStatus.IN_PROGRESS,
            gameSessionService.getNewDeck(2),
            turnPointer,
            mutableListOf(firstUserId!!, secondUserId!!),
            mutableMapOf(firstUserId to pointsFirstPlayer, secondUserId to pointsSecondPlayer),
            firstUserId,
            block
        )

        val game = gameSessionRepository.save(gameSession)
        val gameId = game.id
        val actualCard = game.deck[turnPointer]

        if (actualCard.type == CardType.POINTS) {
            pointsFirstPlayer = (pointsFirstPlayer + actualCard.value)
        } else {
            when (actualCard.name) {
                "COVID-2019" -> {
                    pointsFirstPlayer = 0
                    pointsSecondPlayer = 0
                }

                "DoubleDown" -> {
                    pointsFirstPlayer = 28
                }

                "Steal" -> {
                    pointsFirstPlayer = 15
                    pointsSecondPlayer = 0
                }

                "Double Block" -> {
                    block = 2
                }

                "Block" -> {
                    block = 1
                }
            }
        }

        mockMvc.post("/games/$gameId/turn")
        {
            with(jwtAuth)
        }
            .andExpect {
                status { isOk() }
                content { MediaType.APPLICATION_JSON }
                jsonPath("\$.id") { value(gameId) }
                jsonPath("\$.status") { value(GameStatus.IN_PROGRESS.toString()) }
                jsonPath("\$.turnPointer") { value(turnPointer + 1) }
                jsonPath("\$.players.[0]") { value(firstUserId) }
                jsonPath("\$.players.[1]") { value(secondUserId) }
                jsonPath("\$.scores['$firstUserId']") { value(pointsFirstPlayer) }
                jsonPath("\$.scores['$secondUserId']") { value(pointsSecondPlayer) }
                jsonPath("\$.activeUserId") { value(secondUserId) }
                jsonPath("\$.skipCounter") { value(block) }
            }.andReturn()
    }

    @Test
    @DisplayName("Get existed game test")
    fun statusById() {

        val firstUserId = userRepository.findByUsername("user").get().id
        val secondUserId = userRepository.findByUsername("user2").get().id

        val gameStatus = GameStatus.FINISHED
        val gameTurnPointer = 33
        val gameSkipCounter = 0
        val firstPlayerScore = 7
        val secondPlayerScore = 30
        val gameDeck = gameSessionService.getNewDeck(2)

        val gameSession = GameSession(
            null,
            gameStatus,
            gameDeck,
            gameTurnPointer,
            mutableListOf(firstUserId!!, secondUserId!!),
            mutableMapOf(firstUserId to firstPlayerScore, secondUserId to secondPlayerScore),
            secondUserId,
            gameSkipCounter
        )

        val game = gameSessionRepository.save(gameSession)
        val gameId = game.id

        mockMvc.get("/games/$gameId")
        {
            with(jwtAuth)
        }
            .andExpect {
                status { isOk() }
                content { MediaType.APPLICATION_JSON }
                jsonPath("\$.id") { value(gameId) }
                jsonPath("\$.status") { value(gameStatus.toString()) }
                jsonPath("\$.deck") { jacksonObjectMapper().writeValueAsString(gameDeck) }
                jsonPath("\$.turnPointer") { value(gameTurnPointer) }
                jsonPath("\$.players.[0]") { value(firstUserId) }
                jsonPath("\$.players.[1]") { value(secondUserId) }
                jsonPath("\$.scores['$firstUserId']") { value(firstPlayerScore) }
                jsonPath("\$.scores['$secondUserId']") { value(secondPlayerScore) }
                jsonPath("\$.activeUserId") { value(secondUserId) }
                jsonPath("\$.skipCounter") { value(gameSkipCounter) }
            }.andReturn()
    }
}