package prj.yong.first.properties;

public interface CommonHmacSha256Properties {
    record ApplicationKeyConfig(String applicationKey, String applicationSecret) {}

    String createSignature(String category, String type, String bodyString);
}
