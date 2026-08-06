package cc.shiyi.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SshMcpRequest {
    @ToolParam(required = true, description = "SSH远程连接目标服务器ip")
    private String ip;

    @ToolParam(required = false, description = "SSH远程连接目标服务器端口, 默认值 22")
    private Integer port = 22;

    @ToolParam(required = true, description = "SSH远程登录目标服务器的用户名")
    private String user;

    @ToolParam(required = true, description = "SSH远程登录目标服务器的密码")
    private String passwd;

    @ToolParam(required = true, description = "通过SSH发送给目标服务器的命令")
    private String cmd;
}
