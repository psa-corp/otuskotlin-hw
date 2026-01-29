package net.otuskotlin.ingredientscan.scanner.repositories

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
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

    override suspend fun save(context: IsContext) {
        store.put(context.id.asString(), context)
    }

    override suspend fun findById(id: IsContextId): IsContext? {
        return store.getIfPresent(id.asString())
    }

    override suspend fun delete(id: IsContextId) {
        store.invalidate(id.asString())
    }

    override suspend fun clear() {
        store.cleanUp()
    }

    override fun saveUnsuspend(context: IsContext) {
        store.put(context.id.asString(), context)
    }

    override fun findByIdUnsuspend(id: IsContextId): IsContext? {
        return store.getIfPresent(id.asString())
    }
}