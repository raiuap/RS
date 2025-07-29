package org.example.restlye1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF (útil si es solo API por ahora)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite acceso sin login a todas las rutas
                )
                .httpBasic(Customizer.withDefaults()); // Puedes quitarlo si no quieres ver el popup básico

        return http.build();
    }
}