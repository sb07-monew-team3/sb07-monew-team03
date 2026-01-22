package com.example.monew.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Configuration
public class LogInterceptorConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String requestId = UUID.randomUUID().toString();

        String requestUserId = (request.getHeader("Monew-Request-User-Id") == null)
                ? "anonymous"
                : request.getHeader("Monew-Request-User-Id");

        // MDC 세팅
        MDC.put("requestId", requestId);
        MDC.put("requestUserId", requestUserId);
        MDC.put("requestIp", getClientIpv4(request));
        MDC.put("requestUri", request.getRequestURI());

        // 응답 헤더 설정
        response.setHeader("Monew-Request-Id", requestId);

        // 컨트롤러 진입 로그
        if (handler instanceof HandlerMethod handlerMethod) {
            log.info("[{}] {}.{}",
                    request.getMethod(),
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName()
            );
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("Request completed with status={}", response.getStatus());
        MDC.clear();
    }

    private String getClientIpv4(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if(ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)){
            ip=ip.split(",")[0].trim();
        }
        else{
            ip=request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) { // 로컬의 경우
            ip = "127.0.0.1";
        } else if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }
        return ip;
    }
}
