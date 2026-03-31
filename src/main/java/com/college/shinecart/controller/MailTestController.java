package com.college.shinecart.controller;

import com.college.shinecart.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test-mail")
    public String testMail() {

        emailService.sendEmail(
                "darshanmali44444@gmail.com",
                "SMTP Test",
                "If you received this, SMTP works!"
        );

        return "Mail sent. Check inbox.";
    }
}

