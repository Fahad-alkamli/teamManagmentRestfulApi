package requests_entities.user;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import static entities.CommonFunctions.NotEmpty;
public class ChangeNickName {

	@NotNull
	@Pattern(regexp = NotEmpty)
	private String session,nickname;

	public ChangeNickName() {
		super();
	}

	public ChangeNickName(String session, String nickname) {
		super();
		this.session = session;
		this.nickname = nickname;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String userName) {
		this.nickname = userName;
	}
	
    public String getJson(ChangeNickName request)
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
