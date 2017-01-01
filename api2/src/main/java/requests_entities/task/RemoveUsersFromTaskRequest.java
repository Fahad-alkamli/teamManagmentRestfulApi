package requests_entities.task;

import java.util.ArrayList;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class RemoveUsersFromTaskRequest {
	@NotNull
	private String adminSession;

	@NotNull
	@Min(1)
	private int taskId;
	@NotNull
	private ArrayList<Integer>  userId;
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
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public ArrayList<Integer> getUserId() {
		return userId;
	}
	public void setUserId(ArrayList<Integer> userId) {
		if(userId==null || userId.isEmpty())
		{
			return;
		}
		this.userId = userId;
	}
	
   public RemoveUsersFromTaskRequest(String adminSession, int taskId, ArrayList<Integer> userId) {
        this.adminSession = adminSession;
        this.taskId = taskId;
        this.userId = userId;
    }
	public RemoveUsersFromTaskRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getJson(RemoveUsersFromTaskRequest request)
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
