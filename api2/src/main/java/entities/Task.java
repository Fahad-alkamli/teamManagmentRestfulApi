package entities;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import requests_entities.task.CreateTaskRequest;

public class Task {

	@NotNull
	private int project_id;
	private int task_id;
	private double done_total_hours=0;
	@NotNull
	private String task_summary,task_start_date,task_end_date;
	private boolean completed=false;
	
	
	public Task() {
		super();
	}

	
	public Task(int project_id, int task_id, double done_total_hours, String task_summary,
			String task_start_date, String task_end_date,boolean completed) {
		super();
		this.project_id = project_id;
		this.task_id = task_id;
		this.done_total_hours = done_total_hours;
		this.task_summary = task_summary;
		this.task_start_date = task_start_date;
		this.task_end_date = task_end_date;
		this.completed=completed;
	}
	public Task(CreateTaskRequest taskRequest,int task_id)
	{
		this.task_id = task_id;
		this.project_id = taskRequest.getProject_id();
		this.done_total_hours = taskRequest.getDone_total_hours();
		this.task_summary = taskRequest.getTask_summary();
		this.task_start_date = taskRequest.getTask_start_date();
		this.task_end_date = taskRequest.getTask_end_date();
		this.completed=taskRequest.isCompleted();
	}

	public int getProject_id() {
		return project_id;
	}
	public void setProject_id(int project_id) {
		this.project_id = project_id;
	}
	public int getTask_id() {
		return task_id;
	}
	public void setTask_id(int task_id) {
		this.task_id = task_id;
	}
	public double getDone_total_hours() {
		return done_total_hours;
	}
	public void setDone_total_hours(double done_total_hours) {
		this.done_total_hours = done_total_hours;
	}
	public String getTask_summary() {
		return task_summary;
	}
	public void setTask_summary(String task_summary) {
		if(CommonFunctions.clean(task_summary).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.task_summary = task_summary;
	}
	public String getTask_start_date() {
		return task_start_date;
	}
	public void setTask_start_date(String task_start_date) {
		if(CommonFunctions.clean(task_start_date).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.task_start_date = task_start_date;
	}
	public String getTask_end_date() {
		return task_end_date;
	}
	public void setTask_end_date(String task_end_date) {
		if(CommonFunctions.clean(task_end_date).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.task_end_date = task_end_date;
	}
	
	public String getJson(Task request)
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


	public boolean isCompleted() {
		return completed;
	}


	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	
}
