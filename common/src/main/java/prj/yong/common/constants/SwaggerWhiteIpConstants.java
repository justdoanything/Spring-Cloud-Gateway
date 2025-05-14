package prj.yong.common.constants;

import java.util.List;

public class SwaggerWhiteIpConstants {
    // TODO : SwaggerWebFilter를 통과시킬 IP를 등록합니다. (CIDR 사용 가능)
    public static List<String> swaggerWhiteIpList =
            List.of("127.0.0.1/32", "127.0.0.1/16", "127.0.0.1");
}
