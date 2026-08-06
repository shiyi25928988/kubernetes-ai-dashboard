package cc.shiyi.coleditor.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sa-token")
public class ExcludePathConfig {

    private List<String> excludePath;
}
