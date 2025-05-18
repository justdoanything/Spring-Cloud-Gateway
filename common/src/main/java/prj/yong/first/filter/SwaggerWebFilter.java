package prj.yong.first.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import prj.yong.first.constants.HttpRequestHeaderConstants;
import prj.yong.first.constants.SwaggerConstants;
import prj.yong.first.util.IpUtility;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class SwaggerWebFilter {
    @Bean
    public WebFilter swaggerFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().value();

            boolean isSwaggerPath =
                    requestPath.startsWith("/swagger-ui/")
                            || requestPath.startsWith("/swagger-resources/")
                            || requestPath.startsWith("/webjars/swagger-ui/")
                            || requestPath.startsWith("/v3/api-docs/");

            if (isSwaggerPath) {
                String clientIpAddress =
                        request.getHeaders().getFirst(HttpRequestHeaderConstants.X_FORWARDED_FOR);

                if (ObjectUtils.isEmpty(clientIpAddress)
                        || IpUtility.isAllowClientIpAddress(
                                SwaggerConstants.whiteIpListForSwagger, clientIpAddress)) {
                    log.error(
                            "Blocked Swagger access from IP address ({}) to Swagger UI: {}",
                            clientIpAddress,
                            requestPath);
                    return this.handleSwaggerUnauthorizedAccess(exchange);
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> handleSwaggerUnauthorizedAccess(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

        response.setStatusCode(httpStatus);

        Map<String, String> errorDetails =
                Map.of(
                        "error",
                        httpStatus.getReasonPhrase(),
                        "message",
                        "Swagger access is not allowed.");

        try {
            byte[] errorDetailsBytes = new ObjectMapper().writeValueAsBytes(errorDetails);
            DataBuffer dataBuffer = response.bufferFactory().wrap(errorDetailsBytes);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to serialize error response"));
        }
    }
}
