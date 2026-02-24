package net.otuskotlin.ingredientscan.core.common.external.stubs

import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsError
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import java.time.LocalDateTime

class IsCompositionStub {
    companion object {
        val STUB_COMPOSITION: IsComposition = IsComposition(
            id = IsCompositionId("composition-test456"),
            createDate = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            text = "молоко, сахар, консервант E202",
        )

        val STUB_COMPOSITION_CONTEXT_FINISHING: IsCompositionContext = IsCompositionContext(
            id = IsContextId("context-5678"),
            state = IsState.FINISHING,
            errors = mutableListOf(),
            timeStart = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            composition = STUB_COMPOSITION
        )

        val STUB_COMPOSITION_CONTEXT_FAILING: IsCompositionContext = IsCompositionContext(
            id = IsContextId("context-error"),
            state = IsState.FAILING,
            errors = mutableListOf(
                IsError(
                    code = "not_found",
                    group = "db",
                    field = "contextId",
                    message = "Context not found in database",
                )
            ),
            timeStart = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            composition = IsComposition(),
        )

        val STUB_COMPOSITION_COLA_DOBRA: IsComposition = IsComposition(
            id = IsCompositionId("composition-dobra456"),
            createDate = LocalDateTime.of(2026, 2, 24, 0, 0, 0),
            text = "Очищенная вода, краситель сахарный колер IV, регуляторы кислотности (ортофосфорная кислота, цитрат натрия 3-замещенный), подсластители (натриевая соль цикламовой кислоты, ацесульфам калия, аспартам), натуральный ароматизатор, кофеин (менее 150 мг/л). Содержит источник фенилаланина.",
        )
    }

}