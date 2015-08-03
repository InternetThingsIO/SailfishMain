package io.internetthings.sailfish;

/**
 * Created by Dev on 7/4/2015.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;
import java.util.Properties;

public class EmailSender extends javax.mail.Authenticator {

    private final String logTag = this.getClass().getName();

    private String user;
    private String password;
    private Session session;

    public EmailSender() {

        this.user = "george@internetthings.io";
        this.password = "internetthings123";

        String mailhost = "smtpcorp.com";

        Properties props = new Properties();

        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "8465");
        props.put("mail.smtp.socketFactory.port", "8465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.debug", "false");

        session = Session.getInstance(props, this);

    }
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public void sendEmail(Context context){

        final String recipient = SailfishPreferences.getEmail(context);

        //bail if we don't have an email for some reason
        if (recipient == null) {
            Log.e(logTag, "Couldn't send an email because we had no email address");
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override public Void doInBackground(Void... arg) {

                sendEMailRecipient(recipient);
                return null;
            }
        }.execute();
    }

    private void sendEMailRecipient(String recipient) {

        StringBuilder sb = new StringBuilder();

        try {
            // Create a URL for the desired page
            URL url = new URL("http://www.internetthings.io/notice_resources/email/index.html");

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(sb.toString().getBytes(), "text/html"));
            message.setSender(new InternetAddress("no-reply@internetthings.io", "Internet Things", "utf-8"));
            message.setSubject("Notice is here!");
            message.setDataHandler(handler);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            Transport.send(message);

        } catch (MalformedURLException e) {
            Log.e(logTag, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(logTag, "IOException: " + e.getMessage());
        } catch(MessagingException e){
            Log.e(logTag, "Messaging exception: " + e.getMessage());
        }



    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }

}
