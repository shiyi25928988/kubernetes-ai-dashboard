package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.service.mcp.*;
import lombok.Setter;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Setter(onMethod_ = @Autowired)
public class McpConfig {

    KubernetesMcpService kubernetesMcpService;
    SystemInfoMcpService systemInfoMcpService;
    CommandLineMcpService commandLineMcpService;
    FileMcpService fileMcpService;
    SshMcpService sshMcpService;
    OssMcpService ossMcpService;
    DockerRegistryMcpService dockerRegistryMcpService;

    @Bean
    public ToolCallbackProvider toolCallbackProvider() {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(Arrays.asList(systemInfoMcpService,
                        kubernetesMcpService,
                        commandLineMcpService,
                        fileMcpService,
                        sshMcpService,
                        ossMcpService,
                        dockerRegistryMcpService).toArray())
                .build();
    }
}
