package requests_entities.user;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class UserLoginRequest {


	@NotNull
	private String Email,Password;


	public UserLoginRequest( String email, String password)
	{
		this.Email = CommonFunctions.clean(email);
		this.Password = password;

	}


	public UserLoginRequest() {}


	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		if(CommonFunctions.clean(email).length()<1)
		{
			//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		Email = email;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		if(CommonFunctions.clean(password).length()<1)
		{
			//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		Password = password;
	}

	public String getJson(UserLoginRequest request)
	{
		try{
			ObjectMapper mapper = new ObjectMapper();

			String jsonInString = mapper.writeValueAsString(request);
			return jsonInString;

		}catch(Exception e)
		{
			System.out.println(e.getMessage());

		}
		return null;
	}

}
