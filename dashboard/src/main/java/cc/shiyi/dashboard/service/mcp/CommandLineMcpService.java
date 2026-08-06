package cc.shiyi.dashboard.service.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * 命令行执行MCP服务
 * 允许在当前部署服务器上执行命令行指令，包含安全校验和超时控制
 */
@Slf4j
@Service
public class CommandLineMcpService {

    /**
     * 危险命令黑名单，禁止执行
     */
    private static final String[] DANGER_COMMANDS = {
            "rm", "del", "erase", "format", "shutdown", "reboot", "halt",
            "mkfs", "dd", ":(){ :|:& };:", "poweroff", "init 0", "init 6",
            "chmod", "chown", "passwd", "useradd", "userdel", "groupadd", "groupdel"
    };

    /**
     * 执行命令行指令
     * @param cmd 要执行的命令
     * @param timeout 超时时间（秒），默认30秒，最大300秒
     * @return 执行结果，包含退出码和输出内容
     */
    @Tool(description = "在当前部署服务器上执行命令行指令，返回执行结果（包含标准输出、错误输出和退出码），禁止执行系统高危操作命令")
    public String executeCommand(
            @ToolParam(description = "要执行的命令，例如: dir (Windows)、ls -l (Linux)、ping baidu.com -n 4") String cmd,
            @ToolParam(description = "命令执行超时时间（秒），默认30秒，最大支持300秒") Integer timeout) throws IOException, InterruptedException {
        // 参数校验
        if (cmd == null || cmd.trim().isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }

        // 超时时间处理
        int timeoutSeconds = timeout != null ? timeout : 30;
        if (timeoutSeconds < 1 || timeoutSeconds > 300) {
            throw new IllegalArgumentException("超时时间必须在1-300秒之间");
        }

        // 危险命令校验
        String lowerCmd = cmd.toLowerCase();
        for (String dangerCmd : DANGER_COMMANDS) {
            if (lowerCmd.contains(dangerCmd)) {
                log.warn("检测到危险命令，已拦截: {}", cmd);
                throw new SecurityException("禁止执行危险命令: " + dangerCmd + "，如有特殊需求请联系管理员");
            }
        }

        log.info("MCP调用-执行命令: {}, 超时时间: {}s", cmd, timeoutSeconds);

        // 构建执行命令，根据操作系统选择对应的shell
        ProcessBuilder processBuilder;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", cmd);
        } else {
            processBuilder = new ProcessBuilder("/bin/sh", "-c", cmd);
        }

        // 合并错误流到标准输出，方便统一读取
        processBuilder.redirectErrorStream(true);
        // 设置工作目录为当前程序运行目录
        processBuilder.directory(new java.io.File("."));

        Process process = processBuilder.start();

        // 读取命令输出，根据系统编码处理中文乱码
        StringBuilder output = new StringBuilder();
        Charset charset = osName.contains("win") ? Charset.forName("GBK") : Charset.defaultCharset();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 等待命令执行完成，超时控制
        boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!completed) {
            process.destroy();
            process.waitFor(1, TimeUnit.SECONDS); // 等待进程终止
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            log.warn("命令执行超时，已强制终止: {}", cmd);
            throw new IOException("命令执行超时（" + timeoutSeconds + "秒），已强制终止");
        }

        int exitCode = process.exitValue();
        String resultOutput = output.toString().trim();

        // 构建返回结果
        StringBuilder result = new StringBuilder();
        result.append("执行命令: ").append(cmd).append("\n");
        result.append("退出码: ").append(exitCode).append("\n");
        result.append("执行结果:\n").append(resultOutput.isEmpty() ? "无输出" : resultOutput);

        log.info("命令执行完成，退出码: {}, 输出长度: {}", exitCode, resultOutput.length());
        return result.toString();
    }
}
