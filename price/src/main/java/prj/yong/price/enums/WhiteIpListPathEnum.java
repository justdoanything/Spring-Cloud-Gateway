package prj.yong.price.enums;

import java.util.Arrays;
import prj.yong.common.constants.SwaggerConstants;

public enum WhiteIpListPathEnum {
    SAMPLE1_API("/sample1/**"),
    SAMPLE2_API("/sample2/**"),
    SAMPLE3_API("/sample3/**"),

    SAMPLE1_API_DOCS("/sample1/v3/api-docs"),
    SAMPLE2_API_DOCS("/sample2/v3/api-docs"),
    SAMPLE3_API_DOCS("/sample3/v3/api-docs");

    WhiteIpListPathEnum(String path) {
        this.path = path;
    }

    private final String path;

    public String path() {
        return path;
    }

    public static WhiteIpListPathEnum fromPath(String path) {
        if (path.endsWith(SwaggerConstants.END_OF_API_DOCS_URL)) {
            return Arrays.stream(WhiteIpListPathEnum.values())
                    .filter(type -> type.path().endsWith(path))
                    .findFirst()
                    .orElse(null);
        }

        return Arrays.stream(WhiteIpListPathEnum.values())
                .filter(type -> path.matches(type.path().replace("**", ".*")))
                .findFirst()
                .orElse(null);
    }
}
