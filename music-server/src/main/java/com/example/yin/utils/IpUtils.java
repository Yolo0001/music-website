package com.example.yin.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class IpUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

    private IpUtils() {
    }

    public static String getClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return "UNKNOWN";
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (StringUtils.isNotBlank(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                return extractFirstIp(ipList);
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.defaultIfBlank(remoteAddr, "UNKNOWN");
    }

    private static String extractFirstIp(String ipList) {
        String[] ips = ipList.split(",");
        return ips.length > 0 ? ips[0].trim() : ipList;
    }
}

