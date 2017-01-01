package requests_entities.task;

import javax.validation.constraints.NotNull;

import entities.CommonFunctions;
import entities.Task;

public class UpdateTaskRequest extends Task{

	@NotNull
	private String session;

	public UpdateTaskRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public UpdateTaskRequest(int project_id, int task_id, double done_total_hours, String task_summary,
			String task_start_date, String task_end_date, boolean completed,String session) {
		super(project_id, task_id, done_total_hours, task_summary, task_start_date, task_end_date, completed);
		this.session=session;
		// TODO Auto-generated constructor stub
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		if(CommonFunctions.clean(session) ==null || CommonFunctions.clean(session).length()<1)
		{
			return;
		}
		this.session = session;
	}
	
	
    public UpdateTaskRequest(String session,Task task) {
        super(task.getProject_id(), task.getTask_id(), task.getDone_total_hours(), task.getTask_summary(), task.getTask_start_date(), task.getTask_end_date(), task.isCompleted());
        this.session=session;
    }
	
	
	
	
}
