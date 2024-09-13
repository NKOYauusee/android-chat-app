package com.example.database.enums;

import androidx.annotation.NonNull;

// 申请处理状态枚举
public enum ApplyStatusEnum {
    PENDING(0, "未同意"),  // 状态 0: 未同意
    APPROVED(1, "已同意"), // 状态 1: 已同意
    REJECTED(2, "已拒绝");   // 状态 2: 拒绝

    // 获取状态码
    private final int code;
    // 获取状态描述
    private final String description;

    // 构造函数
    ApplyStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 根据状态码获取枚举实例
    public static ApplyStatusEnum fromCode(int code) {
        for (ApplyStatusEnum status : ApplyStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }

        return PENDING;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @NonNull
    @Override
    public String toString() {
        return description;
    }
}
