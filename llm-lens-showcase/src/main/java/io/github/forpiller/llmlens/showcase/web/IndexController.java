package io.github.forpiller.llmlens.showcase.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class IndexController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        model.addAttribute("chatMessage",     session.getAttribute("chatMessage"));
        model.addAttribute("chatResult",      session.getAttribute("chatResult"));
        model.addAttribute("summarizeText",   session.getAttribute("summarizeText"));
        model.addAttribute("summarizeResult", session.getAttribute("summarizeResult"));
        return "index";
    }
}
