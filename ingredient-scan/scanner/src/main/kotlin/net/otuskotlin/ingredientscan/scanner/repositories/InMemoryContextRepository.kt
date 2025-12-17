package net.otuskotlin.ingredientscan.scanner.repositories

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Duration

// Имитируем RocksDB
@Repository
class InMemoryContextRepository {

    private val log = LoggerFactory.getLogger(InMemoryContextRepository::class.java)

    private val contextCache: Cache<String, IsContext> = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterAccess(Duration.ofMinutes(30))
        .build()

    fun save(key: String, context: IsContext): IsContext {
        contextCache.put(key, context)
        log.info("Context saved/updated for key: $key (State: ${context.state})")
        return context
    }

    fun findByKey(key: String): IsContext? {
        return contextCache.getIfPresent(key)
    }

    fun delete(key: String) {
        contextCache.invalidate(key)
    }
}