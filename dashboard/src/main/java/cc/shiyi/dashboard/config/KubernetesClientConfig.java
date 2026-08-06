package cc.shiyi.dashboard.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Kubernetes Client 配置
 * 支持多种配置方式（按优先级）：
 * 1. HTTP URL 下载 (config-url)
 * 2. 本地文件路径 (config-path)
 * 3. 集群内部署（自动检测 ServiceAccount）
 */
@Slf4j
@Configuration
public class KubernetesClientConfig {

    @Value("${kubernetes.client.config-url:}")
    String configUrl;

    @Value("${kubernetes.client.config-path:}")
    String configPath;

    @Value("${kubernetes.client.trust-certs:true}")
    boolean trustCerts;

    @Bean
    public KubernetesClient kubernetesClient() throws IOException {
        log.info("Initializing Kubernetes Client...");

        Config config = null;
        File kubeconfigFile = null;

        // 1. 优先尝试从 HTTP URL 下载
        if (configUrl != null && !configUrl.isBlank()) {
            try {
                kubeconfigFile = downloadFromUrl(configUrl);
                if (kubeconfigFile != null && kubeconfigFile.exists()) {
                    log.info("Using kubeconfig downloaded from URL: {}", configUrl);
                    config = Config.fromKubeconfig(kubeconfigFile);
                }
            } catch (Exception e) {
                log.warn("Failed to download kubeconfig from URL: {}, trying other methods", configUrl, e);
            }
        }

        // 2. 尝试本地文件
        if (config == null && configPath != null && !configPath.isBlank()) {
            File configFile = new File(configPath);
            if (configFile.exists() && configFile.canRead()) {
                log.info("Using kubeconfig from file: {}", configPath);
                config = Config.fromKubeconfig(configFile);
            } else {
                log.warn("Config file not found or unreadable: {}", configPath);
            }
        }

        // 3. 如果都没有配置，抛出异常退出
        if (config == null) {
            log.error("No Kubernetes configuration found. Please set either kubernetes.client.config-url or kubernetes.client.config-path.");
            throw new IllegalStateException("Kubernetes configuration is required: neither config-url nor config-path is set");
        }

        config.setTrustCerts(trustCerts);

        // 清理临时文件
        if (kubeconfigFile != null && kubeconfigFile.getName().startsWith("kubeconfig-tmp-")) {
            final File tempFile = kubeconfigFile;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (tempFile.exists()) {
                    try {
                        Files.deleteIfExists(tempFile.toPath());
                        log.debug("Cleaned up temporary kubeconfig file");
                    } catch (IOException e) {
                        log.warn("Failed to clean up temp file", e);
                    }
                }
            }));
        }

        log.info("Kubernetes Client initialized, namespace: {}", config.getNamespace());
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    private File downloadFromUrl(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        log.info("Downloading kubeconfig from: {}", uri);

        try (InputStream in = uri.toURL().openStream()) {
            Path tempPath = Files.createTempFile("kubeconfig-tmp-", ".yaml");
            Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Downloaded kubeconfig to: {}", tempPath);
            return tempPath.toFile();
        }
    }
}
