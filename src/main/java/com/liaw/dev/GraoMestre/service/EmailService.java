package com.liaw.dev.GraoMestre.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationEmail(String toEmail, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lucasliaw20@gmail.com"); // Seu e-mail de remetente
        message.setTo(toEmail);
        message.setSubject("Ative sua conta Grão Mestre!");
        message.setText("Olá!\n\nObrigado por se registrar no Grão Mestre. Por favor, clique no link abaixo para ativar sua conta:\n"
                + activationLink + "\n\nSe você não solicitou este registro, por favor, ignore este e-mail.\n\nAtenciosamente,\nEquipe Grão Mestre");
        mailSender.send(message);
    }
}