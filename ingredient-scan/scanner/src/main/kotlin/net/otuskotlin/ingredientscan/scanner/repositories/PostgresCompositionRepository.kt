package net.otuskotlin.ingredientscan.scanner.repositories

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository("postgresCompositionRepo")
open class PostgresCompositionRepository(
    private val db: DatabaseClient
) : IsCompositionRepository {

    private val log = LoggerFactory.getLogger(PostgresCompositionRepository::class.java)

    override suspend fun save(composition: IsComposition) {
        val sql = """
            INSERT INTO compositions (id, text, create_date)
            VALUES (:id, :text, :createDate)
            ON CONFLICT (id) DO UPDATE SET
                text = EXCLUDED.text,
                create_date = EXCLUDED.create_date
        """.trimIndent()

        db.sql(sql)
            .bind("id", composition.id.asString())
            .bind("text", composition.text)
            .bind("createDate", composition.createDate)
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
        log.info("Saved composition: ${composition.id}")
    }

    override suspend fun findById(id: IsCompositionId): IsComposition? {
        val sql = "SELECT id, text, create_date FROM compositions WHERE id = :id"
        return db.sql(sql)
            .bind("id", id.asString())
            .map { row, _ -> mapToComposition(row) }
            .first()
            .asFlow()
            .firstOrNull()
    }

    override suspend fun findByText(text: String): IsComposition? {
        val sql = "SELECT id, text, create_date FROM compositions WHERE text = :text"
        return db.sql(sql)
            .bind("text", text)
            .map { row, _ -> mapToComposition(row) }
            .first()
            .asFlow()
            .firstOrNull()
    }

    override suspend fun delete(id: IsCompositionId) {
        db.sql("DELETE FROM compositions WHERE id = :id")
            .bind("id", id.asString())
            .fetch()
            .rowsUpdated()
            .awaitFirstOrNull()
        log.info("Deleted composition: $id")
    }

    override suspend fun clear() {
        db.sql("DELETE FROM compositions").fetch().rowsUpdated().awaitFirstOrNull()
        log.info("Cleared all compositions")
    }

    private fun mapToComposition(row: Row): IsComposition = IsComposition(
        id = IsCompositionId(row.get("id", String::class.java)!!),
        text = row.get("text", String::class.java)!!,
        createDate = row.get("create_date", LocalDateTime::class.java)!!
    )
}