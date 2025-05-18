package prj.yong.first.filter.global;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        boolean isHealthCheckPath = "/health".equals(request.getURI().getPath());

        if (isHealthCheckPath) {
            return chain.filter(exchange);
        } else {
            final String logHeader =
                    String.format(
                            "%s::%s::%s", request.getId(), request.getMethod(), request.getURI());

            final String requestLogBody =
                    String.format("%-8s >> %s %s", "request", logHeader, request.getHeaders());

            log.debug(requestLogBody);

            this.printAccessLogLikeNginx(exchange);

            return chain.filter(exchange)
                    .doFinally(
                            signalType -> {
                                ServerHttpResponse response = exchange.getResponse();

                                final String responseLogBody =
                                        String.format(
                                                "%-8s >> %s (%s) %s",
                                                "response",
                                                logHeader,
                                                response.getStatusCode(),
                                                response.getHeaders());
                                log.debug(responseLogBody);

                                if (ObjectUtils.isEmpty(response.getStatusCode())
                                        || !response.getStatusCode().is2xxSuccessful()) {
                                    log.error(requestLogBody);
                                    log.error(responseLogBody);
                                }
                            });
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void printAccessLogLikeNginx(ServerWebExchange exchange) {
        log.debug("remote address >>> {}", exchange.getRequest().getRemoteAddress());
        exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous")
                .subscribe(name -> log.debug("remote user >>> {}", name));
        log.debug(
                "time local >>> {}",
                DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z").format(ZonedDateTime.now()));
        log.debug(
                "request >>> {} {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                exchange.getRequest().getHeaders().getFirst("version"));
        log.debug("http refer >>> {}", exchange.getRequest().getHeaders().getFirst("Referer"));
        log.debug(
                "http user agent >>> {}",
                exchange.getRequest().getHeaders().getFirst("User-Agent"));
        log.debug(
                "http x-forwarded-for >>> {}",
                exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"));
    }
}
