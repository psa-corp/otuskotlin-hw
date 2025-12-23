package net.otuskotlin.ingredientscan.scanner.repositories

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Duration

// Имитируем RocksDB
@Repository
open class InMemoryContextRepository {

    private val log = LoggerFactory.getLogger(InMemoryContextRepository::class.java)

    private val store: Cache<String, IsContext> = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterAccess(Duration.ofMinutes(30))
        .build()

    open fun save(key: String, context: IsContext): IsContext {
        store.put(key, context)
        log.info("Context saved/updated for key: $key (State: ${context.state})")
        return context
    }

    open fun findById(id: String): IsContext? {
        return store.getIfPresent(id)
    }

    open fun delete(key: String) {
        store.invalidate(key)
    }

    open fun clear() {
        store.cleanUp()
    }

}