package db.poject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Statement;

import db.DBUtility;
import db.task.TaskService;
import db.user.UsersService;
import db.user.user_extra_functions.UsersExtra;
import entities.CommonFunctions;
import entities.Project;
import entities.User;
import logger.Logger;
import requests_entities.Response;
import requests_entities.project.*;

public class ProjectService {

	static Logger log = new Logger(ProjectService.class.getName());
	public ProjectService() {

	}


	public static Project createProject(CreateProjectRequest projectRequest)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("INSERT INTO project (project_name,start_date,end_date,enabled) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, projectRequest.getProjectName());   
			preparedStatement.setString(2, projectRequest.getStart_date());   
			preparedStatement.setString(3, projectRequest.getEnd_date());   
			preparedStatement.setBoolean(4, projectRequest.isEnabled());   
			int count= preparedStatement.executeUpdate();

			if(count>0)
			{
				//Wait let's get the id and return the ID
				ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
				if (generatedKeys.next()) 
				{
					int projectId=generatedKeys.getInt(1);

					CommonFunctions.closeConnection(preparedStatement);

					return getProject(projectId);
				}

			}
			////System.out.println("Check this: "+count);
		}catch(Exception e)
		{

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}

		if(preparedStatement != null)
		{
			CommonFunctions.closeConnection(preparedStatement);

		}


