package org.example.fairpair_backend

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")  // 모든 경로에 대해 CORS 허용
            .allowedOrigins("http://localhost:4296")  // 허용할 출처
            .allowedMethods("GET", "POST", "PUT", "DELETE")  // 허용할 HTTP 메서드
            .allowedHeaders("*")  // 모든 헤더를 허용
            .allowCredentials(true)  // 자격 증명 허용
    }
}