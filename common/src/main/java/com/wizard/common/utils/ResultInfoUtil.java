package com.wizard.common.utils;


import com.wizard.common.base.ResultInfo;
import com.wizard.common.constants.ApiConstant;

public class ResultInfoUtil {
    /**
     * 请求出错返回
     *
     * @param path 请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildError(String path) {
        return build(ApiConstant.ERROR_CODE, ApiConstant.ERROR_MESSAGE, path, null);
    }


    /**
     * 请求出错返回
     *
     * @param errorCode 错误代码
     * @param message   错误提示信息
     * @param path      请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildErrorMsg(int errorCode, String message, String path) {
        return build(errorCode, message, path, null);
    }

    /**
     * 请求出错返回
     *
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildErrorMsg(String message) {
        return build(ApiConstant.ERROR_CODE, message, null, null);
    }

    /**
     * 请求出错返回
     *
     * @param message 错误信息
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildErrorMsg(String message, int code) {
        return build(code, message, null, null);
    }

    /**
     * 请求成功返回
     *
     * @param path 请求路径
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildSuccess(String path) {
        return build(ApiConstant.SUCCESS_CODE, ApiConstant.SUCCESS_MESSAGE, path, null);
    }

    /**
     * 请求成功返回
     *
     * @param path 请求路径
     * @param data 返回数据对象
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> buildSuccess(String path, T data) {
        return build(ApiConstant.SUCCESS_CODE, ApiConstant.SUCCESS_MESSAGE, path, data);
    }

    /**
     * 构建返回的通用对象
     *
     * @param code    返回code
     * @param message 返回数据对象
     * @param path    请求刘晶
     * @param data    返回的数据对象
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> build(Integer code, String message, String path, T data) {
        if (code == null) {
            code = ApiConstant.SUCCESS_CODE;
        }
        if (message == null) {
            message = ApiConstant.SUCCESS_MESSAGE;
        }
        ResultInfo<T> resultInfo = new ResultInfo<>();
        resultInfo.setCode(code);
        resultInfo.setMsg(message);
        resultInfo.setData(data);
        return resultInfo;
    }
}
