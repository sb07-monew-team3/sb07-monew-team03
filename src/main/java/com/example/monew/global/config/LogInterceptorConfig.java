package com.example.monew.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Configuration
public class LogInterceptorConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUserId;
        requestUserId=request.getHeader("Monew-Request-Id")==null
                ? UUID.randomUUID().toString()
                :request.getHeader("Monew-Request-Id");
       MDC.put("requestUserId",requestUserId);
       MDC.put("requestIp",getClientIpv4(request));
       MDC.put("requestUri",request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
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
