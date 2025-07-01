// package com.example.demo.config;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.util.Collections;

// //@Component
// public class HeaderAuthenticationFilter extends OncePerRequestFilter {

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {

//         String userId = request.getHeader("X-User-Id");
//         String userRole = request.getHeader("X-User-Role");

//         System.out.println("HeaderAuthenticationFilter - Processing headers:");
//         System.out.println("X-User-Id: " + userId);
//         System.out.println("X-User-Role: " + userRole);

//         if (userId != null && !userId.isEmpty() && userRole != null && !userRole.isEmpty()) {
//             // Tạo đối tượng Authentication
//             UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                     userId,
//                     null, // Không có credentials
//                     Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole)));

//             // Đặt authentication vào SecurityContext
//             SecurityContextHolder.getContext().setAuthentication(auth);
//             System.out.println("Authentication set in SecurityContext for user ID: " + userId);
//         } else {
//             System.out.println("No authentication headers found or invalid headers");
//         }

//         filterChain.doFilter(request, response);
//     }
// }