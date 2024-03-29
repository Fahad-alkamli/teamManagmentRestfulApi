package db.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import com.mysql.jdbc.Statement;

import db.DBUtility;
import db.poject.ProjectService;
import db.task.TaskService;
import db.user.user_extra_functions.PasswordAuthentication;
import db.user.user_extra_functions.UsersExtra;
import email_server_setup.SendEmail;
import entities.CommonFunctions;
import entities.User;
import logger.Logger;
import requests_entities.Response;
import requests_entities.user.*;

public class UsersService {

	 static Logger log = new Logger(UsersService.class.getName());

	public static Response createUser(CreateUserRequest user)
	{
		PreparedStatement preparedStatement =null;

		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("insert into user(user_name,user_email,user_password,admin) values (?, ?, ?,?)",Statement.RETURN_GENERATED_KEYS);

			////System.out.println("User name: "+user.getName().toString());
			preparedStatement.setString(1, user.getName());   
			preparedStatement.setString(2, user.getEmail());
			PasswordAuthentication something=new PasswordAuthentication();
			String password=something.hash(user.getPassword().toCharArray());
			//System.out.println(password);
			//System.out.println(something.authenticate(user.getPassword().toCharArray(), password));
			//Added encryption to the password
			preparedStatement.setString(3,password);
			preparedStatement.setBoolean(4, user.isAdmin());
			int count= preparedStatement.executeUpdate();
			//System.out.println("Check this:"+count);
			if(count>0)
			{
				ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
				if (generatedKeys.next()) 
				{
					int projectId=generatedKeys.getInt(1);
					CommonFunctions.closeConnection(preparedStatement);
					User temp=new User(projectId,user.getName(),user.getEmail(),user.isAdmin());

					return new Response(true,temp.getJson(temp));
				}
			}
		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

			//e.printStackTrace();
			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");

	}

	/*
	 * Validate That this request came from an Admin account 
	 */
	public static boolean validateAdminSession(String adminSession)
	{
		PreparedStatement preparedStatement=null;
		try{

			if(adminSession ==null || adminSession.equals(""))
			{
				return false;
			}
			////System.out.println("Admin session: "+adminSession);
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where session=? and admin=1 ");
			preparedStatement.setString(1, adminSession);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				System.out.println("This user is admin");
				CommonFunctions.closeConnection(preparedStatement);
				return true;
			}else{

				//System.out.println("Check this out: "+set.getFetchSize());
			}


		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return false;
	}

