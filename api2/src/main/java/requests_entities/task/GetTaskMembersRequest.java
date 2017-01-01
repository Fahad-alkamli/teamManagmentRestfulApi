package requests_entities.task;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class GetTaskMembersRequest {

	@NotNull
	private String taskId,adminSession;

	
	
	public GetTaskMembersRequest() {
		super();
	}

	public GetTaskMembersRequest(String taskId, String adminSession) {
		super();
		this.taskId = taskId;
		this.adminSession = adminSession;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		if(CommonFunctions.clean(taskId)==null || CommonFunctions.clean(taskId).length()<1)
		{
			return;
		}
		this.taskId = taskId;
	}

	public String getAdminSession() {
		return adminSession;
	}

	public void setAdminSession(String adminSession) {
		if(CommonFunctions.clean(adminSession)==null || CommonFunctions.clean(adminSession).length()<1)
		{
			return;
		}
		this.adminSession = adminSession;
	}
	
	public String getJson(GetTaskMembersRequest request)
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
