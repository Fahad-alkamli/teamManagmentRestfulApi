package requests_entities.task;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class SubmitTaskNotCompleteRequest {

	@NotNull
	private String session;
	@Min(1)
	private int taskId;
	
	
	public SubmitTaskNotCompleteRequest() {
	}
	public SubmitTaskNotCompleteRequest(String session, int taskId) {
		super();
		this.session = session;
		this.taskId = taskId;
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
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public String getJson(SubmitTaskNotCompleteRequest request)
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
