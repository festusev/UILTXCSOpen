package Outlet;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

public class SendMail {
    private static final String ACCOUNT = "txcsopen@gmail.com";
    private static final String FROM = "txcsopen@gmail.com";
    private static final String HOST = "smtp.gmail.com";
    private static final String PASSWORD = "7NNGztFcKv%&M9nt";

    public static int sendVerification(String to, String code, String vtoken) {
        try {
            new Thread(() -> {
            }).start();
            String[] cmds = {
                    "/bin/sh",
                    "-c",
                    "printf \"Subject: Your TXCSOpen Confirmation Code is: "+code+"\\r\\nMIME-Version: 1.0\\r\\nContent-Type: text/html; charset=utf-8\\r\\n\\r\\n<div style='color:#404040;font-family:sans-serif;font-size:20px;padding:2em;width:30em;margin: auto;/*! border: 0 0 5px 0px rgba(0,0,0,0.12); */border: 1px solid rgba(0,0,0,0.12);'> <p style=' font-size:45px;font-weight:bold;margin:0;color:#404040;'>Welcome to TXCSOpen</p> <p style='color:#404040;'>Enter the following code to confirm your email. It's secret, so be sure not to forward this email.</p> <div style='font-family:monospace;font-size:60px; font-weight:bold;text-align:center;color:#52677b;border: 1px solid rgba(0,0,0,0.12);'>"+code+"</div> <p>Didn't work? Follow this link: <span style='color:#edad3e; font-weight:bold;font-family:monospace;display:block;'>txcsopen.com/uil/verify?vtoken="+vtoken+"</span></p> </div>\" | ssmtp " + to
            };
            Process proc = Runtime.getRuntime().exec(cmds);
            proc.waitFor();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

// Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

// Read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
        /*// Get system properties
        Properties properties = System.getProperties();

        System.out.println("--TO: " + to + " SUBJECT: " + subject + " BODY: " + body);

        // Setup mail server
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");
        // properties.put("mail.smtp.ssl.enable", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties,            new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ACCOUNT, PASSWORD);
            }
        } );

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage mimeMessage = new MimeMessage(session);

            // Set From: header field of the header.
            mimeMessage.setFrom(new InternetAddress(FROM));

            // Set To: header field of the header.
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            mimeMessage.setSubject(subject);

            // Now set the actual message
            mimeMessage.setText(body);

            mimeMessage.setSentDate(new Date());

            System.out.println("sending...");

            Transport.send(mimeMessage);

            System.out.println("Sent message successfully....");
        } catch (Exception mex) {
            mex.printStackTrace();
            return -1;
        }
        return 0;*/
    }
}
