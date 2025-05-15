package prj.yong.common.constants;

import java.util.List;

public class SwaggerConstants {
    // TODO : SwaggerWebFilter를 통과 시킬 IP를 등록합니다. (CIDR 사용 가능)
    public static List<String> whiteIpListForSwagger =
            List.of("127.0.0.1/32", "127.0.0.1/16", "127.0.0.1");

    // TODO : 개발자들의 IP를 등록합니다. (CIDR 사용 가능)
    public static List<String> whiteIpListForDevelopers =
            List.of("127.0.0.1/32", "127.0.0.1/16", "127.0.0.1");

    public static String END_OF_API_DOCS_URL = "/v3/api-docs";
}
