package requests_entities.user;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class DeleteUserRequest {

	@NotNull
	private String AdminSession,userId;

	
	public DeleteUserRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DeleteUserRequest(String adminSession, String userId) {
		super();
		AdminSession = adminSession;
		this.userId = userId;
	}

	public String getAdminSession() {
		return AdminSession;
	}

	public void setAdminSession(String adminSession) {
		if(CommonFunctions.clean(adminSession)==null || CommonFunctions.clean(adminSession).length()<1)
		{
			return;
		}
	
		AdminSession = adminSession;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		if(CommonFunctions.clean(userId)==null || CommonFunctions.clean(userId).length()<1)
		{
			return;
		}
	
		this.userId = userId;
	}

	
	
    public String getJson(DeleteUserRequest request)
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
