package com.example.recruitmenttrainingsystem.security;
import com.example.recruitmenttrainingsystem.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtUtil.parseClaims(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class); // Sẽ là null nếu user chưa có role


                // Xử lý trường hợp user chưa có role
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(role)) {
                    // Chỉ thêm quyền nếu role tồn tại
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities // Truyền vào danh sách (có thể rỗng)
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ignored) {}
        }

        filterChain.doFilter(request, response);
    }
}