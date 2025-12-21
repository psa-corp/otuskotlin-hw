package net.otuskotlin.ingredientscan.core.common.external.stubs

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.LOCAL_DATE_TIME_NONE
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
            id = IsCompositionId("comp-test-456"),
            createDate = LocalDateTime.now(),
            text = "молоко, сахар, консервант E202",
        )


        val STUB_COMPOSITION_CONTEXT_FINISHING: IsCompositionContext = IsCompositionContext(
            id = IsContextId("context_5678"),
            state = IsState.FINISHING,
            errors = mutableListOf(),
            timeStart = LocalDateTime.of(2025, 12, 18, 12, 0, 0),
            composition = STUB_COMPOSITION
        )

        val STUB_COMPOSITION_CONTEXT_FAILING: IsCompositionContext = IsCompositionContext(
            id = IsContextId("context_error"),
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
    }

}