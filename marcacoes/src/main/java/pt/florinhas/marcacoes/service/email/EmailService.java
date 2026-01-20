package pt.florinhas.marcacoes.service.email;

public interface EmailService {
    void sendPassword(String to, String password);
}
