package requests_entities.task;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeleteTaskRequest {
	@NotNull
	private String adminSession;
	@NotNull
	@Min(1)
	private int taskId;

	
	public DeleteTaskRequest() {

	}

	
	public DeleteTaskRequest(String adminSession, int taskId) {
		super();
		this.adminSession = adminSession;
		this.taskId = taskId;
	}


	public String getAdminSession() {
		return adminSession;
	}


	public void setAdminSession(String adminSession) {
		this.adminSession = adminSession;
	}


	public int getTaskId() {
		return taskId;
	}


	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}


	public String getJson(DeleteTaskRequest request)
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
