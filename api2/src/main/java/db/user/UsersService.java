package db.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Random;


import com.mysql.jdbc.Statement;

import db.DBUtility;
import db.poject.ProjectService;
import db.task.TaskService;
import db.user.user_extra_functions.UsersExtra;
import entity.CommonFunctions;
import entity.User;
import logger.Logger;
import login.SendEmail;
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

			preparedStatement.setString(3, user.getPassword());
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
				//System.out.println("This user is admin");
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


	public static boolean userExistsCheckByLogin(String email,String password)
	{
		PreparedStatement preparedStatement =null;
		try{

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_email=? and user_password=? ");
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				//System.out.println("User exists");
				CommonFunctions.closeConnection(preparedStatement);
				return true;
			}else{

				//System.out.println("User doesn't exist "+set.getFetchSize());
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return false;
	}


	public static UserLoginResponse loginAndReturnSession(UserLoginRequest user)
	{

		String session=GenerateSession();
		PreparedStatement preparedStatement=null;
		try{

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set session=? where user_email=? and user_password=? ");
			preparedStatement.setString(1, session);
			preparedStatement.setString(2, user.getEmail());
			preparedStatement.setString(3, user.getPassword());
			int effectedRows= preparedStatement.executeUpdate();
			if(effectedRows>0)
			{
				//System.out.println("Done updating the session");
				CommonFunctions.closeConnection(preparedStatement);
				//Now we get the user nickname and session and admin state
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("select user_name,session,admin from user where user_email=? and user_password=? ");
				preparedStatement.setString(1, user.getEmail());
				preparedStatement.setString(2, user.getPassword());
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


	//http://stackoverflow.com/questions/415953/how-can-i-generate-an-md5-hash
	private static String GenerateSession() {
		try {
			String md5 = Long.toString(System.currentTimeMillis());
			md5+="Alkamli";
			Random rnd=new Random();
			md5+=Integer.toString(rnd.nextInt(999999));
			md5+=Integer.toString(rnd.nextInt(999999));
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) 
			{
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) 
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());

		}
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


	public static User userExistsCheckById(int userId)
	{
		PreparedStatement preparedStatement= null;
		try{
			if(userId == -1 )
			{
				return null;
			}
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_id=? ");
			preparedStatement.setInt(1, userId);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				//System.out.println("User exists");

				User user=new User();
				user.setId(set.getInt("user_id"));
				user.setAdmin(set.getBoolean("admin"));
				user.setEmail(set.getString("user_email"));
				user.setName(set.getString("user_name"));
				CommonFunctions.closeConnection(preparedStatement);
				return user;
			}else{

				//System.out.println("User doesn't exist "+set.getFetchSize());
			}

		}catch(Exception e)
		{

			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return null;
	}


	public static int getUserIdFromSession(String session)
	{
		PreparedStatement preparedStatement =null;
		try{

			if(session == null || CommonFunctions.clean(session).length()<1)
			{
				return -1;
			}
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select user_id from user where session=? ");
			preparedStatement.setString(1, session);
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				//System.out.println("User exists");
				int userId=( set.getInt("user_id"));
				//System.out.println("Check this is the user id: "+userId);
				if(userId != 0 )
				{
					CommonFunctions.closeConnection(preparedStatement);
					return (userId);
				}
			}else{

				//System.out.println("User doesn't exist "+set.getFetchSize());
			}


		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			//System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		}
		CommonFunctions.closeConnection(preparedStatement);
		return -1;
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


	public static User userExistsCheckByEmail(String userEmail)
	{
		PreparedStatement preparedStatement =null;
		try{

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from user where user_email=?");
			preparedStatement.setString(1,userEmail.trim());
			ResultSet set= preparedStatement.executeQuery();
			if(set.next())
			{
				//System.out.println("User exists");
			
				User user=new User();
				user.setId(set.getInt("user_id"));
				user.setAdmin(set.getBoolean("admin"));
				user.setEmail(set.getString("user_email"));
				user.setName(set.getString("user_name"));
				CommonFunctions.closeConnection(preparedStatement);
				return user;
			}else{

				//System.out.println("User doesn't exist "+set.getFetchSize());
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


	public static Response changePasswordWithToken(ChangePasswordRequestByToken request)
	{

		PreparedStatement preparedStatement =null;
		try{
			//validate the token first and return the email if the token is valid
			User user=userExistsCheckByEmail(request.getEmail());
			if(user==null )
			{
				return new Response(false,"Not a valid email");
			}
			Response response=validateToken(request.getToken(),user.getId());
			if(!response.getState())
			{
				return new Response(false,"Not a valid token");
			}
			//update the password
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("update user set user_password=? where user_email=?");
			preparedStatement.setString(1, request.getPassword());
			preparedStatement.setString(2, response.getMessage());
			int result=  preparedStatement.executeUpdate();
			if(result>0)
			{
				removeToken(request.getToken().trim());
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


	private static Response validateToken(String token,int userId)
	{
		PreparedStatement preparedStatement =null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from restore_password_requests where token=? and user_id=?");
			preparedStatement.setString(1, token);
			preparedStatement.setInt(2, userId);
			ResultSet result=  preparedStatement.executeQuery();
			if(result.next())
			{
				int user_id=result.getInt("user_id");
				CommonFunctions.closeConnection(preparedStatement);
				User user=userExistsCheckById(user_id);
				if(user ==null)
				{
					return new Response(false,"");
				}
				return new Response(true,user.getEmail());
			}else{
				CommonFunctions.closeConnection(preparedStatement);
				increaseTokenCounter(userId);
				return new Response(false,"");
			}
		}catch(Exception e)
		{
			CommonFunctions.closeConnection(preparedStatement);
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); 
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			return new Response(false,"");
		}
	}

	//This will increase the counter for each token to protect the token from bruteforce attacks
	@SuppressWarnings("resource")
	private static void increaseTokenCounter(int userId)
	{
		PreparedStatement preparedStatement =null;
		try{
			//First we check the counter for the token if it's more than 3 tries then remove the token ,otherwise increase the token by 1
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from restore_password_requests where user_id=?");
			preparedStatement.setInt(1, userId);
			ResultSet result=preparedStatement.executeQuery();
			if(result.next())
			{
				int counter=result.getInt("failed_tries_counter");
				CommonFunctions.closeConnection(preparedStatement);
				
				if(counter>=3)
				{
					//Remove the token 
					preparedStatement = DBUtility.getConnection()
							.prepareStatement("delete from restore_password_requests where user_id=?");
					preparedStatement.setInt(1, userId);
					preparedStatement.executeUpdate();
					CommonFunctions.closeConnection(preparedStatement);
					return;
				}else{
					//Increase the token by 1
					counter++;
					preparedStatement = DBUtility.getConnection().prepareStatement("update restore_password_requests set failed_tries_counter=? where user_id=?");
					preparedStatement.setInt(1, counter);
					preparedStatement.setInt(2, userId);
					preparedStatement.executeUpdate();
					CommonFunctions.closeConnection(preparedStatement);
					return;
				}	
			}else{
				CommonFunctions.closeConnection(preparedStatement);
				return;
			}
			
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); 
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			
		}
	}

	private static void removeToken(String token)
	{
		PreparedStatement preparedStatement =null;		
		try{

			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from restore_password_requests where token=?");
			preparedStatement.setString(1, token);
			preparedStatement.executeUpdate();
			CommonFunctions.closeConnection(preparedStatement);
			return;
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		CommonFunctions.closeConnection(preparedStatement);
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
			deleteAllRequestForPasswordForUser(Integer.parseInt(userId));//restore password requests

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
			preparedStatement.setString(1, password);
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




	private static void deleteAllRequestForPasswordForUser(int userId)
	{
		PreparedStatement preparedStatement =null;		

		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from restore_password_requests where user_id=?");
			preparedStatement.setInt(1, userId);
			if(preparedStatement.executeUpdate()>0)
			{
				//System.out.println("deleteAllRequestForPasswordForUser is secessful");
				CommonFunctions.closeConnection(preparedStatement);
				return;
			}

		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		}
		CommonFunctions.closeConnection(preparedStatement);

	}

}
