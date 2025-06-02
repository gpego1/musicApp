package br.com.project.music.services;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String nomeEvento){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Confirmação de Reserva para o Evento: " + nomeEvento);
            message.setText("Olá,\n\nSua reserva para o evento '" + nomeEvento + "' foi confirmada com sucesso!\n\nObrigado por escolher nosso serviço.\n\nAtenciosamente,\nEquipe de Reservas");
            emailSender.send(message);
            System.out.println("E-mail de confirmação enviado para: " + to + " sobre o evento: " + nomeEvento);
        } catch (MailException e){
            System.out.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
