package email_server_setup;


import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import requests_entities.Response;

public class SendEmail {

	//https://www.tutorialspoint.com/java/java_sending_email.htm
	private String userEmail,token;
	private static final String serverEmail="";
	private static final String serverPassword="";
	public SendEmail(String userEmail,String token)
	{
		this.userEmail=userEmail;
		this.token=token;
	}



	public  Response sendMessage() {   
		try{
			Email message = new EmailBuilder()
					.from("TeamManagment App", serverEmail)
					.to(userEmail,userEmail)
					.subject("TeamManagment Forget Password Request")
					.text("Your Token is : "+token)
					.build();

			new Mailer("smtp.gmail.com", 25, serverEmail, serverPassword, TransportStrategy.SMTP_TLS).sendMail(message,false);
			return new Response(true,"");
		}catch(Exception e)
		{
			return new Response(false,"");

		}
	}
}