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

            helper.setFrom("lucasliaw20@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Ative sua conta Grão Mestre");

            String htmlContent = buildActivationEmailHtml(activationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail de ativação", e);
        }
    }

    private String buildActivationEmailHtml(String activationLink) {
        String year = String.valueOf(java.time.Year.now().getValue());
        
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="margin: 0; padding: 0; background-color: #FDFCF0; font-family: 'Times New Roman', Times, serif;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border: 1px solid #E5E1DA;">
                    <tr>
                        <td align="center" style="padding: 40px 0; border-bottom: 1px solid #F2EFE9;">
                            <h1 style="margin: 0; color: #4A3728; font-size: 32px; letter-spacing: 2px; text-transform: uppercase; font-weight: lighter;">
                                Grão Mestre
                            </h1>
                            <p style="margin: 5px 0 0 0; color: #8C7B6E; font-size: 12px; letter-spacing: 3px; text-transform: uppercase;">
                                Cafés de Origem
                            </p>
                        </td>
                    </tr>
                    
                    <tr>
                        <td style="padding: 50px 40px;">
                            <p style="color: #4A3728; font-size: 18px; margin-bottom: 25px;">Olá,</p>
                            <p style="color: #635A52; font-size: 16px; line-height: 1.8; margin-bottom: 30px;">
                                É um prazer recebê-lo em nossa curadoria. Para confirmar sua presença entre os apreciadores do <strong>Grão Mestre</strong>, por favor, valide sua conta através do convite abaixo.
                            </p>
                            
                            <table align="center" border="0" cellpadding="0" cellspacing="0">
                                <tr>
                                    <td align="center" bgcolor="#4A3728" style="border-radius: 2px;">
                                        <a href="__LINK__" target="_blank" style="padding: 15px 35px; font-size: 14px; color: #ffffff; text-decoration: none; font-weight: bold; display: inline-block; text-transform: uppercase; letter-spacing: 1px;">
                                            Ativar Conta
                                        </a>
                                    </td>
                                </tr>
                            </table>

                            <p style="color: #635A52; font-size: 14px; line-height: 1.8; margin-top: 40px; border-top: 1px solid #F2EFE9; padding-top: 20px;">
                                Se preferir, copie o link em seu navegador:<br>
                                <a href="__LINK__" style="color: #8C7B6E; text-decoration: underline; font-size: 12px;">__LINK__</a>
                            </p>
                        </td>
                    </tr>

                    <tr>
                        <td align="center" style="padding: 30px; background-color: #FDFCF0; color: #8C7B6E; font-size: 11px; letter-spacing: 1px;">
                            <p style="margin: 0 0 10px 0;">&copy; __YEAR__ GRÃO MESTRE. TODOS OS DIREITOS RESERVADOS.</p>
                            <p style="margin: 0;">
                                <a href="http://localhost:3000" style="color: #4A3728; text-decoration: none;">SITE</a> | 
                                <a href="mailto:lucasliaw20@gmail.com" style="color: #4A3728; text-decoration: none;">CONTATO</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
            .replace("__LINK__", activationLink)
            .replace("__YEAR__", year);
    }
}