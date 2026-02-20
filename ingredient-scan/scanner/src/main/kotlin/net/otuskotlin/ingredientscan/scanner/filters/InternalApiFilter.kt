package net.otuskotlin.ingredientscan.scanner.filters

import net.otuskotlin.ingredientscan.scanner.configs.InternalSecurityProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange

@Component
class InternalApiFilter(
    private val securityProps: InternalSecurityProperties
) : CoWebFilter() {

    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        val path = exchange.request.path.value()

        if (path.startsWith(securityProps.prefix)) {
            val authHeader = exchange.request.headers.getFirst(securityProps.header)

            if (authHeader != securityProps.token) {
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                return
            }
        }
        chain.filter(exchange)
    }
}
