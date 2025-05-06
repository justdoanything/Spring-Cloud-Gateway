package prj.yong.common.enums;

public enum SpringProfileEnum {
    LOCAL("local"),
    DEVELOP("devvelop"),
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
