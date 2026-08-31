package ru.morkamo.kontrolbankdata.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserStateInterceptor userStateInterceptor;
    private final DatabaseLockInterceptor databaseLockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStateInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/css/**", "/js/**", "/error");
        registry.addInterceptor(databaseLockInterceptor).addPathPatterns(
                "/bankrecall/create", "/bankrecall/update/**", "/bankrecall/delete/**",
                "/sedrecall/create", "/sedrecall/update/**", "/sedrecall/delete/**",
                "/manualwork/create", "/manualwork/update/**", "/manualwork/delete/**",
                "/admin/users/create", "/admin/users/*/update", "/admin/users/*/delete");
    }
}
