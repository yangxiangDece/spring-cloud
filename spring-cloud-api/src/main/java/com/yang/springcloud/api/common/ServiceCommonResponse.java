package com.yang.springcloud.api.common;

import com.yang.springcloud.api.enums.ResponseStatusEnum;

import java.io.Serializable;
import java.util.List;

/**
*
* @description 服务之间调用后的返回对象
* @author YangXiang
* @time 2018/12/8 10:53
*
*/
public class ServiceCommonResponse implements Serializable {
    private static final long serialVersionUID = -499495915530428944L;

    private int code;

    private String message;

    private List<?> data;

    public ServiceCommonResponse() {
        this.code = ResponseStatusEnum.SUCCESS.getCode();
        this.message = ResponseStatusEnum.SUCCESS.getMsg();
    }

    public ServiceCommonResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceCommonResponse(int code, String message, List<?> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
