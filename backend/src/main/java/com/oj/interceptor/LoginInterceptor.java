package com.oj.interceptor;

import com.oj.common.ErrorCode;
import com.oj.common.UserContext;
import com.oj.exception.BusinessException;
import com.oj.model.entity.User;
import com.oj.service.TokenBlacklistService;
import com.oj.service.UserService;
import com.oj.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserService userService;
    
    @Resource
    private TokenBlacklistService tokenBlacklistService;
    


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 获取请求头中的token
            String header = request.getHeader(HEADER_NAME);
            if (!StringUtils.hasText(header)) {
                log.error("认证失败: 未提供认证信息");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 检查header格式
            if (!header.startsWith(TOKEN_PREFIX)) {
                log.error("认证失败: 认证信息格式错误");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 提取token
            String token = header.substring(TOKEN_PREFIX.length()).trim();
            if (!StringUtils.hasText(token)) {
                log.error("认证失败: 令牌为空");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 检查令牌是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.error("认证失败: 令牌已被注销");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 验证token
            if (!jwtUtils.validateToken(token)) {
                log.error("认证失败: 认证信息无效");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 获取用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                log.error("认证失败: 用户信息获取失败");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 查询用户
            User user = userService.getById(userId);
            if (user == null) {
                log.error("认证失败: 用户不存在");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 设置用户上下文
            UserContext.setUser(user);
            log.info("用户上下文设置成功，用户ID: {}, 用户名: {}", user.getId(), user.getUserName());
            return true;
        } catch (Exception e) {
            log.error("认证过程发生错误", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理用户上下文
        UserContext.clear();

    }
} 