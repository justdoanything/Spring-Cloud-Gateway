package prj.yong.first.configuration;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import prj.yong.first.properties.CommonHmacSha256Properties;
import prj.yong.first.provider.CommonWhiteIpListProvider;

@Configuration
public class CommonConfiguration {
    @Bean
    @ConditionalOnMissingBean(CommonHmacSha256Properties.class)
    public CommonHmacSha256Properties commonHmacSha256Properties() {
        return (country, shop, bodyString) -> StringUtils.EMPTY;
    }

    @Bean
    @ConditionalOnMissingBean(CommonWhiteIpListProvider.class)
    public CommonWhiteIpListProvider commonWhiteIpListProvider() {
        return requestPath -> List.of();
    }
}
