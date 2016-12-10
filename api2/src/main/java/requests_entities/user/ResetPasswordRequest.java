package requests_entities.user;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.CommonFunctions;

public class ResetPasswordRequest {

	@NotNull
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if(CommonFunctions.clean(email)==null || CommonFunctions.clean(email).length()<1)
		{
			return;
			
		}
		this.email = email;
	}

	public ResetPasswordRequest(String email) {
		super();
		this.email = email;
	}
	public ResetPasswordRequest() {
	}
	
	
	
    public String getJson(ResetPasswordRequest request)
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
