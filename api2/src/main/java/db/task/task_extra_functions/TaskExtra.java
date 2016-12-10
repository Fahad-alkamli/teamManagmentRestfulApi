package db.task.task_extra_functions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import db.DBUtility;
import db.poject.ProjectService;
import entity.CommonFunctions;
import entity.Project;
import entity.Task;
import entity.TaskListViewElement;
import logger.Logger;
import requests_entities.Response;

public class TaskExtra {
	static Logger log = new Logger(TaskExtra.class.getName());
	public static boolean doesTaskBelongToUser(int userId,int taskId)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from assign_task_to_user where user_id=? and task_id=?");
			preparedStatement.setInt(1, (userId));
			preparedStatement.setInt(2, (taskId));
			ResultSet result2=preparedStatement.executeQuery();

			if(result2.next())
			{
				CommonFunctions.closeConnection(preparedStatement);
				return true;
			}

		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return false;
	}

	public static Task getTaskById(String taskId)
	{	PreparedStatement preparedStatement=null;
	try{
		preparedStatement = DBUtility.getConnection()
				.prepareStatement("select * from tasks where task_id=?");	
		preparedStatement.setInt(1, Integer.parseInt(taskId));
		ResultSet result2=preparedStatement.executeQuery();
		if(result2.next())
		{
			Task task= new Task(result2.getInt("project_id"),result2.getInt("task_id"),result2.getDouble("done_total_hours")
					,result2.getString("task_summary"),result2.getString("task_start_date"),result2.getString("task_end_date")
					,result2.getBoolean("completed"));
			CommonFunctions.closeConnection(preparedStatement);
			return task;
		}

	}catch(Exception e)
	{
		class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
	}
	CommonFunctions.closeConnection(preparedStatement);
	return null;
	}

	public static Response getAllTasksForAnAdmin()
	{
		PreparedStatement preparedStatement=null;
		ArrayList<Integer> projectIds=new ArrayList<Integer>();
		ArrayList<TaskListViewElement> tasks=new ArrayList<TaskListViewElement>();
		try{

			//First we get all the project ids in the tasks database;
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("SELECT project_id FROM tasks GROUP by project_id");
			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				projectIds.add(result.getInt("project_id"));
			}
			if(projectIds.size()<1)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}
			CommonFunctions.closeConnection(preparedStatement);
			for(int temp:projectIds)
			{
				//We close the connection because we need to start a new query 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("SELECT * FROM tasks where project_id=?");
				preparedStatement.setInt(1, temp);
				ResultSet result2=preparedStatement.executeQuery();
				ArrayList<Task> tempTasks=new ArrayList<Task>();
				while(result2.next())
				{
					Task task= new Task(result2.getInt("project_id"),result2.getInt("task_id"),result2.getDouble("done_total_hours")
							,result2.getString("task_summary"),result2.getString("task_start_date"),result2.getString("task_end_date")
							,result2.getBoolean("completed"));

					tempTasks.add(task);
				}
				Project project=ProjectService.getProject(temp);
				//tasks
				TaskListViewElement element=new TaskListViewElement(project,tempTasks);
				tasks.add(element);
				CommonFunctions.closeConnection(preparedStatement);
			}

			//Return tasks;
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response=objectMapper.writeValueAsString(tasks);
			return new Response(true,response);

			//Now i have all the tasks in the order i need , i will just return it 


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}


	public static Response getAllTasksForUserResponse(int userId)
	{
		/*select * from tasks where task_id in(
		select task_id from assign_task_to_user where user_id=1
			)
		 */
		PreparedStatement preparedStatement=null;
		ArrayList<Integer> projectIds=new ArrayList<Integer>();
		ArrayList<TaskListViewElement> tasks=new ArrayList<TaskListViewElement>();
		try{

			//First we get all the project ids in the tasks database;
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from tasks where task_id in(select task_id from assign_task_to_user where user_id=?)");
			preparedStatement.setInt(1, (userId));
			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				projectIds.add(result.getInt("project_id"));
			}
			if(projectIds.size()<1)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");
			}
			CommonFunctions.closeConnection(preparedStatement);
			for(int temp:projectIds)
			{
				//We close the connection because we need to start a new query 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("SELECT * FROM tasks where project_id=? and task_id in(select task_id from tasks where task_id in(select task_id from assign_task_to_user where user_id=?))");
				preparedStatement.setInt(1, temp);
				preparedStatement.setInt(2, (userId));
				ResultSet result2=preparedStatement.executeQuery();
				ArrayList<Task> tempTasks=new ArrayList<Task>();
				while(result2.next())
				{
					Task task= new Task(result2.getInt("project_id"),result2.getInt("task_id"),result2.getDouble("done_total_hours")
							,result2.getString("task_summary"),result2.getString("task_start_date"),result2.getString("task_end_date")
							,result2.getBoolean("completed"));

					tempTasks.add(task);
				}
				Project project=ProjectService.getProject(temp);
				//tasks
				TaskListViewElement element=new TaskListViewElement(project,tempTasks);
				//This make sure that the user receive only tasks that belong to an active project 
				if(project.isEnabledState())
				{
				tasks.add(element);
				}
				CommonFunctions.closeConnection(preparedStatement);
			}
			//WE don't have any tasks 
			if(tasks.isEmpty())
			{
				return new Response(true,"");
			}

			//Return tasks;
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response=objectMapper.writeValueAsString(tasks);
			return new Response(true,response);

			//Now i have all the tasks in the order i need , i will just return it 


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}

	public static boolean isTaskAssignedToSomeBody(String taskId)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from assign_task_to_user where task_id=?");	
			preparedStatement.setInt(1, Integer.parseInt(taskId));
			ResultSet result2=preparedStatement.executeQuery();
			if(result2.next())
			{

				CommonFunctions.closeConnection(preparedStatement);
				////System.out.println("This task is assigned to somebody");
				return true;
			}
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		CommonFunctions.closeConnection(preparedStatement);
		////System.out.println("This task is not assigned to somebody");
		return false;
	}


	public static boolean deleteAllAssignmentsFromATask(String taskId)
	{
		PreparedStatement preparedStatement=null;

		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from assign_task_to_user where task_id=?");	
			preparedStatement.setInt(1, Integer.parseInt(taskId));

			int count=preparedStatement.executeUpdate();
			if(count>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				//	//System.out.println("Assignment has been deleted");
				return true;
			}


		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

		}
		CommonFunctions.closeConnection(preparedStatement);
		////System.out.println("Assignment has not been deleted");
		return false;
	}


	public static ArrayList<Task> getAllTasksForUser(String userid)
	{
		/*select * from tasks where task_id in(
		select task_id from assign_task_to_user where user_id=1
			)
		 */
		PreparedStatement preparedStatement=null;
		ArrayList<Integer> projectIds=new ArrayList<Integer>();
		ArrayList<TaskListViewElement> tasks=new ArrayList<TaskListViewElement>();
		try{

			//First we get all the project ids in the tasks database;
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from tasks where task_id in(select task_id from assign_task_to_user where user_id=?)");
			preparedStatement.setInt(1, Integer.parseInt(userid));
			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				projectIds.add(result.getInt("project_id"));
			}
			if(projectIds.size()<1)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return null;
			}
			CommonFunctions.closeConnection(preparedStatement);
			ArrayList<Task> tempTasks=new ArrayList<Task>();

			for(int temp:projectIds)
			{
				//We close the connection because we need to start a new query 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("SELECT * FROM tasks where project_id=? and task_id in(select task_id from tasks where task_id in(select task_id from assign_task_to_user where user_id=?))");
				preparedStatement.setInt(1, temp);
				preparedStatement.setInt(2, Integer.parseInt(userid));
				ResultSet result2=preparedStatement.executeQuery();
				while(result2.next())
				{
					Task task= new Task(result2.getInt("project_id"),result2.getInt("task_id"),result2.getDouble("done_total_hours")
							,result2.getString("task_summary"),result2.getString("task_start_date"),result2.getString("task_end_date")
							,result2.getBoolean("completed"));

					tempTasks.add(task);
				}
				Project project=ProjectService.getProject(temp);
				//tasks
				TaskListViewElement element=new TaskListViewElement(project,tempTasks);
				tasks.add(element);
				CommonFunctions.closeConnection(preparedStatement);
			}
			return tempTasks;

			//Now i have all the tasks in the order i need , i will just return it 


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return null;
		}
	}
}
