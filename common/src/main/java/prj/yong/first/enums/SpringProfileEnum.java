package prj.yong.first.enums;

public enum SpringProfileEnum {
    LOCAL("local"),
    DEVELOP("develop"),
    STAGE("stage"),
    PRODUCTION("production");

    private final String value;

    SpringProfileEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
