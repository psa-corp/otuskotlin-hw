package net.otuskotlin.ingredientscan.core.common.external.stubs

import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import java.time.LocalDateTime

open class IsAnalysisStub {

    companion object {
        val STUB_ANALYSIS: IsAnalysis = IsAnalysis(
            id = IsAnalysisId("analysis-test123"),
            compositionId = IsCompositionId("composition-test456"),
            createDate = LocalDateTime.now(),
            description = "Test analysis description",
            rating = 4.5,
            color = IsColor.GREEN,
        )
    }
}