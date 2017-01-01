package requests_entities.user;

import static entities.CommonFunctions.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;
import static entities.CommonFunctions.ValidEmail;
public class CreateUserRequest{
	
	//@Pattern(regexp = "^.{1,}\\@.{1,}\\..{1,}$")
	 @NotNull
	 @Pattern(regexp = ValidEmail)
	 private String Email;
	 @NotNull
	 private String Name,Password;
	 @NotNull
	 private String AdminSession;
	 private boolean IsAdmin=false;

	    public boolean isAdmin() {
	        return IsAdmin;
	    }

	    public void setAdmin(boolean admin) {
	        IsAdmin = admin;
	    }

	    //The primary key in this sense will be the email address
		    public CreateUserRequest( String name, String email, String password,String adminSession,boolean isAdmin)
		    {
		        this.Name = name.trim();
		        this.Email = CommonFunctions.clean(email);
		        this.Password = password;
		        this.AdminSession=adminSession;
	            this.IsAdmin=isAdmin;
		    }

	    public String getAdminSession() {
			return AdminSession;
		}

		public void setAdminSession(String adminSession) {
			if(CommonFunctions.clean(adminSession).length()<1)
			{
			//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
				return;
			}
			AdminSession = adminSession;
		}

		public CreateUserRequest() {}

	    public String getName() {
	        return Name;
	    }

	    public void setName(String name) {
	        Name = name;
	    }

	    public String getEmail() {
	        return Email;
	    }

	    public void setEmail(String email) {
	        Email = CommonFunctions.clean(email);
	    }

	    public String getPassword() {
	        return Password;
	    }

	    public void setPassword(String password) {
	        Password = password;
	    }


	    public String getJson(CreateUserRequest request)
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
