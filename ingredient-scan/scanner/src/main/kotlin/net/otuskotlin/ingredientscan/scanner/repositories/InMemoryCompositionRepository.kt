package net.otuskotlin.ingredientscan.scanner.repositories

import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionId
import net.otuskotlin.ingredientscan.core.common.external.models.IsCompositionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

// Имитируем Elasticsearch или PostgreSQL
@Repository
open class InMemoryCompositionRepository : IsCompositionRepository {
    private val log = LoggerFactory.getLogger(InMemoryCompositionRepository::class.java)
    private val store = ConcurrentHashMap<IsCompositionId, IsComposition>()

    private val textStore = ConcurrentHashMap<String, IsComposition>()

    override suspend fun save(composition: IsComposition) {
        val text = composition.text.trim()
        store[composition.id] = composition
        textStore[text] = composition

        log.info("Saved composition: ${composition.id}")
    }

    override fun saveUnsuspend(composition: IsComposition) {
        val text = composition.text.trim()
        store[composition.id] = composition
        textStore[text] = composition

        log.info("Saved composition: ${composition.id}")
    }

    override suspend fun findById(id: IsCompositionId): IsComposition? {
        return store[id]
    }

    override suspend fun findByText(text: String): IsComposition? {
        val cleanText = text.trim()
        return textStore[cleanText]
    }

    override fun findByTextUnsuspend(text: String): IsComposition? {
        val cleanText = text.trim()
        return textStore[cleanText]
    }

    override suspend fun delete(id: IsCompositionId) {
        val comp = store[id]
        if (comp != null) {
            store.remove(id)
            val cleanText = comp.text.trim()
            textStore.remove(cleanText)
            log.info("Delete analysis: ${comp.id}")
        }
    }

    override suspend fun clear() {
        store.clear()
        textStore.clear()
    }
}