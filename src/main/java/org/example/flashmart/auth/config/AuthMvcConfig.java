package org.example.flashmart.auth.config;

import org.example.flashmart.auth.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class AuthMvcConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;

    public AuthMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 商品浏览和登录注册保持公开，购物车、结算、订单等交易接口必须登录。
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/refresh",
                        "/api/products",
                        "/api/products/**",
                        "/login",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );
    }
}
