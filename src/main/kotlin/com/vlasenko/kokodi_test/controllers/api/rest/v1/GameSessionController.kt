package com.vlasenko.kokodi_test.controllers.api.rest.v1

import com.vlasenko.kokodi_test.dto.GameSessionStatus
import com.vlasenko.kokodi_test.dto.TurnStatus
import com.vlasenko.kokodi_test.services.GameSessionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Endpoints for game actions
 */

@RestController
@RequestMapping("/games")
class GameSessionController(

    private val gameService: GameSessionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(): Long =
        gameService.createNew()

    @PostMapping("/{gameId}/join")
    fun join(@PathVariable gameId: Long): Long =
        gameService.join(gameId)

    @PostMapping("/{gameId}/start")
    fun start(@PathVariable gameId: Long): TurnStatus =
        gameService.start(gameId)

    @PostMapping("/{gameId}/turn")
    fun turn(@PathVariable gameId: Long): TurnStatus =
        gameService.turn(gameId)

    @GetMapping(path = ["/{gameId}"])
    fun statusById(@PathVariable gameId: Long): GameSessionStatus =
        gameService.getGameSessionStatus(gameId)
}