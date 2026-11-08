package io.github.forpiller.llmlens.showcase.web;

import io.github.forpiller.llmlens.annotation.LlmFeature;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ChatController {

    private final ChatClient chatClient;

    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Rispondi sempre in italiano, in modo conciso e diretto.")
                .build();
    }

    @PostMapping("/chat")
    @LlmFeature("chatbot")
    public String chat(@RequestParam String message, HttpSession session) {
        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();
        session.setAttribute("chatMessage", message);
        session.setAttribute("chatResult", reply);
        return "redirect:/";
    }
}
