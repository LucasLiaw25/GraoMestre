package com.liaw.dev.GraoMestre.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationEmail(String toEmail, String activationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("lucasliaw20@gmail.com"); // Seu e-mail de remetente
            helper.setTo(toEmail);
            helper.setSubject("Ative sua conta Grão Mestre!");

            String htmlContent = buildActivationEmailHtml(activationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail de ativação: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de ativação", e);
        }
    }

    private String buildActivationEmailHtml(String activationLink) {
        String rawHtmlTemplate = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ative sua conta Grão Mestre</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f8f5f0; /* Cor de fundo suave */
                        margin: 0;
                        padding: 0;
                        -webkit-text-size-adjust: 100%;
                        -ms-text-size-adjust: 100%;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 12px;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                        overflow: hidden;
                        border: 1px solid #e0e0e0;
                    }
                    .header {
                        background-color: white
                        color: #ffffff;
                        padding: 30px 25px;
                        text-align: center;
                        border-top-left-radius: 12px;
                        border-top-right-radius: 12px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px 25px;
                        color: #333333;
                        line-height: 1.6;
                        font-size: 16px;
                    }
                    .content p {
                        margin-bottom: 15px;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .button {
                        display: inline-block;
                        background-color: #8B4513; /* Um tom de marrom café */
                        color: #ffffff;
                        padding: 14px 28px;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: bold;
                        font-size: 17px;
                        transition: background-color 0.3s ease;
                    }
                    .button:hover {
                        background-color: #6a350f; /* Tom mais escuro no hover */
                    }
                    .footer {
                        background-color: #f0ede8; /* Cor de fundo mais clara para o rodapé */
                        color: #777777;
                        padding: 20px 25px;
                        text-align: center;
                        font-size: 13px;
                        border-bottom-left-radius: 12px;
                        border-bottom-right-radius: 12px;
                        border-top: 1px solid #e0e0e0;
                    }
                    .footer a {
                        color: #8B4513;
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bem-vindo ao Grão Mestre!</h1>
                    </div>
                    <div class="content">
                        <p>Olá!</p>
                        <p>Obrigado por se registrar em nossa plataforma. Para ativar sua conta e começar a explorar o mundo dos cafés especiais, por favor, clique no botão abaixo:</p>
                        <div class="button-container">
                            <a href="__ACTIVATION_LINK__" class="button">Ativar Minha Conta</a>
                        </div>
                        <p>Se o botão não funcionar, você pode copiar e colar o seguinte link em seu navegador:</p>
                        <p><a href="__ACTIVATION_LINK__" style="color: #8B4513; word-break: break-all;">__ACTIVATION_LINK__</a></p>
                        <p>Se você não solicitou este registro, por favor, ignore este e-mail.</p>
                        <p>Atenciosamente,<br>Equipe Grão Mestre</p>
                    </div>
                    <div class="footer">
                        <p>&copy; __CURRENT_YEAR__ Grão Mestre. Todos os direitos reservados.</p>
                        <p><a href="http://localhost:3000">Nosso Site</a> | <a href="mailto:lucasliaw20@gmail.com">Contato</a></p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return rawHtmlTemplate
                .replace("__ACTIVATION_LINK__", activationLink)
                .replace("__CURRENT_YEAR__", String.valueOf(java.time.Year.now().getValue()));
    }
}