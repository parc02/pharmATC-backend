package park.pharmatc.v1;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // API 경로 전체에 대해
                .allowedOrigins("https://pharmatc-90ac0.web.app") // 허용할 프론트 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용 메소드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(false); // 쿠키/세션 필요 없으면 false
    }
}
