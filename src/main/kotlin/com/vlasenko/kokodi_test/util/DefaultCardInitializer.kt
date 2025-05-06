package com.vlasenko.kokodi_test.util

import com.vlasenko.kokodi_test.domain.Card
import com.vlasenko.kokodi_test.domain.enums.CardType
import com.vlasenko.kokodi_test.repository.CardRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Component that add default cards into CardRepository
 */

@Component
class DefaultCardInitializer(private val cardRepository: CardRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        initDefaultCards()
    }

    private fun initDefaultCards() {
        val cards = listOf(
            Card(null, "Healing Potion", value = 5, type = CardType.POINTS),
            Card(null, "Greater Healing Potion", value = 10, type = CardType.POINTS),
            Card(null, "Black Lotus", value = 15, type = CardType.POINTS),
            Card(null, "COVID-2019", value = 15, type = CardType.ACTION),
            Card(null, "DoubleDown", value = 2, type = CardType.ACTION),
            Card(null, "Steal", value = 3, type = CardType.ACTION),
            Card(null, "Block", value = 1, type = CardType.ACTION),
            Card(null, "Double Block", value = 2, type = CardType.ACTION)
        )

        cardRepository.saveAll(cards)
    }
}