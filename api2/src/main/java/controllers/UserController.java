package controllers;

import java.util.ArrayList;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import db.user.UsersService;
import entity.CommonFunctions;
import entity.User;
import requests_entities.Response;
import requests_entities.project.SessionOnlyRequest;
import requests_entities.user.ChangePasswordRequestByAdmin;
import requests_entities.user.ChangePasswordRequestByToken;
import requests_entities.user.CreateUserRequest;
import requests_entities.user.DeleteUserRequest;
import requests_entities.user.LogoutUserRequest;
import requests_entities.user.ResetPasswordRequest;
import requests_entities.user.UserLoginRequest;
import requests_entities.user.UserLoginResponse;

@RestController
public class UserController {

	@RequestMapping(value="/create_user", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest user)  
	{
		//System.out.println(user.getEmail().toString());
		//System.out.println("This got invoked");
		if(!UsersService.validateAdminSession(user.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception");
			//	throw new UserIsNotAuthorized();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=UsersService.createUser(user);
		if(!response.getState())
		{
			//Exception handling
			//System.out.println("Error couldn't create a user");
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("duplicate user");
		}
		//return userResponse;
		//System.out.println("User has been created successfulys");
		return new ResponseEntity<String>(response.getMessage(),HttpStatus.CREATED);

	}

	@RequestMapping(value="/user_login", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> login(@Valid @RequestBody UserLoginRequest user)  
	{	
		//we make sure the user exists in the system 
		if(!UsersService.userExistsCheckByLogin(user.getEmail(),user.getPassword()))
		{
			//System.out.println("Trying to throw an exception");
			//	throw new UserIsNotAuthorized();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		//Next we start the login process and see either it's successful or not 
		UserLoginResponse response=null;
		//Handle the case where the login failed
		if((response=UsersService.loginAndReturnSession(user)) ==null)
		{
			//Exception handling
			//System.out.println("Error couldn't create a user");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		//return userResponse;
		//System.out.println("Login seccessfuly");

		return new ResponseEntity<String>(response.getJson(response),HttpStatus.OK);	
	}

	@RequestMapping(value="/user_logout", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> logout(@Valid @RequestBody LogoutUserRequest session)
	{
		UsersService.logout(session);
		//System.out.println("logout seccessfuly");
		return new ResponseEntity<String>("",HttpStatus.OK);	
	}

	@RequestMapping(value="/get_all_users", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> getAllUsers(@Valid @RequestBody SessionOnlyRequest request)
	{
		try{
			//Before we call any function we need to validate the session that it actually belongs to a user 
			int userId=UsersService.getUserIdFromSession(request.getSession());
			if(userId==-1 || userId==0)
			{
				//this session doesn't exists 
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}
			ArrayList<User> users=UsersService.getAllUsers(request.getSession(),userId);
			if(users != null && users.size()>0)
			{
				//Response http://www.makeinjava.com/convert-list-objects-to-from-json-using-jackson-objectmapper/
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String response=objectMapper.writeValueAsString(users);
				return new ResponseEntity<String>(response,HttpStatus.OK);	
			}else if(users != null)
			{
				//We simply couldn't find anything 
				return new ResponseEntity<String>("",HttpStatus.OK);	
			}
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
		}
		return new ResponseEntity<String>("",HttpStatus.METHOD_NOT_ALLOWED);	

	}

	@RequestMapping(value="/reset_password", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
	{
		//Check if the email exists
		User user=UsersService.userExistsCheckByEmail(request.getEmail());
		if(user ==null)
		{
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		//The user exists
		Response response=UsersService.requestPasswordReset(user);

		if(response.getState())
		{
			return new ResponseEntity<String>("",HttpStatus.OK);	
		}
		return new ResponseEntity<String>("",HttpStatus.METHOD_NOT_ALLOWED);	

	}
	@RequestMapping(value="/change_password_by_token", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	
	public ResponseEntity<String> changePasswordByToken(@Valid @RequestBody ChangePasswordRequestByToken request)
	{
		try{
			Response response=UsersService.changePasswordWithToken(request);
			if(response.getState())
			{
				return ResponseEntity.status(HttpStatus.OK).body(null);
			}else{
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response.getJson(response));
			}
		}catch(Exception e)
		{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(e.getMessage());
		}

	}
	
	@RequestMapping(value="/change_password_by_admin", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> changePasswordByAdmin(@Valid @RequestBody ChangePasswordRequestByAdmin request)
	{
		try{
		//Validate the admin first
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception");
			//	throw new UserIsNotAuthorized();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		Response response=UsersService.changeUserPassword(request.getUserId(),request.getNewPassword());
		if(response.getState())
		{
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}catch(Exception e)
	{
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(e.getMessage());
	}
		
	}

	@RequestMapping(value="/delete_user", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> deleteUser(@Valid @RequestBody DeleteUserRequest request)
	{
		try{
		//Validate the admin first
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception");
			//	throw new UserIsNotAuthorized();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		Response response=UsersService.deleteUser(request.getUserId());
		if(response.getState())
		{
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}catch(Exception e)
	{
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(e.getMessage());
	}
		
	}
	


}
