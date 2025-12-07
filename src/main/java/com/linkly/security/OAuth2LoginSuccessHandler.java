package com.linkly.security;

import com.linkly.entity.User;
import com.linkly.repository.UserRepository;
import com.linkly.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Injecting the frontend URL to redirect back to React
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Extract details from Google/GitHub
        // Note: 'email' is standard for Google, 'login' for GitHub
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Fallback if email is missing (should handle gracefully in prod)
        String username = (email != null) ? email : oAuth2User.getName();

        // 2. Check if user exists, or Signup (Create) them
        Optional<User> existingUser = userRepository.findByUsername(username);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            // OAuth users don't have passwords, but our DB requires one.
            // We set a random dummy password.
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.setRole("ROLE_USER");
            userRepository.save(newUser);
        }

        // 3. Generate JWT
        String token = jwtUtil.generateToken(username);

        // 4. Redirect to Frontend with Token
        // We send the token in a query param so React can grab it
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}