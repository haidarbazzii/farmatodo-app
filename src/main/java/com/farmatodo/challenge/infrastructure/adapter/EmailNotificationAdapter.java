package com.farmatodo.challenge.infrastructure.adapter;
import com.farmatodo.challenge.domain.port.out.NotificationPort;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationAdapter implements NotificationPort {

    // NO pongas @Autowired aquí
    private final MailjetClient client;

    @Value("haidarbazzi4@gmail.com")
    private String fromEmail;

    public EmailNotificationAdapter(
            @Value("4013e313007d0c1d6bf1e251f5ec1cfd") String apiKey,
            @Value("edb4acbb2df8f88bda3ba01a05d136e8") String secretKey) {

        // 1. Configuramos las opciones
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(secretKey)
                .build();

        // 2. Aquí creamos el objeto manualmente con "new".
        // Spring no lo inyecta, nosotros lo fabricamos.
        this.client = new MailjetClient(options);
    }

    @Async
    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", fromEmail)
                                            .put("Name", "Farmatodo Challenge"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)))
                                    .put(Emailv31.Message.SUBJECT, subject)
                                    .put(Emailv31.Message.TEXTPART, body)
                            ));

            MailjetResponse response = client.post(request);

            if (response.getStatus() == 200) {
                System.out.println("✅ Email enviado con Mailjet a: " + toEmail);
            } else {
                System.err.println("❌ Error Mailjet Status: " + response.getStatus());
                System.err.println("Detalle: " + response.getData());
            }

        } catch (MailjetException e) {
            System.err.println("❌ Excepción de Mailjet: " + e.getMessage());
            e.printStackTrace();
        }
    }
}