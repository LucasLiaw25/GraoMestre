package com.liaw.dev.GraoMestre.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${EMAIL_PASS}")
    private String sendGridApiKey;

    @Value("${spring.mail.username.sender:lucasliaw20@gmail.com}")
    private String fromEmail;

    public void sendActivationEmail(String toEmail, String activationLink) {
        Email from = new Email(fromEmail);
        String subject = "Ative sua conta - Grão Mestre";
        Email to = new Email(toEmail);

        String htmlContent = buildActivationEmailHtml(activationLink);
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                System.err.println("Erro SendGrid API: " + response.getBody());
                throw new RuntimeException("Falha no envio via API SendGrid");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erro de rede ao contactar SendGrid API", ex);
        }
    }

    public void sendChangePasswordEmail(String toEmail, String changePasswordLink) {
        Email from = new Email(fromEmail);
        String subject = "Troque sua senha - Grão Mestre";
        Email to = new Email(toEmail);

        String htmlContent = buildChangePasswordEmailHtml(changePasswordLink);
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                System.err.println("Erro SendGrid API: " + response.getBody());
                throw new RuntimeException("Falha no envio via API SendGrid");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erro de rede ao contactar SendGrid API", ex);
        }
    }

    private String buildChangePasswordEmailHtml(String changePasswordLink) {
        String year = String.valueOf(java.time.Year.now().getValue());

        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="margin: 0; padding: 0; background-color: #fafaf9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; margin: 40px auto;">
                    <tr>
                        <td style="padding: 20px;">
                            <!-- Card Principal -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #ffffff; border: 1px solid #e7e5e4; border-radius: 16px; overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td align="center" style="padding: 40px 20px 24px 20px; border-bottom: 1px solid #f5f5f4;">
                                        <p style="margin: 0 0 6px 0; color: #78716c; font-size: 11px; font-weight: bold; text-transform: uppercase; letter-spacing: 1.5px;">
                                            Painel Administrativo
                                        </p>
                                        <h1 style="margin: 0; color: #1c1917; font-size: 28px; font-family: Georgia, 'Times New Roman', serif; font-weight: bold;">
                                            Grão Mestre
                                        </h1>
                                    </td>
                                </tr>

                                <!-- Conteúdo -->
                                <tr>
                                    <td style="padding: 32px 40px;">
                                        <h2 style="margin: 0 0 16px 0; color: #1c1917; font-size: 18px; font-family: Georgia, 'Times New Roman', serif;">Olá,</h2>
                                        <p style="margin: 0 0 24px 0; color: #44403c; font-size: 15px; line-height: 1.6;">
                                            Precisa redefinir sua senha?. Para alterar sua senha  do <strong>Grão Mestre</strong>, por favor, clique no botão abaixo.
                                        </p>

                                        <!-- Botão -->
                                        <table align="center" border="0" cellpadding="0" cellspacing="0" style="margin: 32px auto;">
                                            <tr>
                                                <td align="center" style="background-color: #292524; border-radius: 12px;">
                                                    <a href="__LINK__" target="_blank" style="display: inline-block; padding: 14px 32px; font-size: 14px; color: #ffffff; text-decoration: none; font-weight: bold;">
                                                        Redefinir a Senha
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>

                                       
                                    </td>
                                </tr>
                            </table>

                            <!-- Footer -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                <tr>
                                    <td align="center" style="padding: 24px 20px; color: #a8a29e; font-size: 12px;">
                                        <p style="margin: 0 0 8px 0;">&copy; __YEAR__ Grão Mestre. Todos os direitos reservados.</p>
                                        <p style="margin: 0;">
                                            <a href="http://localhost:3000" style="color: #78716c; text-decoration: none; font-weight: bold;">Acessar Sistema</a>
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
                .replace("__LINK__", changePasswordLink)
                .replace("__YEAR__", year);
    }

    private String buildActivationEmailHtml(String activationLink) {
        String year = String.valueOf(java.time.Year.now().getValue());

        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="margin: 0; padding: 0; background-color: #fafaf9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; margin: 40px auto;">
                    <tr>
                        <td style="padding: 20px;">
                            <!-- Card Principal -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #ffffff; border: 1px solid #e7e5e4; border-radius: 16px; overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td align="center" style="padding: 40px 20px 24px 20px; border-bottom: 1px solid #f5f5f4;">
                                        <p style="margin: 0 0 6px 0; color: #78716c; font-size: 11px; font-weight: bold; text-transform: uppercase; letter-spacing: 1.5px;">
                                            Painel Administrativo
                                        </p>
                                        <h1 style="margin: 0; color: #1c1917; font-size: 28px; font-family: Georgia, 'Times New Roman', serif; font-weight: bold;">
                                            Grão Mestre
                                        </h1>
                                    </td>
                                </tr>

                                <!-- Conteúdo -->
                                <tr>
                                    <td style="padding: 32px 40px;">
                                        <h2 style="margin: 0 0 16px 0; color: #1c1917; font-size: 18px; font-family: Georgia, 'Times New Roman', serif;">Olá,</h2>
                                        <p style="margin: 0 0 24px 0; color: #44403c; font-size: 15px; line-height: 1.6;">
                                            É um prazer recebê-lo em nossa plataforma. Para confirmar sua conta e ter acesso ao sistema do <strong>Grão Mestre</strong>, por favor, clique no botão abaixo.
                                        </p>

                                        <!-- Botão -->
                                        <table align="center" border="0" cellpadding="0" cellspacing="0" style="margin: 32px auto;">
                                            <tr>
                                                <td align="center" style="background-color: #292524; border-radius: 12px;">
                                                    <a href="__LINK__" target="_blank" style="display: inline-block; padding: 14px 32px; font-size: 14px; color: #ffffff; text-decoration: none; font-weight: bold;">
                                                        Ativar Minha Conta
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Link Alternativo -->
                                        <div style="margin-top: 32px; padding-top: 24px; border-top: 1px solid #f5f5f4;">
                                            <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px;">Se o botão não funcionar, copie e cole o link abaixo no seu navegador:</p>
                                            <a href="__LINK__" style="color: #2563eb; text-decoration: underline; font-size: 12px; word-break: break-all;">__LINK__</a>
                                        </div>
                                    </td>
                                </tr>
                            </table>

                            <!-- Footer -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                <tr>
                                    <td align="center" style="padding: 24px 20px; color: #a8a29e; font-size: 12px;">
                                        <p style="margin: 0 0 8px 0;">&copy; __YEAR__ Grão Mestre. Todos os direitos reservados.</p>
                                        <p style="margin: 0;">
                                            <a href="http://localhost:3000" style="color: #78716c; text-decoration: none; font-weight: bold;">Acessar Sistema</a>
                                        </p>
                                    </td>
                                </tr>
                            </table>
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