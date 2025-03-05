package com.oj.interceptor;

import com.oj.common.BusinessException;
import com.oj.common.ErrorCode;
import com.oj.common.UserContext;
import com.oj.model.entity.User;
import com.oj.service.UserService;
import com.oj.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Value("${jwt.header}")
    private String headerName;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        try {
            // 获取token
            String header = request.getHeader(headerName);
            logger.debug("Received header: {}", header);

            // 检查header是否存在
            if (!StringUtils.hasText(header)) {
                logger.debug("No authorization header found");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未提供认证信息");
            }

            // 检查header格式
            if (!header.startsWith(tokenPrefix)) {
                logger.debug("Invalid authorization header format");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "认证信息格式错误");
            }

            // 提取token
            String token = header.substring(tokenPrefix.length()).trim();
            if (!StringUtils.hasText(token)) {
                logger.debug("Empty token");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "令牌为空");
            }

            // 验证token
            if (!jwtUtils.validateToken(token)) {
                logger.debug("Invalid token");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "令牌无效");
            }

            // 获取用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                logger.debug("User not found for id: {}", userId);
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
            }

            // 设置用户信息到上下文
            UserContext.setUser(user);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Authentication error", e);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "认证过程发生错误");
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 清理用户上下文
        UserContext.clear();
    }
} 