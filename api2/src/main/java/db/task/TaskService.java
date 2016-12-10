package db.task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Statement;

import db.DBUtility;
import db.poject.ProjectService;
import db.user.UsersService;
import db.task.task_extra_functions.TaskExtra;
import entity.CommonFunctions;
import entity.Task;
import entity.User;
import logger.Logger;
import requests_entities.Response;
import requests_entities.task.AssignTaskToUserRequest;
import requests_entities.task.CreateTaskRequest;
import requests_entities.task.RemoveUsersFromTaskRequest;
import requests_entities.task.UpdateTaskRequest;

public class TaskService {

	static Logger log = new Logger(TaskService.class.getName());
	
	public static Response createTask(CreateTaskRequest request)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("INSERT INTO tasks(project_id, task_summary, task_start_Date, task_end_Date, done_total_hours) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setInt(1, request.getProject_id());   
			preparedStatement.setString(2,request.getTask_summary());   
			preparedStatement.setString(3, request.getTask_start_date());   
			preparedStatement.setString(4, request.getTask_end_date());   
			preparedStatement.setDouble(5,request.getDone_total_hours());
			int count= preparedStatement.executeUpdate();

			if(count>0)
			{
				//Wait let's get the id and return the ID
				ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
				if (generatedKeys.next()) 
				{
					int taskId=generatedKeys.getInt(1);

					CommonFunctions.closeConnection(preparedStatement);

					//First we get a new Task object and give it the request and the generated id 
					Task task=new Task(request,taskId);
					return new Response(true,task.getJson(task));
					//return getProject(projectId);
				}else{
					return new Response(false,"");
				}

			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
	}

	public static Response getAllTasks(String session,int userId)
	{	
		try{
			//If the user is an Admin then we fetch all tasks 
			if(UsersService.validateAdminSession(session))
			{
				return TaskExtra.getAllTasksForAnAdmin();
			}else{
				return TaskExtra.getAllTasksForUserResponse(userId);
			}
			//Otherwise we fetch only tasks assigned to the user 

		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}

	}

	public static Response deleteTask(int taskId)
	{
		PreparedStatement preparedStatement=null;
		try{

			//Does task exists?
			if(TaskExtra.getTaskById(Integer.toString(taskId))==null)
			{
				////System.out.println("Task doesn't exists");
				return new Response(false,"");
			}
			//is the task assigned to somebody? delete that assignment first 
			if(TaskExtra.isTaskAssignedToSomeBody(Integer.toString(taskId)))
			{
				//Delete all the assignments
				if(TaskExtra.deleteAllAssignmentsFromATask(Integer.toString(taskId))==false)
				{
					////System.out.println("deleteAllAssignmentsFromATask failed2");
					return new Response(false,"");
				}
			}
			//Now finally delete the task 
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from tasks where task_id=?");

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setInt(1,taskId);   
			int count=preparedStatement.executeUpdate();
			if(count>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				////System.out.println("Task has been deleted");
				return new Response(true,"");
			}else{
				CommonFunctions.closeConnection(preparedStatement);
				////System.out.println("Task has not been deleted");
				return new Response(false,"");
			}




		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());	
		}
	}

