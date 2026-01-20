package pt.florinhas.marcacoes.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPassword(String to, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Acesso à Plataforma Florinhas");
        message.setText("Foi criada uma conta para si.\n\n" +
                "A sua password inicial é: " + password + "\n\n" +
                "Por favor, altere a sua password após o primeiro login.");

        mailSender.send(message);
        System.out.println("Email enviado para " + to);
    }
}
