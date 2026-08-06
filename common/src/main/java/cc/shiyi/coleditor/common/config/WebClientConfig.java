package cc.shiyi.coleditor.common.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.SSLException;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient() throws SSLException {

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        ConnectionProvider connectionProvider = ConnectionProvider
                .builder("http-client")
                .maxConnections(100)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .maxIdleTime(Duration.ofSeconds(5))
                .pendingAcquireMaxCount(-1)
                .build();
        HttpClient httpClient = HttpClient
                .create(connectionProvider)
                .secure(t -> t.sslContext(sslContext))
                .responseTimeout(Duration.ofSeconds(15));
        WebClient webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        return webClient;
    }
}
