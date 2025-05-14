package prj.yong.common.provider;

import java.util.List;

public interface CommonWhiteIpListProvider {
    List<String> getWhiteIpList(String requestPath);
}
