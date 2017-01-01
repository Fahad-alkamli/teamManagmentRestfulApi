package requests_entities.user;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static entities.CommonFunctions.NotEmpty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChangePasswordRequestByUserSession {

	@NotNull
	@Pattern(regexp = NotEmpty)
	private String oldPassword,newPassword,newPassword2,session;
	
	
	
    public ChangePasswordRequestByUserSession() {
		super();
		
	}

	public String getSession() {
		return session;
	}



	public void setSession(String session) {
		this.session = session;
	}



	public ChangePasswordRequestByUserSession(String oldPassword, String newPassword, String newPassword2,String session) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.newPassword2 = newPassword2;
		this.session=session;
	}



	public String getOldPassword() {
		return oldPassword;
	}



	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}



	public String getNewPassword() {
		return newPassword;
	}



	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}



	public String getNewPassword2() {
		return newPassword2;
	}



	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}



	public String getJson(ChangePasswordRequestByUserSession request)
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
