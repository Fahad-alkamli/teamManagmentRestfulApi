package requests_entities.task;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.CommonFunctions;

public class AddHoursToTaskRequest {
	

	@NotNull
	private String session;
	@Min(1)
	private int taskId;
	@Min(1)
	private double hours;
	
	
	
	
	public AddHoursToTaskRequest(String session, int taskId, double hours) {
		super();
		this.session = session;
		this.taskId = taskId;
		this.hours = hours;
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
	
	



	public double getHours() {
		return hours;
	}
	public void setHours(double hours) {
		this.hours = hours;
	}
	public AddHoursToTaskRequest() {
	}




	public String getJson(AddHoursToTaskRequest request)
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
