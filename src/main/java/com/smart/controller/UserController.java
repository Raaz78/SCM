package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

//importing razorpay class
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController 
{
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 
	
	@Autowired
	private UserRepository userRepository ;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	//method for adding common data for pages
	@ModelAttribute
	public void addCommonData( Model m , Principal p)
	{
		
		String email = p.getName();
		User user = userRepository.getUserByUserName(email);
		m.addAttribute("user",user);
		
	}
	
	//dashboard
	@RequestMapping("/index")
	public String dashboard(Model m )
	{
		m.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m)
	{
		m.addAttribute("title","Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form" ;
	}
	
	//process add-contact form
	@PostMapping("/process-contact")
	public String processAddContactForm(@Valid @ModelAttribute("contact") Contact contact, BindingResult result2, 
										Model m, Principal p,@RequestParam("profileImage") MultipartFile file,
										HttpSession session)
	{
		try 
		{
			//processing and uploading file
			String email1 = p.getName();
			User user = userRepository.getUserByUserName(email1);
			if(file.isEmpty())
			{
				//if the file is empty
				contact.setImage("noImage.png");
				System.out.println("File is empty !!");
			}
			else
			{
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("/static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			}
//			if(3>2)
//			{
//				throw new Exception();
//			}
			if(result2.hasErrors())
			{
				System.out.println(result2);
				return "normal/add_contact_form" ;
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			//System.out.println(contact);
			System.out.println("Added to database");
			session.setAttribute("message", new Message("Your Contact is added Successfully ! Add more..", "success"));
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try Again", "danger"));
		}
		return "normal/add_contact_form" ;
	}
	
	//show contacts handler
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal p)
	{
		m.addAttribute("title","Show User Contacts");
		String userName = p.getName();
		User user = userRepository.getUserByUserName(userName);
		//current page - page
		//contacts per page - 3
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts" ;
	}
	//showing particular contact details
	
	@GetMapping("/{cId}/contact")
	public String showSingleContactDetails(@PathVariable("cId") Integer cId, Model m, Principal p)
	{
		System.out.println(cId);
		Optional<Contact> optional = contactRepository.findById(cId);
		Contact contact = optional.get();
		
		String name = p.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		if(user.getId()==contact.getUser().getId())
		{
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail" ;
	}
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model m, Principal p, HttpSession session)
	{
		try {
			Contact contact = this.contactRepository.findById(cId).get();
			
			String name = p.getName();
			User user = this.userRepository.getUserByUserName(name);
			//to de-link the contact with user
			
			
			if(user.getId()==contact.getUser().getId())
			{
				contact.setUser(null);
				//delete photo also
				File deleteFile = new ClassPathResource("/static/image").getFile();
				File file1 = new File(deleteFile,contact.getImage());
				file1.delete();
				this.contactRepository.delete(contact);
				session.setAttribute("message", new Message("Contact deleted successfully...","success"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/show-contacts/0";
		
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid , Model m)
	{
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("title","Update Contact");
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
									Model m, HttpSession session, Principal p)
	{
		try 
		{
			//old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty())
			{
				//delete old photo
				File deleteFile = new ClassPathResource("/static/image").getFile();
				File file1 = new File(deleteFile,oldContactDetails.getImage());
				file1.delete();
				//update new photo	
				File saveFile = new ClassPathResource("/static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByUserName(p.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			System.out.println(contact.getEmail()+contact.getSecondName()+contact.getPhone());
			session.setAttribute("message", new Message("Your contact is updated..","success"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("CONTACT NAME : "+contact.getName());
		System.out.println("Phone No : "+contact.getPhone());
		return "redirect:/user/"+contact.getcId()+"/contact" ;
	}
	
	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	// open settings handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
								@RequestParam("newPassword") String newPassword,Principal p, HttpSession session)
	{
		System.out.println("Old Password"+oldPassword);
		System.out.println("New Password"+newPassword);
		User user = userRepository.getUserByUserName(p.getName());
		if(bCryptPasswordEncoder.matches(oldPassword, user.getPassword()))
		{
			//change the password	
			user.setPassword(bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your Password is successfully changed !!", "success"));
		}
		else
		{
			session.setAttribute("message", new Message("Please enter correct old password !!", "danger"));
			return "redirect:/user/settings";
			
		}
		return "redirect:/user/index";
	}
	
	//creating order for payment
	@PostMapping("/create-order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal p) throws Exception
	{
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		//System.out.println("Hey! Order function executed..");
		var client = new RazorpayClient("33ac", "acasv");
		JSONObject orderRequest = new JSONObject();
	    orderRequest.put("amount",amt*100); // amount in the smallest currency unit
	    orderRequest.put("currency", "INR");
	    orderRequest.put("receipt", "order_rcptid_11");
	    
	    //create new order
	    
	    Order order = client.orders.create(orderRequest);
	    System.out.println(order);
	    
	    //save the order in database
	    MyOrder myOrder = new MyOrder();
	    
	    myOrder.setAmount(order.get("amount")+"");
	    myOrder.setOrderId(order.get("id"));
	    myOrder.setPaymentId(null);
	    myOrder.setStatus("created");
	    myOrder.setUser(this.userRepository.getUserByUserName(p.getName()));
	    myOrder.setReceipt(order.get("receipt"));
	    this.myOrderRepository.save(myOrder);
	    
		return order.toString();
	}
	
	@PostMapping("/update_order") 	
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data)
	{
		System.out.println(data);
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		this.myOrderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
