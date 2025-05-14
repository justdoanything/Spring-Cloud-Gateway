package prj.yong.common.filter;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import prj.yong.common.constants.HttpRequestHeaderConstants;
import prj.yong.common.provider.CommonWhiteIpListProvider;
import prj.yong.common.util.IpUtility;

@Slf4j
@Component
public class IpFilter extends AbstractGatewayFilterFactory<IpFilter.IpFilterConfig> {

    public static class IpFilterConfig {}

    private final CommonWhiteIpListProvider commonWhiteIpListProvider;

    public IpFilter(CommonWhiteIpListProvider commonWhiteIpListProvider) {
        super(IpFilter.IpFilterConfig.class);
        this.commonWhiteIpListProvider = commonWhiteIpListProvider;
    }

    @Override
    public GatewayFilter apply(IpFilterConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().value();
            String clientIpAddress =
                    request.getHeaders().getFirst(HttpRequestHeaderConstants.X_FORWARDED_FOR);

            List<String> whiteIpList = commonWhiteIpListProvider.getWhiteIpList(requestPath);

            if (ObjectUtils.isEmpty(clientIpAddress)
                    || ObjectUtils.isEmpty(whiteIpList)
                    || !IpUtility.isAllowClientIpAddress(whiteIpList, clientIpAddress)) {
                log.error(
                        "Client IP address ({}) is not allowed ({}) for request path ({})",
                        clientIpAddress,
                        whiteIpList,
                        requestPath);
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Client IP address is not allowed for this request path");
            }
            return chain.filter(exchange);
        };
    }
}
