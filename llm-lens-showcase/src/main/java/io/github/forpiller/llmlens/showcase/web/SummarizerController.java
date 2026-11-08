package io.github.forpiller.llmlens.showcase.web;

import io.github.forpiller.llmlens.annotation.LlmFeature;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class SummarizerController {

    private final ChatClient chatClient;

    SummarizerController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/summarize")
    @LlmFeature("summarizer")
    public String summarize(@RequestParam String text, HttpSession session) {
        String summary = chatClient.prompt()
                .user("Summarize the following text concisely:\n" + text)
                .call()
                .content();
        session.setAttribute("summarizeText", text);
        session.setAttribute("summarizeResult", summary);
        return "redirect:/";
    }
}
