package com.queuemgmt.controller;

import com.queuemgmt.dto.QueueStatusResponse;
import com.queuemgmt.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final QueueService queueService;

    /**
     * Home page for registration
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Admin dashboard
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    /**
     * Queue tracking page (accessed via SMS link)
     */
    @GetMapping("/queue/track/{accessToken}")
    public String trackQueue(@PathVariable String accessToken, Model model) {
        try {
            QueueStatusResponse status = queueService.getStatusByAccessToken(accessToken);
            model.addAttribute("status", status);
            return "track";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
