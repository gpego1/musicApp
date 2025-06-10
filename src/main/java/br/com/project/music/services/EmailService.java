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

    public void resetPasswordEmail(String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Redefinição de Senha - Sonora");
            String resetLink = "http://localhost:5173/esquecisenha";
            String emailContent = "Olá,\n\n"
                    + "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n"
                    + "Para redefinir sua senha, por favor, clique no link abaixo.\n"
                    + "Se você não conseguir clicar, copie e cole em uma ferramenta como Postman ou em um formulário de redefinição de senha em nosso site.\n\n"
                    + "Link para redefinição (requer POST com email e newPassword no corpo):\n"
                    + resetLink + "\n\n"
                    + "Se você não solicitou esta redefinição de senha, por favor, ignore este e-mail.\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe Sonora";

            message.setText(emailContent);
            emailSender.send(message);
            System.out.println("E-mail de redefinição de senha enviado para: " + to);
        } catch (MailException e) {
            System.out.println("Erro ao enviar e-mail de redefinição de senha: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendEmail(String to, String nomeEvento){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Confirmação de Reserva para o Evento: " + nomeEvento);
            message.setText("Olá,\n\nSua reserva para o evento '" + nomeEvento + "' foi confirmada com sucesso!\n\nObrigado por escolher nosso serviço.\n\nAtenciosamente,\nEquipe Sonora!");
            emailSender.send(message);
            System.out.println("E-mail de confirmação enviado para: " + to + " sobre o evento: " + nomeEvento);
        } catch (MailException e){
            System.out.println("Erro ao enviar e-mail de confirmação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
