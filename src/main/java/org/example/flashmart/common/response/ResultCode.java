package org.example.flashmart.common.response;

public enum ResultCode {

    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "请求参数不合法"),
    BUSINESS_ERROR(400, "业务处理失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    INTERNAL_ERROR(500, "服务器内部错误，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
