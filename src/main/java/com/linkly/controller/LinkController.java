package com.linkly.controller;

import com.linkly.entity.Link;
import com.linkly.entity.User;
import com.linkly.repository.UserRepository;
import com.linkly.service.LinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class LinkController {

    private final LinkService linkService;
    private final UserRepository userRepository; // Injected to fetch the full User entity

    public LinkController(LinkService linkService, UserRepository userRepository) {
        this.linkService = linkService;
        this.userRepository = userRepository;
    }

    // API 1: Create a Short Link
    // POST /api/v1/links?longUrl=https://google.com
    @PostMapping("/api/v1/links")
    public ResponseEntity<String> createShortLink(@RequestParam String longUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        String shortCode = linkService.createShortLink(longUrl, user);

        // In a real app, return the full domain (e.g., http://localhost:8080/r/...)
        return ResponseEntity.ok(shortCode);
    }

    // API 2: Get All Links for Current User (New)
    @GetMapping("/api/v1/links")
    public ResponseEntity<List<Link>> getAllLinks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Link> links = linkService.getAllLinksByUser(user);
        return ResponseEntity.ok(links);
    }

    // API 2: Redirect
    // GET /r/{shortCode}
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<String> link = linkService.getOriginalLink(shortCode);

        if (link.isPresent()) {
            // 302 Found (Temporary Redirect)
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(link.get()))
                    .build();
        } else {
            // 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}