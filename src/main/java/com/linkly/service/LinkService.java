package com.linkly.service;

import com.linkly.entity.Link;
import com.linkly.entity.User;
import com.linkly.repository.LinkRepository;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final StringRedisTemplate redisTemplate;

    // Constructor Injection (Best Practice)
    public LinkService(LinkRepository linkRepository, StringRedisTemplate redisTemplate) {
        this.linkRepository = linkRepository;
        this.redisTemplate = redisTemplate;
    }

    // 1. Logic to create a short link
    public String createShortLink(String longUrl, User user) {
        // TODO: In the future, check if URL is valid or if it already exists

        // Generate a random 8-character string for the short code
        // (Simple approach for Layer 1)
        String shortCode = UUID.randomUUID().toString().substring(0, 8);

        Link link = new Link();
        link.setLongUrl(longUrl);
        link.setShortCode(shortCode);
        link.setUser(user);

        linkRepository.save(link);

        redisTemplate.opsForValue().set(shortCode, longUrl, 10, TimeUnit.MINUTES);

        return shortCode;
    }

    // 2. Logic to retrieve the original URL
    public Optional<String> getOriginalLink(String shortCode) {

        String cachedUrl  = redisTemplate.opsForValue().get(shortCode);

        if(cachedUrl == null) {
            return Optional.of(cachedUrl);
        }

        Optional<Link> link = linkRepository.findByShortCode(shortCode);

        if(link.isPresent()) {
            String longUrl = link.get().getLongUrl();
            redisTemplate.opsForValue().set(shortCode, longUrl, 10, TimeUnit.MINUTES);
            return Optional.of(link.get().getLongUrl());
        }


        return Optional.empty();

    }

    // 3. Get All Links for a User (New)
    public List<Link> getAllLinksByUser(User user) {
        return linkRepository.findByUser(user);
    }
}