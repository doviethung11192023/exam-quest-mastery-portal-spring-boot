// // package com.example.demo.config;
// // import java.util.Arrays;
// // import org.springframework.web.cors.CorsConfiguration;
// // import org.springframework.web.cors.CorsConfigurationSource;
// // import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// // import org.springframework.context.annotation.Bean;
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// // import org.springframework.security.web.SecurityFilterChain;

// // @Configuration
// // @EnableWebSecurity
// // public class SecurityConfig {

// //     @Bean
// //     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// //         http
// //                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
// //                 .csrf(csrf -> csrf.disable())
// //                 .authorizeHttpRequests(authorize -> authorize
// //                         .requestMatchers("/test/**", "/auth/**",
// //                                 "/accounts/**").permitAll()
// //                         .anyRequest().authenticated());

// //         return http.build();
// //     }

// //     @Bean
// //     public CorsConfigurationSource corsConfigurationSource() {
// //         CorsConfiguration configuration = new CorsConfiguration();
// //         configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
// //         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
// //         configuration.setAllowedHeaders(Arrays.asList("*"));
// //         configuration.setExposedHeaders(Arrays.asList("Authorization"));
// //         configuration.setAllowCredentials(true);
// //         configuration.setMaxAge(3600L);

// //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// //         source.registerCorsConfiguration("/**", configuration);
// //         return source;
// //     }
// // }
// package com.example.demo.config;

// import java.util.Arrays;
// import java.util.Collections;

// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import jakarta.servlet.http.HttpServletResponse;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// // @Configuration
// // @EnableWebSecurity
// // public class SecurityConfig {

// //     @Bean
// //     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// //         http
// //                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
// //                 .csrf(csrf -> csrf.disable())
// //                 .authorizeHttpRequests(authorize -> authorize
// //                         .requestMatchers("/test/**", "/auth/**",
// //                                 "/accounts/**", "/giaovien/**")    
// //                         .permitAll()
// //                         .anyRequest().authenticated());

// //         return http.build();
// //     }

// //     //     @Bean
// //     //     public CorsConfigurationSource corsConfigurationSource() {
// //     //         CorsConfiguration configuration = new CorsConfiguration();
// //     //         configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
// //     //         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

// //     //         // Add X-User-Role to allowed headers (case insensitive)
// //     //         configuration.setAllowedHeaders(Arrays.asList(
// //     //                 "Authorization", "Content-Type", "X-User-Role"));

// //     //         configuration.setExposedHeaders(Collections.singletonList("Authorization"));
// //     //         configuration.setAllowCredentials(true);
// //     //         configuration.setMaxAge(3600L);

// //     //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// //     //         source.registerCorsConfiguration("/**", configuration);
// //     //         return source;
// //     //     }
// //     // }
// //     // SecurityConfig.java
// //     @Bean
// //     public CorsConfigurationSource corsConfigurationSource() {
// //         CorsConfiguration configuration = new CorsConfiguration();
// //         configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
// //         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

// //         // Add X-User-Id to allowed headers
// //         configuration.setAllowedHeaders(Arrays.asList(
// //                 "Authorization", "Content-Type", "X-User-Role", "X-User-Id"));

// //         configuration.setExposedHeaders(Collections.singletonList("Authorization"));
// //         configuration.setAllowCredentials(true);
// //         configuration.setMaxAge(3600L);

// //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// //         source.registerCorsConfiguration("/**", configuration);
// //         return source;
// //     }
// // }
// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
// @Autowired
// private HeaderAuthenticationFilter headerAuthenticationFilter;
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                 .csrf(csrf -> csrf.disable())
//                  .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        
//                 .authorizeHttpRequests(authorize -> authorize
//                         .requestMatchers("/test/**", "/auth/**", "/accounts/**", "/giaovien/**", "/public/**")
//                         .permitAll()
//                         .anyRequest().authenticated())
//                 .exceptionHandling(ex -> ex
//                         .authenticationEntryPoint((request, response, authException) -> {
//                             System.out.println("Security exception: " + authException.getMessage());
//                             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                             response.getWriter().write("Authentication failed: " + authException.getMessage());
//                         }));

//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//         configuration.setAllowedHeaders(Arrays.asList(
//                 "Authorization", "Content-Type", "X-User-Role", "X-User-Id", "Cache-Control",
//                 "X-Requested-With", "Accept", "Access-Control-Allow-Origin"));
//         configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
//         configuration.setAllowCredentials(true);
//         configuration.setMaxAge(3600L);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }
// }

package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình đơn giản nhất: cho phép tất cả mọi request và tắt CSRF
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Cho phép tất cả các request không cần xác thực
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}