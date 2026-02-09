package net.otuskotlin.ingredientscan.biz.common.repo

import net.otuskotlin.ingredientscan.biz.common.exceptions.IsNotConfiguredException
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.helpers.errorSystem
import net.otuskotlin.ingredientscan.core.common.external.helpers.fail
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.worker

@JvmName("initRepoComposition")
fun ICorChainDsl<IsContext>.initRepoComposition(title: String) = worker {
    this.title = title
    description = """
        Вычисление основного рабочего репозитория в зависимости от запрошенного режима работы        
    """.trimIndent()
    handle {
        compositionRepo = settings.compositionRepository
        if (compositionRepo == IsCompositionRepository.NONE) fail(
            errorSystem(
                violationCode = "dbNotConfigured",
                e = IsNotConfiguredException("composition database")
            )
        )
    }
}
@JvmName("initRepoCompositionInternal")
fun ICorChainDsl<InternalContext>.initRepoComposition(title: String) = worker {
    this.title = title
    description = """
        Вычисление основного рабочего репозитория в зависимости от запрошенного режима работы        
    """.trimIndent()
    handle {
        compositionRepo = settings.compositionRepository
        if (compositionRepo == IsCompositionRepository.NONE) fail(
            errorSystem(
                violationCode = "dbNotConfigured",
                e = IsNotConfiguredException("composition database")
            )
        )
    }
}

fun ICorChainDsl<IsContext>.initRepoContext(title: String) = worker {
    this.title = title
    description = """
        Вычисление основного рабочего репозитория в зависимости от запрошенного режима работы        
    """.trimIndent()
    handle {
        contextRepo = settings.contextRepository
        if (contextRepo == IsContextRepository.NONE) fail(
            errorSystem(
                violationCode = "dbNotConfigured",
                e = IsNotConfiguredException("context database")
            )
        )
    }
}

@JvmName("initRepoAnalysis")
fun ICorChainDsl<IsContext>.initRepoAnalysis(title: String) = worker {
    this.title = title
    description = """
        Вычисление основного рабочего репозитория в зависимости от запрошенного режима работы        
    """.trimIndent()
    handle {
        analysisRepo = settings.analysisRepository
        if (analysisRepo == IsAnalysisRepository.NONE) fail(
            errorSystem(
                violationCode = "dbNotConfigured",
                e = IsNotConfiguredException("analysis database")
            )
        )
    }
}

@JvmName("initRepoAnalysisInternal")
fun ICorChainDsl<InternalContext>.initRepoAnalysis(title: String) = worker {
    this.title = title
    description = """
        Вычисление основного рабочего репозитория в зависимости от запрошенного режима работы        
    """.trimIndent()
    handle {
        analysisRepo = settings.analysisRepository
        if (analysisRepo == IsAnalysisRepository.NONE) fail(
            errorSystem(
                violationCode = "dbNotConfigured",
                e = IsNotConfiguredException("analysis database")
            )
        )
    }
}
