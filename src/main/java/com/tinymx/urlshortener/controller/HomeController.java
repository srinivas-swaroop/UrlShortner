package com.tinymx.urlshortener.controller;

import com.tinymx.urlshortener.model.ShortUrl;
import com.tinymx.urlshortener.service.ShortUrlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final ShortUrlService shortUrlService;
    public HomeController(ShortUrlService shortUrlService) { this.shortUrlService = shortUrlService; }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentUrls", shortUrlService.getRecentUrls());
        return "index";
    }

    @PostMapping("/shorten")
    public String shorten(@RequestParam String url,
                          @RequestParam(required = false) String slug,
                          RedirectAttributes redirectAttributes) {
        try {
            ShortUrl saved = shortUrlService.createShortUrl(url, slug);
            redirectAttributes.addFlashAttribute("shortLink", shortUrlService.getShortLink(saved));
            redirectAttributes.addFlashAttribute("originalUrl", saved.getOriginalUrl());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/{code:[a-zA-Z0-9_-]{3,30}}")
    public String redirectToOriginal(@PathVariable String code) {
        return "redirect:" + shortUrlService.findByCode(code).getOriginalUrl();
    }
}
