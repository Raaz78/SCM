package com.smart.controller;

import java.security.Principal;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	Random random = new Random(1000);

	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
		int otp = random.nextInt(99999);
		String message = "<div style='border:1px solid #e2e2e2; padding:20px'>"
						+"<h1>"
						+"OTP is "
						+"<b>"+otp
						+"</b>"
						+"</h1>"
						+"</div>" ;
		boolean flag = emailService.sendEmail("OTP from SCM", message, email);
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("We have sent otp to your mail !!","success"));
			return "verify_otp";
		}
		else
		{
			System.out.println("nahi jayega.............................................................");
			session.setAttribute("message", new Message("Check your email Id !!","danger"));
			return "forgot_email_form";
			
		}
		
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session)
	{
		int myotp = (int)session.getAttribute("myotp");
		System.out.println("myotp : "+myotp);
		System.out.println("otp : "+otp);
		String email = (String)session.getAttribute("email");
		if(myotp==otp)
		{
			//password change form
			User user = userRepository.getUserByUserName(email);
			if(user==null)
			{
				//send error message
				session.setAttribute("message", new Message("User does not exist with this email !!","danger"));
				return "forgot_email_form";
			}
			else
			{
				//send change password form
				return "password_change_form";
			}
			
		}
		else 
		{
			session.setAttribute("message", new Message("you have entered wrong otp !!","danger"));
			return "verify_otp";
		}
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session, Principal p)
	{
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(bCryptPasswordEncoder.encode(newPassword));
		userRepository.save(user);
		return "redirect:/signin?change=password changed successfully";
	}
}
