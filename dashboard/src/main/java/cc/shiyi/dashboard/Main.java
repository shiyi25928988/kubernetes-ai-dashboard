package cc.shiyi.dashboard;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages="cc.shiyi")
@ComponentScan(basePackages = {"cc.shiyi"})
@MapperScan(basePackages = {"cc.shiyi"}, markerInterface = BaseMapper.class)
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}