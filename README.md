# Microserviço de Email

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

Este projeto é uma API de disparo de e-mails construída utilizando **Java, Spring Boot e AWS Simple Email Service (SES)**.

## Sumário

- [Instalação](#instalação)
- [Uso](#uso)
- [Endpoints da API](#endpoints-da-api)
- [Documentação da API](#documentação-da-api)

## Instalação

1. Clone o repositório:

```bash
git clone https://github.com/JVictor011/EmailService
```

2. Instale as dependências com Maven:

```bash
mvn clean install
```

3. Atualize o application.properties com suas credenciais da AWS:

```bash
aws.region=us-east-1
aws.accessKeyId=1111111
aws.secretKey=111111
```

## Uso

1. Inicie a aplicação com Maven:

```bash
mvn spring-boot:run
```

2. A API estará acessível em http://localhost:8080

## Endpoints da API

A API fornece o seguinte endpoint:

**ENVIAR EMAIL**

```markdown
POST /api/email/send - Envia um e-mail do remetente para o destinatário
```

**Corpo da Requisição**

```json
{
  "to": "test@gmail.com",
  "subject": "teste",
  "body": "teste"
}
```

# Documentação da API

## Pacote `adapters`

O pacote `adapters` contém interfaces que definem os contratos para enviar e-mails. Ele serve como um ponto de abstração, permitindo a implementação de diferentes provedores de serviços de e-mail.

### `EmailSenderGateway`

```java
package com.jvictor011.email_service.adapters;

public interface EmailSenderGateway {
    void sendEmail(String to, String subject, String body);
}
```

## Pacote `application`

O pacote `application` contém a lógica de aplicação e implementa os casos de uso definidos nos pacotes `core` e `adapters`.

### `EmailSenderService`

```java
package com.jvictor011.email_service.application;

import com.jvictor011.email_service.adapters.EmailSenderGateway;
import com.jvictor011.email_service.core.EmailSenderUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService implements EmailSenderUseCase {

    private final EmailSenderGateway emailSenderGateway;

    @Autowired
    public EmailSenderService(EmailSenderGateway emailGateway){
        this.emailSenderGateway = emailGateway;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        this.emailSenderGateway.sendEmail(to, subject, body);
    }
}
```

## Pacote `controllers`

O pacote `controllers` contém os controladores REST que expõem os endpoints da API. Esses controladores recebem as requisições HTTP, processam-nas utilizando os serviços da aplicação e retornam as respostas apropriadas.

### `EmailSenderController`

```java
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
public class EmailSenderController {
    private final EmailSenderService emailSenderService;

    @Autowired
    public EmailSenderController(EmailSenderService emailService){
        this.emailSenderService = emailService;
    }

    @PostMapping()
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request){
        try {
            this.emailSenderService.sendEmail(request.to(), request.subject(), request.body());
            return ResponseEntity.ok("Email enviado com sucesso!!");
        } catch (EmailServiceException exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar email");
        }
    }
}
```

## Pacote `core`

O pacote `core` contém as regras de negócio da aplicação. Ele define os conceitos centrais e as operações essenciais para o domínio do problema que a aplicação está resolvendo.

### `EmailRequest`

```java
package com.jvictor011.email_service.core;

public record EmailRequest(String to, String subject, String body) {
}
```

`EmailSenderUseCase`

```java
package com.jvictor011.email_service.core;

public interface EmailSenderUseCase {
    void sendEmail(String to, String subject, String body);
}
```

## Pacote `core.exceptions`

O pacote `core.exceptions` contém as classes de exceção específicas para o domínio da aplicação.

### `EmailServiceException`

```java
package com.jvictor011.email_service.core.exceptions;

public class EmailServiceException extends RuntimeException{
    public EmailServiceException(String message){
        super(message);
    }

    public EmailServiceException(String message, Throwable cause){
        super(message, cause);
    }
}

```

## Pacote `infra.ses`

O `pacote infra.ses` contém as classes e configurações relacionadas à integração com o AWS Simple Email Service (SES).

### `AwsSesConfig`
```java
package com.jvictor011.email_service.infra.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSesConfig {
    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService(){
        return AmazonSimpleEmailServiceClientBuilder.standard().build();
    }
}
```

### `SesEmailSender`

```java
package com.jvictor011.email_service.infra.ses;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.jvictor011.email_service.adapters.EmailSenderGateway;
import com.jvictor011.email_service.core.exceptions.EmailServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SesEmailSender implements EmailSenderGateway {
    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Autowired
    public SesEmailSender(AmazonSimpleEmailService amazonSimpleEmailService){
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        SendEmailRequest request = new SendEmailRequest()
                .withSource("joaovictor.20739@gmail.com")
                .withDestination(new Destination().withToAddresses(to))
                .withMessage(new Message()
                        .withSubject(new Content(subject))
                        .withBody(new Body().withText(new Content(body)))
                );
        try {
            this.amazonSimpleEmailService.sendEmail(request);
        } catch (AmazonServiceException exception){
            throw new EmailServiceException("Falha ao enviar o email");
        }
    }
}
```