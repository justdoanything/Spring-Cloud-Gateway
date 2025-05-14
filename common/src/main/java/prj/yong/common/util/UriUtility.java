package prj.yong.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@UtilityClass
public class UriUtility {
    public PathPattern.PathMatchInfo parsingPath(PathContainer pathContainer, String PathTemplate) {
        PathPatternParser pathPatternParser = new PathPatternParser();
        PathPattern pathPattern = pathPatternParser.parse(PathTemplate);
        return pathPattern.matchAndExtract(pathContainer);
    }
}
