package net.otuskotlin.ingredientscan.scanner.repositories

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Duration

// Имитируем RocksDB
@Repository
open class InMemoryContextRepository: IsContextRepository {

    private val log = LoggerFactory.getLogger(InMemoryContextRepository::class.java)

    private val store: Cache<String, IsContext> = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterAccess(Duration.ofMinutes(30))
        .build()

    override fun save(context: IsContext): IsContext {
        store.put(context.id.toString(), context)
        log.info("Context saved/updated for key: ${context.id} (State: ${context.state})")
        return context
    }

    override fun findById(id: String): IsContext? {
        return store.getIfPresent(id)
    }

    override fun delete(key: String) {
        store.invalidate(key)
    }

    override fun clear() {
        store.cleanUp()
    }

}