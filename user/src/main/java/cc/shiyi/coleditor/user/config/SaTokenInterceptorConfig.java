package cc.shiyi.coleditor.user.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    ExcludePathConfig excludePathConfig;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/**", () -> {StpUtil.checkLogin();});
//                    SaRouter.match("/admin/**", () -> {StpUtil.checkRole("admin");});
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(excludePathConfig.getExcludePath().toArray(new String[0]));
    }
}
