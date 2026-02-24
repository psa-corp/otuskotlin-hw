package net.otuskotlin.ingredientscan.scanner.repositories

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysis
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisId
import net.otuskotlin.ingredientscan.core.common.external.models.IsAnalysisRepository
import net.otuskotlin.ingredientscan.core.common.external.models.IsColor
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.mappers.commonListComponentsDeserialize
import net.otuskotlin.ingredientscan.core.common.mappers.commonListComponentsSerialize
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository("postgresAnalysisRepo")
open class PostgresAnalysisRepository(
    private val db: DatabaseClient
) : IsAnalysisRepository {

    private val log = LoggerFactory.getLogger(PostgresAnalysisRepository::class.java)

    override suspend fun saveAnalysis(analysis: IsAnalysis) {
        val components = commonListComponentsSerialize(analysis.components)


        val sql = """
            INSERT INTO analysis (
                id, composition_id, create_date, description, rating, color,
                components
            ) VALUES (
                :id, :compositionId, :createDate, :description, :rating, :color,
                cast(:components AS jsonb)
            )
            ON CONFLICT (id) DO UPDATE SET
                composition_id = EXCLUDED.composition_id,
                create_date = EXCLUDED.create_date,
                description = EXCLUDED.description,
                rating = EXCLUDED.rating,
                color = EXCLUDED.color,
                components = EXCLUDED.components
        """.trimIndent()

        db.sql(sql)
            .bind("id", analysis.id.asString())
            .bind("compositionId", analysis.compositionId.asString())
            .bind("createDate", analysis.createDate)
            .bind("description", prepareDescription(analysis.description))
            .bind("rating", analysis.rating)
            .bind("color", analysis.color.name)
            .bind("components", components)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
        log.info("Saved analysis: ${analysis.id}")
    }

    override suspend fun findAnalysisById(id: IsAnalysisId): IsAnalysis? {
        val sql = """
            SELECT id, composition_id, create_date, description, rating, color,
                   components
            FROM analysis
            WHERE id = :id
        """.trimIndent()
        return db.sql(sql)
            .bind("id", id.asString())
            .map { row, _ -> mapToAnalysis(row) }
            .first()
            .asFlow()
            .firstOrNull()
    }

    override suspend fun findAnalysisByCompositionId(id: IsCompositionId): IsAnalysis? {
        val sql = """
            SELECT id, composition_id, create_date, description, rating, color,
                   components
            FROM analysis
            WHERE composition_id = :compositionId
        """.trimIndent()
        return db.sql(sql)
            .bind("compositionId", id.asString())
            .map { row, _ -> mapToAnalysis(row) }
            .first()
            .asFlow()
            .firstOrNull()
    }

    override suspend fun updateAnalysis(analysis: IsAnalysis) = saveAnalysis(analysis)

    override suspend fun deleteAnalysis(id: IsAnalysisId) {
        db.sql("DELETE FROM analysis WHERE id = :id")
            .bind("id", id.asString())
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
        log.info("Deleted analysis: $id")
    }

    override suspend fun clearAnalysis() {
        db.sql("DELETE FROM analysis").fetch().rowsUpdated().awaitFirstOrNull()
        log.info("Cleared all analysis")
    }

    private fun mapToAnalysis(row: Row): IsAnalysis {
        val id = IsAnalysisId(row.get("id", String::class.java)!!)
        val compositionId = IsCompositionId(row.get("composition_id", String::class.java)!!)
        val createDate = row.get("create_date", LocalDateTime::class.java)!!
        val description = prepareDescription(row.get("description", String::class.java) ?: "")
        val rating = row.get("rating", Double::class.java) ?: -1.0
        val color = row.get("color", String::class.java)?.let { IsColor.valueOf(it) } ?: IsColor.NONE

        val componentsJson = row.get("components", String::class.java) ?: "[]"

        val components = commonListComponentsDeserialize(componentsJson).toMutableList()


        return IsAnalysis(
            id = id,
            compositionId = compositionId,
            createDate = createDate,
            description = description,
            rating = rating,
            color = color,
            components = components,
        )
    }

    fun prepareDescription(description: String): String {
        return description.replace("\\\\n", "\\n")
    }
}
