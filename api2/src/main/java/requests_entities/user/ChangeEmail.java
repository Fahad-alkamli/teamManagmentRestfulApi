package requests_entities.user;

import static entities.CommonFunctions.NotEmpty;
import static entities.CommonFunctions.ValidEmail;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class ChangeEmail {
	
	@NotNull
	@Pattern(regexp = NotEmpty)
	private String session,password;
	@NotNull
	@Pattern(regexp = ValidEmail)
	private String email;

    public ChangeEmail() {
		super();
	}

	public ChangeEmail(String email, String session,String password) 
	{
		super();
		this.email = email;
		this.session = session;
		this.password=password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = CommonFunctions.clean(email);
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session =  CommonFunctions.clean(session);
	}

	public String getJson(ChangeEmail request)
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
