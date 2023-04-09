package com.smart.service;



import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService 
{
	@Autowired
	private JavaMailSender javaMailSender;
	
	String from = "abc@gmail.com";
	public boolean sendEmail(String subject, String message, String to)
	{
		boolean f = true ;
//		Properties properties = System.getProperties();
//		properties.put("mail.smtp.host", "smtp.gmail.com");
//		properties.put("mail.smtp.port", "465");
//		properties.put("mail.smtp.ssl.enable", "true");
//		properties.put("mail.smtp.auth", "true");
//		
//		
//		Session session = Session.getInstance(properties, new Authenticator() {
//
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				// TODO Auto-generated method stub
//				return new PasswordAuthentication(from,"***");
//			}
//		
//		
//		}) ;
//		session.setDebug(true);
		
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
		try 
		{
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
//			helper.setText(message);
			helper.setText(message, true);
			
			javaMailSender.send(mimeMessage);
			System.out.println("Sent Successfully...");
			
		} catch (Exception e) {
			f=false;
			e.printStackTrace();
		}
		return f ;
	}
}
