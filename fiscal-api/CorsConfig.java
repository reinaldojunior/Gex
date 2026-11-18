package br.gov.serpro.rtc.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Libera CORS para que a página Fiscal do GEx (rodando no navegador, em outro
 * domínio) consiga chamar a calculadora.
 *
 * Copie este arquivo para:
 *   src/main/java/br/gov/serpro/rtc/config/security/CorsConfig.java
 *
 * Origens permitidas: variável de ambiente GEX_CORS_ORIGINS (separadas por vírgula).
 * Ex.: GEX_CORS_ORIGINS=https://meu-gex.vercel.app,http://localhost:5500
 * Padrão "*" (qualquer origem) — a API é pública e não usa cookies.
 */
@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${GEX_CORS_ORIGINS:*}") String origins) {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> lista = Arrays.stream(origins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        cfg.setAllowedOriginPatterns(lista);
        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("x-warning-dados-simulados"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
