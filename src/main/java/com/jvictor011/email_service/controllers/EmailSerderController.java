package com.jvictor011.email_service.controllers;

import com.jvictor011.email_service.application.EmailSenderService;
import com.jvictor011.email_service.core.EmailRequest;
import com.jvictor011.email_service.core.exceptions.EmailServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailSerderController {
    private final EmailSenderService emailSenderService;

    @Autowired
    public EmailSerderController(EmailSenderService emailService){
        this.emailSenderService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request){
        try {
            this.emailSenderService.sendEmail(request.to(), request.subject(), request.body());
            return ResponseEntity.ok("Email enviado com sucesso!!");
        }catch (EmailServiceException exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar email");
        }
    }
}
