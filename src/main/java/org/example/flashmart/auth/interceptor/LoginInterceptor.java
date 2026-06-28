package org.example.flashmart.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.flashmart.auth.util.JwtUtil;
import org.example.flashmart.common.response.Result;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        // 没有token
        if (token == null || !token.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "未登录");
            return false;
        }
        token = token.substring(7); // 去掉 "Bearer "
        // token 无效或过期
        if (!jwtUtil.isValid(token)) {
            writeUnauthorizedResponse(response, "Token 无效或已过期");
            return false;
        }
        // 把用户信息存入 request，后续 Controller 直接取
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        Result<Void> result = Result.error(401, message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(buildJsonResponse(result));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
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
