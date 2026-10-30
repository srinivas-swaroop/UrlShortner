package com.tinymx.urlshortener.service;

import com.tinymx.urlshortener.model.ShortUrl;
import com.tinymx.urlshortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;

@Service
public class ShortUrlService {
    private final ShortUrlRepository repository;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public ShortUrlService(ShortUrlRepository repository,
                           @Value("${app.base-url}") String baseUrl) {
        this.repository = repository;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public ShortUrl createShortUrl(String inputUrl, String customSlug) {
        String url = validateAndNormalizeUrl(inputUrl);
        String code;
        if (customSlug != null && !customSlug.isBlank()) {
            code = customSlug.trim();
            if (!code.matches("[a-zA-Z0-9_-]{3,30}")) {
                throw new IllegalArgumentException("Custom slug must be 3-30 characters (letters, numbers, _ or -).");
            }
            if (repository.existsByShortCode(code)) {
                throw new IllegalArgumentException("That custom slug is already taken.");
            }
        } else {
            // Retry a few times in the extremely unlikely event of a random collision.
            int attempts = 0;
            do {
                code = generateCode();
                attempts++;
                if (attempts > 10) throw new IllegalStateException("Could not generate a unique short code. Try again.");
            } while (repository.existsByShortCode(code));
        }
        return repository.save(new ShortUrl(url, code));
    }

    public ShortUrl findByCode(String code) {
        return repository.findByShortCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Short link was not found."));
    }

    public List<ShortUrl> getRecentUrls() {
        return repository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(10).toList();
    }

    public String getShortLink(ShortUrl url) {
        return baseUrl + "/" + url.getShortCode();
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(7);
        for (int i = 0; i < 7; i++) code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        return code.toString();
    }

    private String validateAndNormalizeUrl(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("Please enter a URL.");
        String value = input.trim();
        if (!value.matches("(?i)^https?://.*")) value = "https://" + value;
        try {
            URI uri = URI.create(value);
            if (uri.getHost() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Enter a valid http or https URL.");
            }
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException && ex.getMessage() != null && ex.getMessage().startsWith("Enter a valid")) throw ex;
            throw new IllegalArgumentException("Enter a valid URL, such as https://example.com.");
        }
        if (value.length() > 2048) throw new IllegalArgumentException("URL is too long (maximum 2048 characters).");
        return value;
    }
}
