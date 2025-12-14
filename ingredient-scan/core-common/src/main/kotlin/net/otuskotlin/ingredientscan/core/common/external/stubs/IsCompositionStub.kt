package net.otuskotlin.ingredientscan.core.common.external.stubs

import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import java.time.LocalDateTime

class IsCompositionStub {
    companion object {
        val STUB_COMPOSITION: IsComposition = IsComposition(
            id = IsCompositionId("comp-test-456"),
            createDate = LocalDateTime.now(),
            text = "молоко, сахар, консервант E202",
        )
    }
}