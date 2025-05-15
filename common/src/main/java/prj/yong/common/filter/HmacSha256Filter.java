package prj.yong.common.filter;

import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import prj.yong.common.properties.CommonHmacSha256Properties;
import prj.yong.common.util.UriUtility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HmacSha256Filter
        extends AbstractGatewayFilterFactory<HmacSha256Filter.HmacSha256FilterConfig> {
    public static class HmacSha256FilterConfig {}

    private final CommonHmacSha256Properties commonHmacSha256Properties;

    @Setter(value = AccessLevel.PROTECTED)
    private String urlTemplate;

    @Setter(value = AccessLevel.PROTECTED)
    private String shopName;

    public HmacSha256Filter(CommonHmacSha256Properties commonHmacSha256Properties) {
        super(HmacSha256Filter.HmacSha256FilterConfig.class);
        this.commonHmacSha256Properties = commonHmacSha256Properties;
    }

    private Mono<Void> returnUnauthorizedResponseWithMessage(
            ServerWebExchange exchange, String message) {
        log.error("HmacSha256Filter Unauthorized: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private byte[] getBytesFromDateBuffer(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        return bytes;
    }

    @Override
    public GatewayFilter apply(HmacSha256Filter.HmacSha256FilterConfig config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            RequestPath requestPath = request.getPath();
            String requestHeaderAuthorization =
                    request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            long requestHeaderContentLength = request.getHeaders().getContentLength();

            if (StringUtils.isBlank(this.urlTemplate)
                    || StringUtils.isBlank(this.shopName)
                    || StringUtils.isBlank(requestHeaderAuthorization)
                    || requestHeaderContentLength <= 0) {
                return this.returnUnauthorizedResponseWithMessage(
                        exchange, "Required request header is missing.");
            }

            return DataBufferUtils.join(request.getBody())
                    .flatMap(
                            dataBuffer -> {
                                PathPattern.PathMatchInfo pathMatchInfo =
                                        UriUtility.parsingPath(requestPath, urlTemplate);

                                if (pathMatchInfo == null
                                        || ObjectUtils.isEmpty(pathMatchInfo.getUriVariables())
                                        || StringUtils.isBlank(
                                                pathMatchInfo.getUriVariables().get("country"))) {
                                    return this.returnUnauthorizedResponseWithMessage(
                                            exchange, "Required path variable is missing.");
                                }

                                byte[] bodyBytes = this.getBytesFromDateBuffer(dataBuffer);
                                String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
                                String country =
                                        pathMatchInfo
                                                .getUriVariables()
                                                .get("country")
                                                .toLowerCase();
                                String signature =
                                        commonHmacSha256Properties.createSignature(
                                                country, shopName, bodyString);

                                log.debug(
                                        "Proceed to compare request and signature:"
                                                + "\n- Request Path: {}"
                                                + "\n- URL Template: {}"
                                                + "\n- Shop Name: {}"
                                                + "\n- Country: {}"
                                                + "\n- Content Length: {}"
                                                + "\n- Body: {}"
                                                + "\n- Authorization: {}"
                                                + "\n- Signature: {}",
                                        requestPath,
                                        this.urlTemplate,
                                        this.shopName,
                                        country,
                                        requestHeaderContentLength,
                                        bodyString,
                                        requestHeaderAuthorization,
                                        signature);

                                if (!requestHeaderAuthorization.equals(signature)) {
                                    return this.returnUnauthorizedResponseWithMessage(
                                            exchange, "Authorization is different from signature.");
                                }

                                ServerHttpRequest mutateRequest =
                                        new ServerHttpRequestDecorator(request) {
                                            @Override
                                            public @NonNull Flux<DataBuffer> getBody() {
                                                DataBuffer dataBuffer =
                                                        DefaultDataBufferFactory.sharedInstance
                                                                .wrap(bodyBytes);
                                                return Flux.just(dataBuffer);
                                            }
                                        };

                                return chain.filter(
                                        exchange.mutate().request(mutateRequest).build());
                            });
        });
    }
}
