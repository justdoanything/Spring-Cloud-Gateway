package prj.yong.common.provider;

import java.util.List;

public interface CommonWhiteIpListProvider {
    List<String> getWhiteIpListByPath(String requestPath);
}