		return null;
	}

	public static Response enableProject(String projectId)
	{
		PreparedStatement preparedStatement = null;
		try{

			//? i guess we have to make sure that the project actually exists before we go for an update 
			if(!projectExists(projectId))
			{
				return new Response(false,"There is no project with this ID.");

			}

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update project set enabled=? where project_id=?");
			preparedStatement.setBoolean(1, true);
			preparedStatement.setString(2, projectId);
			int count= preparedStatement.executeUpdate();
			if(count>0)
			{
				//We need to return the updated records so i shall query the database again 
				Project tempResponse=getProject(Integer.parseInt(projectId));
				if(tempResponse != null)
				{
					CommonFunctions.closeConnection(preparedStatement);

					return new Response(true,tempResponse.getJson(tempResponse));
				}

			}
			////System.out.println("Check this: "+count);

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			////System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static Response disableProject(String projectId)
	{
		PreparedStatement preparedStatement=null;
		try{
			//? i guess we have to make sure that the project actually exists before we go for an update 
			if(!projectExists(projectId))
			{
				return new Response(false,"There is no project with this ID.");

			}
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update project set enabled=? where project_id=?");
			preparedStatement.setBoolean(1, false);
			preparedStatement.setString(2, projectId);
			int count= preparedStatement.executeUpdate();
			if(count>0)
			{
				//We need to return the updated records so i shall query the database again 
				Project tempResponse=getProject(Integer.parseInt(projectId));
				if(tempResponse != null)
				{
					CommonFunctions.closeConnection(preparedStatement);

					return new Response(true,tempResponse.getJson(tempResponse));
				}
			}
			////System.out.println("Check this: "+count);

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);

			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static boolean projectExists(String projectId)
	{
		PreparedStatement preparedStatement =null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from project where project_id=?");
			preparedStatement.setString(1, projectId);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				////System.out.println("This project id exists");
				CommonFunctions.closeConnection(preparedStatement);

				return true;
			}else{

				////System.out.println("Check this out: "+set.getFetchSize());
			}


		}catch(Exception e)
		{

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

		}
		CommonFunctions.closeConnection(preparedStatement);
		return false;
	}

	public static Response addMemberToProject(String projectId,String memberId)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("INSERT INTO project_memebers(project_id,user_id) VALUES (?,?)");
			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, CommonFunctions.clean(projectId));   
			preparedStatement.setString(2, CommonFunctions.clean(memberId));   
			int count= preparedStatement.executeUpdate();
			if(count>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");
			}
			////System.out.println("Check this: "+count);
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			if(e.getMessage().toLowerCase().contains("duplicate"))
			{
				CommonFunctions.closeConnection(preparedStatement);

				return new Response(false,"The user is already enrolled in this project");
			}
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static Response getAllProjects(SessionOnlyRequest request)
	{
		//If the user is an admin then i will fetch all the projects if not then only the projects that he/she is enrolled in
		//Furthermore, if not admin they can see only enabled projects 
		ArrayList<Project> temp=new ArrayList<Project>();
		PreparedStatement preparedStatement=null;
		try{
			if(UsersService.validateAdminSession(CommonFunctions.clean(request.getSession())))
			{
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select * from project");
			}else{
				//First we get the user id that this session belongs too
				int userId=UsersExtra.getUserIdFromSession(CommonFunctions.clean(request.getSession()));
				if(userId==-1)
				{
					//We couldn't find the user id 
					CommonFunctions.closeConnection(preparedStatement);

					return new Response(false,"We couldn't find the user id");
				}
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select * from project where project_id in(select project_id from project_memebers where user_id=?) and enabled=1");
				preparedStatement.setInt(1,userId);
			}

			ResultSet result=preparedStatement.executeQuery();
			boolean hasResult=false;
			while(result.next())
			{
				temp.add(new Project(result.getInt("project_id"),result.getString("project_name"),result.getString("start_date"),result.getString("end_date"),result.getBoolean("enabled")));
				hasResult=true;
			}

			if(!hasResult)
			{
				//The query result was 0
				//System.out.println("The query result was 0");
				CommonFunctions.closeConnection(preparedStatement);

				return new Response(true,"");
			}

			CommonFunctions.closeConnection(preparedStatement);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response=objectMapper.writeValueAsString(temp);
			return new Response(true,response);
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}

	}

	public static Project getProject(int projectId)
	{
		PreparedStatement preparedStatement=null;
		try{


			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from project where project_id =?");
			preparedStatement.setInt(1,projectId);
			ResultSet result=preparedStatement.executeQuery();

			if(result.next())
			{
				Project project=new Project(result.getInt("project_id"),result.getString("project_name"),result.getString("start_date"),result.getString("end_date"),result.getBoolean("enabled"));
				CommonFunctions.closeConnection(preparedStatement);

				return project;
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		CommonFunctions.closeConnection(preparedStatement);

		return null;
	}

	public static Response getAllUsersThatBelongToProject(int projectId)
	{
		PreparedStatement preparedStatement =null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_id in(select user_id from project_memebers where project_id=?)");
			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setInt(1, projectId);   
			ResultSet result=preparedStatement.executeQuery();

			ArrayList<User> usersList=new ArrayList<User>();
			while(result.next())
			{
				//usersList
				usersList.add(new User(result.getInt("user_id"),result.getString("user_name"),result.getString("user_email"),result.getBoolean("admin")));
			}

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response=objectMapper.writeValueAsString(usersList);
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(true,response);
		}catch(Exception e)
		{

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
	}

	public static Response removeUsersFromProject(RemoveUserFromProject request)
	{
		PreparedStatement preparedStatement=null;

		try{
			int count=0;
			for(String userId:request.getUsersId())
			{
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("DELETE FROM project_memebers WHERE project_id=? && user_id=?");
				////System.out.println("User name: "+user.getName().toString());
				preparedStatement.setString(1, CommonFunctions.clean(request.getProjectId()));   
				preparedStatement.setString(2, CommonFunctions.clean(userId));   
				count+=preparedStatement.executeUpdate();
				CommonFunctions.closeConnection(preparedStatement);
				preparedStatement=null;

			}

			//Check how many queries got executed correctly 
			if(count==request.getUsersId().size())
			{
				return new Response(true,"");
			}else{
				return new Response(false,"Successfully removed: "+Integer.toString(count));
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}

	public static Response removeUsersFromAllProjects(String userId)
	{
		PreparedStatement preparedStatement=null;

		try{
			int count=0;
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("DELETE FROM project_memebers WHERE user_id=?");
			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, CommonFunctions.clean(userId));   
			count+=preparedStatement.executeUpdate();
			CommonFunctions.closeConnection(preparedStatement);
			preparedStatement=null;

			//Check how many queries got executed correctly 
			if(count>0)
			{
				return new Response(true,"");
			}else{
				return new Response(false,"Successfully removed: "+Integer.toString(count));
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}

	public static User doesUserBelongToProject(int userId,int projectId)
	{
		PreparedStatement preparedStatement=null;

		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select *  FROM project_memebers WHERE user_id=? and project_id=?");
			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setInt(1, userId);   
			preparedStatement.setInt(2, projectId);   
			ResultSet result=preparedStatement.executeQuery();
			if(result.next())
			{
				User user=UsersExtra.userExistsCheckById(result.getInt("user_id"));
				if(user != null)
				{
					return user;
				}
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		return null;
	}

	public static Project updateProject(UpdateProjectRequest projectRequest)
	{
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("UPDATE project SET project_name=?,start_date=?,end_date=?,enabled=? WHERE project_id=?");

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, projectRequest.getProjectName());   
			preparedStatement.setString(2, projectRequest.getStart_date());   
			preparedStatement.setString(3, projectRequest.getEnd_date());   
			preparedStatement.setBoolean(4, projectRequest.isEnabled());   
			preparedStatement.setString(5, projectRequest.getProjectId());   
			int count= preparedStatement.executeUpdate();

			if(count>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return getProject(Integer.parseInt(projectRequest.getProjectId()));
			}
			//System.out.println("Check this: "+count);
		}catch(Exception e)
		{

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}

		if(preparedStatement != null)
		{
			CommonFunctions.closeConnection(preparedStatement);

		}


		return null;
	}

	public static Response deleteProject(DeleteProjectRequest request)
	{
		PreparedStatement preparedStatement=null;
		try{
			//First we need to detach all the users that belong to that project first then we can delete the project 
			//This also means that we have to do this for the tasks table later on 
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from project_memebers where project_id=?");
			preparedStatement.setString(1, request.getProjectId());   
			preparedStatement.executeUpdate();
			CommonFunctions.closeConnection(preparedStatement);
			//we need to delete the association with the Task tables, 
			TaskService.deleteAllTasksForProject(Integer.parseInt(request.getProjectId()));
			//Now we start deleting the project itself.
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from project where project_id=?");

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, request.getProjectId());   
			int count= preparedStatement.executeUpdate();

			if(count>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");
			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}
			////System.out.println("Check this: "+count);

		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}
}
