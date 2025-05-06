package prj.yong.common.util;

import java.net.InetAddress;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IpUtility {
    private boolean isAllowIpCidrRange(String whiteIpAddress, String clientIpAddress) {
        try {
            int slashIndex = whiteIpAddress.indexOf('/');
            if (slashIndex < 0) {
                return clientIpAddress.equals(whiteIpAddress);
            }

            String subnet = whiteIpAddress.substring(0, slashIndex);
            int prefixLength = Integer.parseInt(whiteIpAddress.substring(slashIndex + 1));

            InetAddress subneInetAddress = InetAddress.getByName(subnet);
            InetAddress clientInetAddress = InetAddress.getByName(clientIpAddress);

            byte[] subnetBytes = subneInetAddress.getAddress();
            byte[] clientBytes = clientInetAddress.getAddress();

            int maskFullBytes = prefixLength / 8;
            byte finalByte = (byte) (0xFF00 >> (prefixLength & 0x07));

            for (int index = 0; index < maskFullBytes; index++) {
                if (clientBytes[index] != subnetBytes[index]) {
                    return false;
                }
            }

            if (finalByte != 0) {
                return (clientBytes[maskFullBytes] & finalByte)
                        == (subnetBytes[maskFullBytes] & finalByte);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllowClientIpAddress(List<String> whiteIpList, String clientIpAddress) {
        for (String whiteIp : whiteIpList) {
            if (isAllowIpCidrRange(whiteIp, clientIpAddress)) {
                return true;
            }
        }
        return false;
    }
}
