package requests_entities.user;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.CommonFunctions;

public class ChangePasswordRequestByToken {

	@NotNull
	private	String token,password;

	public ChangePasswordRequestByToken() {
	}

	public ChangePasswordRequestByToken(String token, String password) {
		super();
		this.token = token;
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		if(CommonFunctions.clean(token) ==null || CommonFunctions.clean(token).length()<1 )
		{
			return;
		}
		this.token = CommonFunctions.clean(token);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if(CommonFunctions.clean(password) ==null || CommonFunctions.clean(password).length()<1 )
		{
			return;
		}
		this.password = password;
	}
	
	

    public String getJson(ChangePasswordRequestByToken request)
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
