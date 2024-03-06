package com.ikn.ums.msteams.utils;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EmailService {
	
	@Autowired
	private JavaMailSender sender;
	
	public boolean sendMail(String to, String subject, String textBody, String[] cc, String[] bcc, MultipartFile file, boolean isHtml) {
		
		boolean flag = false;
	    MimeMessage message = sender.createMimeMessage();
	    
	    try {
	    	MimeMessageHelper helper = new MimeMessageHelper(message,file!=null?true:false);
	    	helper.setTo(to);
	    	helper.setSubject(subject);
	    	//helper.setText(textBody);
	    	helper.setText(textBody, isHtml);
	    	if(cc !=null) {
	    		helper.setCc(cc);	
	    	}
	    	if(bcc != null) {
	    		helper.setBcc(bcc);
	    	}
	    	if(file!=null) {
	    		helper.addAttachment(file.getOriginalFilename(), file);
	    	}
	    	sender.send(message);
	    	flag = true;
	    }catch (Exception e) {
			// TODO: handle exception
	    	flag = false;
	    	e.printStackTrace();		
	    }
		return flag;
		
	}
	public boolean sendMail(String to, String subject, String textBody, boolean isHtml) {
		
		return sendMail(to,subject,textBody,null,null,null, isHtml);
		
	}
	
    public boolean sendMail(String[] to, String subject, String textBody, String[] cc, String[] bcc, MultipartFile file, boolean isHtml) {
		
		boolean flag = false;
	    MimeMessage message = sender.createMimeMessage();
	    
	    try {
	    	MimeMessageHelper helper = new MimeMessageHelper(message,file!=null?true:false);
	    	helper.setTo(to);
	    	helper.setSubject(subject);
	    	//helper.setText(textBody);
	    	helper.setText(textBody, isHtml);
	    	if(cc !=null) {
	    		helper.setCc(cc);	
	    	}
	    	if(bcc != null) {
	    		helper.setBcc(bcc);
	    	}
	    	if(file!=null) {
	    		helper.addAttachment(file.getOriginalFilename(), file);
	    	}
	    	sender.send(message);
	    	flag = true;
	    }catch (Exception e) {
			// TODO: handle exception
	    	flag = false;
	    	e.printStackTrace();		
	    }
		return flag;
		
	}
	public boolean sendMail(String[] to, String subject, String textBody, boolean isHtml) {
		
		return sendMail(to,subject,textBody,null,null,null, isHtml);
		
	}
   
	
	

}