	public static Response assignTaskToUser(AssignTaskToUserRequest request)
	{
		PreparedStatement preparedStatement=null;

		try{
			if(UsersService.userExistsCheckById((request.getUserId()))==null)
			{
				return new Response(false,"User doesn't exists");
			}
			int count=0;
			for(int taskId:request.getTaskIds())
			{
				//check if the task exists 
				Task task=TaskExtra.getTaskById(Integer.toString(taskId));
				if(task==null)
				{

					//return new Response(false,"Task Doesn't exists");
					continue;
				}

				//We need to make sure that the user is assigned to that project before giving him/her a task that belong to that project?!
				
				if(ProjectService.doesUserBelongToProject(request.getUserId(),task.getProject_id())==null)
				{
					return new Response(false,"User doesn't belong to the project");
				}

				//Finally we assign the task to the user 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("INSERT INTO assign_task_to_user(task_id,user_id) VALUES (?,?)");

				////System.out.println("User name: "+user.getName().toString());
				preparedStatement.setInt(1, taskId);   
				preparedStatement.setInt(2,request.getUserId());   
				count+=preparedStatement.executeUpdate();
				CommonFunctions.closeConnection(preparedStatement);
				preparedStatement=null;
			}
			if(count == request.getTaskIds().size())
			{
				//CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");
			}else if(count>0)
			{
				return new Response(true,"Some tasks couldn't be assigned to a user");
			}
		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());	
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static Response updateTask(UpdateTaskRequest task)
	{
		PreparedStatement preparedStatement=null;
		boolean admin=false;
		try{
			if(UsersService.validateAdminSession(task.getSession()))
			{
				admin=true;
			}else if(TaskExtra.doesTaskBelongToUser((UsersService.getUserIdFromSession(task.getSession())),(task.getTask_id()))==false)
			{
				return new Response(false,"User doesn't belong to this task");
			}
			if(admin)
			{
				//Update all the task with the new task 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("UPDATE tasks SET task_summary=?,task_start_Date=?,task_end_Date=?,done_total_hours=?,completed=? where task_id=?");	
				preparedStatement.setString(1, task.getTask_summary());
				preparedStatement.setString(2, task.getTask_start_date());
				preparedStatement.setString(3, task.getTask_end_date());
				preparedStatement.setDouble(4, task.getDone_total_hours());
				preparedStatement.setBoolean(5, task.isCompleted());
				preparedStatement.setInt(6, task.getTask_id());
			}else{
				//Update all the task with the new task 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("UPDATE tasks SET done_total_hours=?,completed=? where task_id=?");	
				preparedStatement.setDouble(1, task.getDone_total_hours());
				preparedStatement.setBoolean(2, task.isCompleted());
				preparedStatement.setInt(3, task.getTask_id());
			}

			int result=preparedStatement.executeUpdate();
			if(result>0)
			{
				//successful 

				Task task2=TaskExtra.getTaskById(Integer.toString(task.getTask_id()));
				if(task2 != null)
				{
					return new Response(true,task2.getJson(task2));	
				}

			}
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
		return new Response(false,"");
	}

	public static Response getTaskMembers(String taskId)
	{
		PreparedStatement preparedStatement=null;

		try{
			if(!TaskExtra.isTaskAssignedToSomeBody(taskId))
			{
				return new Response(false,"Task is not assigned to any user.");
			}

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_id in (select user_id from assign_task_to_user where task_id =?)");	
			preparedStatement.setInt(1, Integer.parseInt(taskId));
			ArrayList<User> users=new ArrayList<>();
			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				User temp=new User();
				temp.setId(result.getInt("user_id"));
				temp.setAdmin(result.getBoolean("admin"));
				temp.setEmail(result.getString("user_email"));
				temp.setName(result.getString("user_name"));
				users.add(temp);
			}
			CommonFunctions.closeConnection(preparedStatement);

			if(users.size()>0)
			{
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String response=objectMapper.writeValueAsString(users);
				return new Response(true,response);
			}
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static Response removeUsersFromTask(RemoveUsersFromTaskRequest request)
	{
		PreparedStatement preparedStatement=null;
		try{
			boolean valid=false;
			for(int userId:request.getUserId())
			{
				if(!TaskExtra.doesTaskBelongToUser((userId), (request.getTaskId())))
				{
					continue;
				}
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("delete from assign_task_to_user where task_id =? and user_id=?");	
				preparedStatement.setInt(1, request.getTaskId());
				preparedStatement.setInt(2, userId);

				if(preparedStatement.executeUpdate()>0)
				{
					//Found at least one user belongs to a task and removed it 
					valid=true;
				}
				CommonFunctions.closeConnection(preparedStatement);
			}


			if(valid)
			{
				return new Response(true,"");
			}else{
				return new Response(false,"");
			}

		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
	}

	public static Response deleteAllTasksForProject(int projectId)
	{
		PreparedStatement preparedStatement=null;
		try{
			ArrayList<Integer> tasksToGetRemoved=new ArrayList<Integer>();

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from task where project_id=?");	
			preparedStatement.setInt(1, projectId);
			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				tasksToGetRemoved.add(result.getInt("task_id"));

			}
			if(tasksToGetRemoved.isEmpty())
			{
				return new Response(false,"");	
			}
			//Now that we have all the tasks that belong to that project all that we need to do it illiterate through them and delete them one by one.
			for(int temp:tasksToGetRemoved)
			{
				if(deleteTask(temp).getState())
				{
					//System.out.println("Task: "+temp+" ,got deleted");
				}

			}
			return new Response(true,"");

		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());	
		}
	}


	public static Response removeTheUserFromAllTasks(int userId)
	{
		//First we get all the tasks 
		ArrayList<Task> tasks=TaskExtra.getAllTasksForUser(Integer.toString(userId));
		if(tasks == null ||tasks.isEmpty())
		{
			return new Response(false,"");
		}
		//we remove each association 
		for(Task temp:tasks)
		{
			RemoveUsersFromTaskRequest request=new RemoveUsersFromTaskRequest();
			request.setTaskId(temp.getTask_id());
			ArrayList<Integer> ids=new ArrayList<Integer>();
			ids.add(userId);
			request.setUserId(ids);
			removeUsersFromTask(request);
		}


		return new Response(false,"");
	}
}


