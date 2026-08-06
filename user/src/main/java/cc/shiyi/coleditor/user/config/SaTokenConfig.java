package cc.shiyi.coleditor.user.config;

import cc.shiyi.coleditor.user.service.StpInterfaceImpl;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SaTokenConfig {

    @Bean
    @Primary
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }

    @PostConstruct
    public void rewriteToken() {
        SaStrategy.instance.createToken = (loginId, loginType) -> SaFoxUtil.getRandomString(32);
    }
}
