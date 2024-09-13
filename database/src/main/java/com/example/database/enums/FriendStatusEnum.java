package com.example.database.enums;

public enum FriendStatusEnum {
    NORMAL(0, "正常"),  // 正常
    BLACKLIST(1, "拉黑"), // 拉黑
    BLOCK(2, "屏蔽"); // 屏蔽 待定

    private final int statusCode;
    private final String desc;

    FriendStatusEnum(int statusCode, String desc) {
        this.statusCode = statusCode;
        this.desc = desc;
    }

    // 根据状态码获取枚举实例
    public static FriendStatusEnum fromStatusCode(int statusCode) {
        for (FriendStatusEnum status : FriendStatusEnum.values()) {
            if (status.getStatusCode() == statusCode) {
                return status;
            }
        }

        //throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        return NORMAL;
    }

    public static String getDescFromStatusCode(String desc) {
        for (FriendStatusEnum status : FriendStatusEnum.values()) {
            if (status.getDesc().equals(desc)) {
                return status.getDesc();
            }
        }

        //throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        return NORMAL.getDesc();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDesc() {
        return desc;
    }
}
