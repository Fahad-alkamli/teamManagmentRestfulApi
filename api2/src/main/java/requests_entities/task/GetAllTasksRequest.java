package requests_entities.task;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class GetAllTasksRequest {
	@NotNull
	private String session;

	
	public GetAllTasksRequest() {
		super();
	}

	public GetAllTasksRequest(String session) {
		super();
		this.session = session;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
        if(CommonFunctions.clean(session)==null || CommonFunctions.clean(session).length()<1)
        {
            return;
        }
		this.session = session;
	}
	

	public String getJson(GetAllTasksRequest request)
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
