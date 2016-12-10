package requests_entities.project;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.CommonFunctions;

public class SessionOnlyRequest {

	 @NotNull
	 private String session;

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		if(CommonFunctions.clean(session).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.session = session;
	}

	public SessionOnlyRequest(String session) {
		this.session = session;
	}

	public SessionOnlyRequest() {
	}
	 
	
	
    public String getJson(SessionOnlyRequest request)
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
