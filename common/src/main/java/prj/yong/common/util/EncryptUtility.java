package prj.yong.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EncryptUtility {
    private final String HMAC_SHA256 = "HmacSHA256";

    public String getSignature(String base, String secret) {
        try {
            Mac hmacSha256Mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), HMAC_SHA256);
            hmacSha256Mac.init(secretKeySpec);
            return byteArrayToHexString(hmacSha256Mac.doFinal(base.getBytes()));
        } catch (Exception ignored) {
        }
        return null;
    }

    public String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        String stmp;

        for (byte b : bytes) {
            stmp = Integer.toHexString(b & 0xFF);
            if (stmp.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(stmp);
        }
        return stringBuilder.toString().toLowerCase();
    }
}
