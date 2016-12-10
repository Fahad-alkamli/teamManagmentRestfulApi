package entity;

import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskListViewElement {
	@NotNull
	private Project project;
	@NotNull
	private ArrayList<Task> taskArrayList;

	public TaskListViewElement(Project project, ArrayList<Task> taskArrayList)
	{
		this.project = project;
		this.taskArrayList = taskArrayList;
	}
	public TaskListViewElement()
	{
	}
    public TaskListViewElement(Project project, Task task)
    {
        this.project = project;

        this.taskArrayList=new ArrayList<Task>();
        this.taskArrayList.add(task);
    }

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ArrayList<Task> getTaskArrayList() {
		return taskArrayList;
	}

	public void setTaskArrayList(ArrayList<Task> taskArrayList) {
		this.taskArrayList = taskArrayList;
	}

	public String getJson(TaskListViewElement request)
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
