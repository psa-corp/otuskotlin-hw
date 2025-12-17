package net.otuskotlin.ingredientscan.scanner.repositories

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.models.IsComposition
import java.time.Duration

// Имитируем Elasticsearch
@Repository
class InMemoryCompositionRepository {
    private val log = LoggerFactory.getLogger(InMemoryCompositionRepository::class.java)
    private val store: Cache<String, IsComposition> = Caffeine.newBuilder()
        .maximumSize(20_000)
        .expireAfterWrite(Duration.ofDays(1))
        .build()

    private val textIndex = ConcurrentHashMap<String, String>()

    fun save(composition: IsComposition): IsComposition {
        val id = composition.id.asString()
        val text = composition.text.trim()

        store.put(id, composition)
        textIndex[text] = id

        log.info("Saved composition: $id")
        return composition
    }

    fun findById(id: String): IsComposition? {
        return store.getIfPresent(id)
    }

    fun findByText(text: String): IsComposition? {
        val cleanText = text.trim()

        val id = textIndex[cleanText] ?: return null
        val composition = store.getIfPresent(id)

        if (composition != null) {
            log.info("Found existing composition by text: ${composition.id.asString()}")
        } else {
            textIndex.remove(cleanText)
        }

        return composition
    }
}