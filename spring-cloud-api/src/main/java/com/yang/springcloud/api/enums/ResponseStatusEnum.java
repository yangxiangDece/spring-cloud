package com.yang.springcloud.api.enums;

public enum ResponseStatusEnum {

    SUCCESS(1000,"Success","调用成功"),

    FAIL(1001,"Fail","调用失败"),

    ERROR(1000,"Server internal error","服务器内部错误");

    private int code;
    private String msg;
    private String desc;

    ResponseStatusEnum(int code, String msg, String desc) {
        this.code = code;
        this.msg = msg;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDesc() {
        return desc;
    }
}