	public static Response userExistsCheckByLogin(String email,String password)
	{
		PreparedStatement preparedStatement =null;
		try{
			User user=UsersExtra.userExistsCheckByEmail(email);
			if(user==null)
			{
				return new Response(false,"user doesn't exists");
			}
			//preparedStatement = DBUtility.getConnection().prepareStatement("select * from user where user_email=? and user_password=? ");
			preparedStatement = DBUtility.getConnection().prepareStatement("select * from user where user_email=?");
			preparedStatement.setString(1, email);
			//preparedStatement.setString(2, password);
			ResultSet set= preparedStatement.executeQuery();
			
			boolean loginState= UsersExtra.login_counter_state(user.getId());
			if(set.next() &&loginState)
			{
				//validate the password here 
				String user_password=set.getString("user_password");
				PasswordAuthentication authenticate=new PasswordAuthentication();
				boolean successful=authenticate.authenticate(password.toCharArray(), user_password);
				System.out.println(successful);
				if(successful)
				{
					//System.out.println("User exists");
					CommonFunctions.closeConnection(preparedStatement);
					//Remove the old attempts 
					UsersExtra.removeFailedTries(user.getId());
					return new Response(true,"");		
				}else{
					//In case of a failed login we need to count that try 
					UsersExtra.increaseErrorCountForLogin(user.getId());
					//System.out.println("User doesn't exist "+set.getFetchSize());
					if(!loginState)
					{
						//user needs to back off for a short time
						System.out.println("user needs to back off for a short time");
						return new Response(false,"Too many attempts, please wait 10 minutes before trying again.");
					}
				}

			}else{

				//In case of a failed login we need to count that try 
				UsersExtra.increaseErrorCountForLogin(user.getId());
				//System.out.println("User doesn't exist "+set.getFetchSize());
				if(!loginState)
				{
					//user needs to back off for a short time
					System.out.println("user needs to back off for a short time");
					return new Response(false,"Too many attempts, please wait 10 minutes before trying again.");
				}
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}

	public static UserLoginResponse loginAndReturnSession(UserLoginRequest user)
	{
		String session=UsersExtra.GenerateSession();
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set session=? where user_email=?");
			preparedStatement.setString(1, session);
			preparedStatement.setString(2, user.getEmail());
			int effectedRows= preparedStatement.executeUpdate();
			if(effectedRows>0)
			{
				System.out.println("Done updating the session");
				CommonFunctions.closeConnection(preparedStatement);
				//Now we get the user nickname and session and admin state
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select user_name,session,admin from user where user_email=?");
				preparedStatement.setString(1, user.getEmail());
				//preparedStatement.setString(2, user.getPassword());
				ResultSet result=  preparedStatement.executeQuery();

				if(result.next())
				{
					UserLoginResponse response= new UserLoginResponse(result.getString("session"),result.getBoolean("admin"),result.getString("user_name"));
					CommonFunctions.closeConnection(preparedStatement);
					return response;
				}

			}else{

				//System.out.println("Session didn't get updated"+effectedRows);

			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);


		return null;

	}

	public static void logout(LogoutUserRequest session)
	{
		PreparedStatement preparedStatement=null;
		try{

			if(CommonFunctions.clean(session.getSession()) ==null || CommonFunctions.clean(session.getSession()) .equals(""))
			{
				return;
			}
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set session=? where session=?");
			preparedStatement.setString(1, null);
			preparedStatement.setString(2, session.getSession());
			preparedStatement.executeUpdate();
			CommonFunctions.closeConnection(preparedStatement);
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}


	}

	public static ArrayList<User> getAllUsers(String session,int userId)
	{
		//If the user is an admin then i will fetch all the users if not then Get me users that belong to a project i am enrolled in 
		//Furthermore, if not admin they can see only users with the enabled projects 
		ArrayList<User> temp=new ArrayList<User>();
		PreparedStatement preparedStatement=null;
		try{
			//First we get the user id that this session belongs too

			if(UsersService.validateAdminSession(CommonFunctions.clean(session)))
			{
				//Show me all the users except me 
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select user_id,admin,user_name,user_email from user where user_id <> ?");
				preparedStatement.setInt(1,userId);

			}else{

				//preparedStatement = DBUtility.getConnection().prepareStatement("select user_id,admin,user_name,user_email from user where user_id <> ? and user_id in (select user_id from project_memebers WHERE project_id in (select DISTINCT(project_id) from project_memebers where user_id=?))");
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select user_id,admin,user_name,user_email from user where user_id <> ? and user_id"
								+ " in (select user_id from project_memebers WHERE project_id in(select DISTINCT(project_id) from project_memebers where user_id=? and project_id "
								+ "in(select project_id from project where project_id =project_id and enabled=true)))");
				preparedStatement.setInt(1,(userId));
				preparedStatement.setInt(2,(userId));
			}

			ResultSet result=preparedStatement.executeQuery();
			while(result.next())
			{
				temp.add(new User(result.getInt("user_id"),result.getString("user_name"),result.getString("user_email"),result.getBoolean("admin")));
			}

			if(temp.isEmpty())
			{
				//The query result was 0
				//		//System.out.println("The query result was 0");
				CommonFunctions.closeConnection(preparedStatement);
				return temp;
			}
			CommonFunctions.closeConnection(preparedStatement);
			return temp;
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return null;
	}

	public static Response changePasswordWithToken(ChangePasswordRequestByToken request)
	{

		PreparedStatement preparedStatement =null;
		try{
			//validate the token first and return the email if the token is valid
			User user=UsersExtra.userExistsCheckByEmail(request.getEmail());
			if(user==null )
			{
				return new Response(false,"Not a valid email");
			}
			Response response=UsersExtra.validateToken(request.getToken(),user.getId());
			if(!response.getState())
			{
				return new Response(false,"Not a valid token");
			}
			//update the password
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set user_password=? where user_email=?");
			PasswordAuthentication auth=new PasswordAuthentication();
			preparedStatement.setString(1,auth.hash(request.getPassword().toCharArray()));
			preparedStatement.setString(2, response.getMessage());
			int result=  preparedStatement.executeUpdate();
			if(result>0)
			{
				UsersExtra.removeToken(request.getToken().trim());
				CommonFunctions.closeConnection(preparedStatement);
				//Don't forget to remove the attempts otherwise they user can't login after he/she rest his/her password
				UsersExtra.removeFailedTries(user.getId());
				return new Response(true,"");
			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}

		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

			return new Response(false,e.getMessage());
		}


	}

	public static Response requestPasswordReset(User user)
	{
		PreparedStatement preparedStatement =null;
		try{

			if(user==null)
			{
				return new Response(false,"");
			}


			//First we need to remove any older request the user has in the request password table 
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from restore_password_requests where user_id=?");
			preparedStatement.setInt(1, user.getId());
			if( preparedStatement.executeUpdate()>0)
			{
				//System.out.println("An older request has been deleted");
			}
			CommonFunctions.closeConnection(preparedStatement);
			//Then we create a random 6 numbers and insert that into the database
			int token=UsersExtra.getRandomNumber();
			//System.out.println("The random value is "+token);

			//Now i need to send inser this into the database and then send an email 
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("insert INTO restore_password_requests(user_id,token, expire_date) VALUES (?,?, ?)");
			preparedStatement.setInt(1, user.getId());
			preparedStatement.setInt(2, token);
			String expireDate=UsersExtra.getNextDay();
			if(expireDate==null)
			{
				expireDate="";
			}
			preparedStatement.setString(3, expireDate);
			if( preparedStatement.executeUpdate()<0)
			{
				// //System.out.println("An older request has been deleted");
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}
			CommonFunctions.closeConnection(preparedStatement);

			//Now we send an email with that token 
			SendEmail message= new SendEmail(user.getEmail(),Integer.toString(token));
			Response response=message.sendMessage();
			return response;
		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); 
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

			return new Response(false,e.getMessage());
		}



	}

	public static Response deleteUser(String userId)
	{
		PreparedStatement preparedStatement =null;		
		try{
			//make sure that the id is not the super admin id
			if(Integer.parseInt(userId)==1)
			{
				return new Response(false,"");
			}

			//Remove the user from all the projects first 
			ProjectService.removeUsersFromAllProjects(userId); //Delete from this table projet_members
			TaskService.removeTheUserFromAllTasks(Integer.parseInt(userId))	;//assigned_task_to_user
			UsersExtra.deleteAllRequestForPasswordForUser(Integer.parseInt(userId));//restore password requests

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from user where user_id=?");
			preparedStatement.setInt(1, Integer.parseInt(userId));
			if(preparedStatement.executeUpdate()>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");

			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}

		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}

	public static Response changeUserPassword(String userId,String password)
	{
		PreparedStatement preparedStatement =null;		
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set user_password=? where user_id=?");
			PasswordAuthentication auth=new PasswordAuthentication();
			preparedStatement.setString(1,auth.hash(password.toCharArray()));
			preparedStatement.setInt(2, Integer.parseInt(userId));
			if(preparedStatement.executeUpdate()>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");

			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}

	}

	public static Response validateUserPassword(int userId,String password)
	{
		PreparedStatement preparedStatement= null;
		try{	
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_id=?");
			preparedStatement.setInt(1, userId);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				String user_password=set.getString("user_password");
				PasswordAuthentication auth=new PasswordAuthentication();
				if(auth.authenticate(password.toCharArray(), user_password))
				{
					CommonFunctions.closeConnection(preparedStatement);
					return new Response(true,"");					
				}

			}
		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			return new Response(false,e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return new Response(false,"");
	}
	
	public static Response changeEmail(int userId,String email)
	{
		PreparedStatement preparedStatement =null;		
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set user_email=? where user_id=?");
			preparedStatement.setString(1, email);
			preparedStatement.setInt(2, userId);
			if(preparedStatement.executeUpdate()>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");

			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
	}

	public static Response changeNickname(int userId,String nickname)
	{
		PreparedStatement preparedStatement =null;		
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set user_name=? where user_id=?");
			preparedStatement.setString(1, nickname);
			preparedStatement.setInt(2, userId);
			if(preparedStatement.executeUpdate()>0)
			{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(true,"");

			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return new Response(false,"");
			}


		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,e.getMessage());
		}
		
	}
}
