package net.otuskotlin.ingredientscan.analyzer.repositories

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.otuskotlin.ingredientscan.core.common.external.IsLightContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContextId
import net.otuskotlin.ingredientscan.core.common.external.models.IsLightContextRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Duration

// Имитируем RocksDB
@Repository("memoryLightContextRepo")
open class InMemoryLightContextRepository: IsLightContextRepository {

    private val log = LoggerFactory.getLogger(InMemoryLightContextRepository::class.java)

    private val store: Cache<String, IsLightContext> = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterAccess(Duration.ofHours(2))
        .build()

    override fun save(context: IsLightContext) {
        store.put(context.id.asString(), context)
    }

    override fun findById(id: IsContextId): IsLightContext? {
        return store.getIfPresent(id.asString())
    }

    override fun delete(id: IsContextId) {
        store.invalidate(id.asString())
    }

    override fun clear() {
        store.cleanUp()
    }
}