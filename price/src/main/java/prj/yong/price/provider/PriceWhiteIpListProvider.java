package prj.yong.price.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import prj.yong.common.constants.SwaggerConstants;
import prj.yong.common.enums.SpringProfileEnum;
import prj.yong.common.provider.CommonWhiteIpListProvider;
import prj.yong.price.enums.WhiteIpListPathEnum;

@Slf4j
@Component
public class PriceWhiteIpListProvider implements CommonWhiteIpListProvider {
    @Value("${spring.config.activate.on-profile}")
    private String activeProfile;

    @Value("${filter.module-test-server.ip:#{null}}")
    private List<String> moduleTestServerIp;

    private final Map<WhiteIpListPathEnum, List<String>> whiteIpListMap = new ConcurrentHashMap<>();

    private void addAllowWhiteIpListToWhiteIpListPathEnum(
            WhiteIpListPathEnum[] whiteIpListPathEnums, List<String> whiteIpList) {
        Arrays.stream(whiteIpListPathEnums)
                .forEach(
                        whiteIpListPathEnum ->
                                whiteIpListMap.put(whiteIpListPathEnum, whiteIpList));
    }

    private void addWhiteIpList() {
        WhiteIpListPathEnum[] whiteIpListSamples = {
            WhiteIpListPathEnum.SAMPLE1_API,
            WhiteIpListPathEnum.SAMPLE2_API,
            WhiteIpListPathEnum.SAMPLE3_API
        };

        WhiteIpListPathEnum[] whiteIpListSamplesDocs = {
            WhiteIpListPathEnum.SAMPLE1_API_DOCS,
            WhiteIpListPathEnum.SAMPLE2_API_DOCS,
            WhiteIpListPathEnum.SAMPLE3_API_DOCS
        };

        if (moduleTestServerIp == null) moduleTestServerIp = List.of();

        if (activeProfile.equalsIgnoreCase(SpringProfileEnum.LOCAL.value())
                || activeProfile.equalsIgnoreCase(SpringProfileEnum.DEVELOP.value())) {
            this.addAllowWhiteIpListToWhiteIpListPathEnum(
                    whiteIpListSamples,
                    Stream.concat(
                                    moduleTestServerIp.stream(),
                                    SwaggerConstants.whiteIpListForDevelopers.stream())
                            .toList());

            this.addAllowWhiteIpListToWhiteIpListPathEnum(
                    whiteIpListSamplesDocs,
                    Stream.concat(
                                    SwaggerConstants.whiteIpListForSwagger.stream(),
                                    SwaggerConstants.whiteIpListForDevelopers.stream())
                            .toList());

        } else if (activeProfile.equalsIgnoreCase(SpringProfileEnum.STAGE.value())) {
            this.addAllowWhiteIpListToWhiteIpListPathEnum(whiteIpListSamples, moduleTestServerIp);

            this.addAllowWhiteIpListToWhiteIpListPathEnum(
                    whiteIpListSamplesDocs, SwaggerConstants.whiteIpListForDevelopers);

        } else if (activeProfile.equalsIgnoreCase(SpringProfileEnum.PRODUCTION.value())) {
            this.addAllowWhiteIpListToWhiteIpListPathEnum(
                    whiteIpListSamples, SwaggerConstants.whiteIpListForSwagger);
        }
    }

    private void initialWhiteIpListMap() {
        this.addWhiteIpList();
    }

    @Override
    public List<String> getWhiteIpListByPath(String path) {
        if (ObjectUtils.isEmpty(whiteIpListMap)) this.initialWhiteIpListMap();

        WhiteIpListPathEnum whiteIpListPathEnum = WhiteIpListPathEnum.fromPath(path);
        if (whiteIpListPathEnum == null) {
            log.error("There is no matched white ip list for path: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed IP.");
        }
        return whiteIpListMap.get(whiteIpListPathEnum);
    }
}
