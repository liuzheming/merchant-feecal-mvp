package com.merchant.feecal.controller.response;

import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * @author : kaerKing
 * @date : 2023/10/19
 */
@Data
public class ResponseResult<T> implements Serializable {
    private static final String ERROR_NO_SUCCESS = "0";
    private static final String ERROR_MSG_SUCCESS = "";
    private String errno;
    private String errmsg;
    private String tip;
    private String traceId;
    private T data;

    public ResponseResult() {
        this.traceId = getTraceId();

    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "" : traceId;
    }

    public static ResponseResult<Void> success() {
        ResponseResult<Void> responseResult = new ResponseResult<>();
        responseResult.setErrno(ERROR_NO_SUCCESS);
        responseResult.setErrmsg(ERROR_MSG_SUCCESS);
        responseResult.setData(null);
        return responseResult;
    }

    public ResponseResult<T> errmsg(String msg) {
        this.setErrmsg(msg);
        return this;
    }

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> responseResult = new ResponseResult<>();
        responseResult.setErrno(ERROR_NO_SUCCESS);
        responseResult.setErrmsg(ERROR_MSG_SUCCESS);
        responseResult.setData(data);
        return responseResult;
    }

    public static ResponseResult fail(String errno, String errmsg) {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.setErrno(errno);
        responseResult.setErrmsg(errmsg);
        responseResult.setData(null);
        return responseResult;
    }

    public static <T> ResponseResult<T> fail(String errno, String errmsg, T data) {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.setErrno(errno);
        responseResult.setErrmsg(errmsg);
        responseResult.setData(data);
        return responseResult;
    }

    public static ResponseResult fail(String errno, String errmsg, String tip) {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.setErrno(errno);
        responseResult.setErrmsg(errmsg);
        responseResult.setTip(tip);
        responseResult.setData(null);
        return responseResult;
    }

}
