package org.example.flashmart.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.flashmart.auth.service.TokenBlacklistService;
import org.example.flashmart.auth.util.JwtUtil;
import org.example.flashmart.common.response.Result;
import org.example.flashmart.common.response.ResultCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginInterceptor(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "未登录");
            return false;
        }
        token = token.substring(7);

        if (!jwtUtil.isValid(token)) {
            writeUnauthorizedResponse(response, "Token 无效或已过期");
            return false;
        }
        // 只接受访问令牌，刷新令牌不能直接用来访问业务接口。
        if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(jwtUtil.getTokenType(token))) {
            writeUnauthorizedResponse(response, "Token 类型不正确");
            return false;
        }
        // 已登出的 token 即使没过期也要拒绝。
        if (tokenBlacklistService.isBlacklisted(jwtUtil.getJti(token))) {
            writeUnauthorizedResponse(response, "登录态已失效，请重新登录");
            return false;
        }

        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));
        return true;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(buildJsonResponse(result));
    }

    private String buildJsonResponse(Result<Void> result) {
        return "{\"code\":" + result.getCode()
                + ",\"message\":\"" + escapeJson(result.getMessage())
                + "\",\"data\":null}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
