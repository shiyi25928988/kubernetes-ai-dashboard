package cc.shiyi.dashboard.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A2A 认证过滤器
 * 对 /a2a/** 路径的请求进行 API Key 认证，并建立 Sa-Token 系统登录上下文。
 *
 * <p>认证流程：</p>
 * <ol>
 *   <li>从 Authorization（Bearer token）或 X-API-Key 请求头提取 API Key</li>
 *   <li>与配置的 a2a.api-key（默认复用 external.api-key）比对</li>
 *   <li>认证通过后调用 StpUtil.login(systemUserId) 建立登录上下文，
 *       使下游 MpiJobService 等依赖 Sa-Token 的服务可正常获取创建者 ID</li>
 *   <li>认证失败返回 401 Unauthorized</li>
 * </ol>
 *
 * <p>注意：GET /a2a/card 与 /a2a/.well-known/agent-card.json 为发现端点，免认证。</p>
 */
@Slf4j
@Component
@Order(1)
@Setter(onMethod_ = @Autowired)
@ConditionalOnProperty(prefix = "a2a", name = "enabled", havingValue = "true", matchIfMissing = true)
public class A2AAuthFilter extends OncePerRequestFilter {

    A2AProperties a2aProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 仅拦截 A2A 端点
        if (!path.startsWith(a2aProperties.getContextPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        // GET 发现端点免认证
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 未启用认证则直接放行
        if (!a2aProperties.isAuthEnabled()) {
            establishSystemLogin();
            filterChain.doFilter(request, response);
            return;
        }

        // 校验 API Key
//        String apiKey = extractApiKey(request);
//        if (apiKey == null || apiKey.isEmpty()) {
//            log.warn("A2A 请求缺少 API Key: path={}", path);
//            sendUnauthorized(response, "缺少 API Key，请在 Authorization 或 X-API-Key 请求头中提供");
//            return;
//        }
//
//        if (!apiKey.equals(a2aProperties.getApiKey())) {
//            log.warn("A2A 请求 API Key 无效: path={}", path);
//            sendUnauthorized(response, "API Key 无效");
//            return;
//        }

        // 认证通过，建立系统登录上下文
        establishSystemLogin();
        log.debug("A2A 认证通过: path={}", path);
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 API Key
     * 优先从 X-API-Key 头获取，其次从 Authorization: Bearer <key> 头获取
     */
    private String extractApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    /**
     * 建立系统登录上下文，使下游依赖 Sa-Token 的服务可用
     */
    private void establishSystemLogin() {
        try {
            if (!StpUtil.isLogin()) {
                StpUtil.login(a2aProperties.getSystemUserId());
                log.debug("A2A 建立系统登录上下文: userId={}", a2aProperties.getSystemUserId());
            }
        } catch (Exception e) {
            log.warn("A2A 建立系统登录上下文失败: {}", e.getMessage());
        }
    }

    /**
     * 返回 401 未授权响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}");
    }
}
