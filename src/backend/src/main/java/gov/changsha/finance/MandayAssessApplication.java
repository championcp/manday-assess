package gov.changsha.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 长沙市财政评审中心软件规模评估系统 - 主启动类
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@SpringBootApplication
@EnableJpaAuditing
public class MandayAssessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MandayAssessApplication.class, args);
    }
}