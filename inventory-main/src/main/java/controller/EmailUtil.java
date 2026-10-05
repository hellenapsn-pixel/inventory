package controller;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    public static void enviarCodigo(
            String destinatario,
            String codigo) throws Exception {

        // =========================================
        // DADOS DO E-MAIL
        // =========================================

        String remetente = System.getenv("MAIL_EMAIL");
        String senha = System.getenv("MAIL_PASSWORD");

        if (remetente == null ||
                remetente.trim().isEmpty()) {

            throw new Exception(
                    "MAIL_EMAIL não configurado."
            );
        }

        if (senha == null ||
                senha.trim().isEmpty()) {

            throw new Exception(
                    "MAIL_PASSWORD não configurado."
            );
        }

        // =========================================
        // CONFIGURAÇÃO DO GMAIL
        // =========================================

        Properties props = new Properties();

        props.put(
                "mail.smtp.auth",
                "true"
        );

        props.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        props.put(
                "mail.smtp.host",
                "smtp.gmail.com"
        );

        props.put(
                "mail.smtp.port",
                "587"
        );

        // =========================================
        // SESSÃO
        // =========================================

        Session sessao =
                Session.getInstance(
                        props,
                        new Authenticator() {

                            @Override
                            protected PasswordAuthentication
                            getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        remetente,
                                        senha
                                );
                            }
                        }
                );

        // =========================================
        // CRIAR E-MAIL
        // =========================================

        MimeMessage mensagem =
                new MimeMessage(sessao);

        mensagem.setFrom(
                new InternetAddress(
                        remetente,
                        "Inventory"
                )
        );

        mensagem.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(
                        destinatario
                )
        );

        mensagem.setSubject(
                "Código de verificação - Inventory",
                "UTF-8"
        );

        // =========================================
        // HTML DO E-MAIL
        // =========================================

        String html =
                "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"

                + "<head>"
                + "<meta charset='UTF-8'>"
                + "</head>"

                + "<body style='"
                + "margin:0;"
                + "padding:0;"
                + "background:#14101a;"
                + "font-family:Arial,sans-serif;"
                + "color:white;"
                + "'>"

                + "<div style='"
                + "max-width:600px;"
                + "margin:40px auto;"
                + "background:#202830;"
                + "padding:40px;"
                + "border-radius:15px;"
                + "text-align:center;"
                + "'>"

                + "<h1 style='"
                + "font-size:32px;"
                + "margin-bottom:10px;"
                + "color:#ffffff;"
                + "'>"
                + "Inventory"
                + "</h1>"

                + "<p style='"
                + "font-size:18px;"
                + "color:#dddddd;"
                + "'>"
                + "Verificação de e-mail"
                + "</p>"

                + "<p style='"
                + "font-size:16px;"
                + "color:#cccccc;"
                + "'>"
                + "Use o código abaixo para confirmar seu e-mail:"
                + "</p>"

                + "<div style='"
                + "margin:30px 0;"
                + "padding:20px;"
                + "background:#14181c;"
                + "border-radius:10px;"
                + "border:1px solid #6300c0;"
                + "'>"

                + "<span style='"
                + "font-size:38px;"
                + "font-weight:bold;"
                + "letter-spacing:8px;"
                + "color:#c084fc;"
                + "'>"
                + escapar(codigo)
                + "</span>"

                + "</div>"

                + "<p style='"
                + "font-size:14px;"
                + "color:#999999;"
                + "'>"
                + "Esse código é válido por 24 horas."
                + "</p>"

                + "<p style='"
                + "font-size:14px;"
                + "color:#777777;"
                + "margin-top:30px;"
                + "'>"
                + "Se você não realizou este cadastro, ignore este e-mail."
                + "</p>"

                + "</div>"

                + "</body>"
                + "</html>";

        mensagem.setContent(
                html,
                "text/html; charset=UTF-8"
        );

        // =========================================
        // ENVIAR
        // =========================================

        Transport.send(mensagem);
    }

    // =========================================
    // PROTEGER TEXTO PARA HTML
    // =========================================

    private static String escapar(
            String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}