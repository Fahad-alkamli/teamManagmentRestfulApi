package db.user.user_extra_functions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import db.DBUtility;
import db.user.UsersService;
import entities.CommonFunctions;
import entities.User;
import logger.Logger;
import requests_entities.Response;

public class UsersExtra {

	static Logger log = new Logger(UsersExtra.class.getName());
	 
	//http://stackoverflow.com/questions/5392693/java-random-number-with-given-length
	public static int getRandomNumber() {

		int rand = (new Random()).nextInt(900000) + 100000;
		if(Integer.toString(rand).length()<6)
		{
			rand+=(new Random()).nextInt(900000) + 100000;
			String temp=Integer.toString(rand);
			rand=Integer.parseInt(temp.substring(temp.length()-6));
			//System.out.println("This is bigger than6");
		}
		return rand;
	}

	public static String getNextDay()
	{
		try {
			Calendar cal2 = Calendar.getInstance();
			// cal2.setTime(startDate);
			cal2.add(Calendar.HOUR, 48);
			DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
			//System.out.println("The date tomorrow will be : "+dateFormat.format(cal2.getTime()));
			return dateFormat.format(cal2.getTime());
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			
		}
		return null;
	}

	private static String getNextMinutes(int minutes)
	{
		try {
			Calendar cal2 = Calendar.getInstance();
			// cal2.setTime(startDate);
			cal2.add(Calendar.MINUTE, minutes);
			DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy h:m");
			
		//System.out.println("The date tomorrow will be : "+dateFormat.format(cal2.getTime()));
			return dateFormat.format(cal2.getTime());
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			
		}
		return null;
	}
	
	public static boolean login_counter_state(int userId)
	{
		PreparedStatement preparedStatement=null;
		
		try{
			//First we check if the email address exists in the users table
			//Next we get the counter 
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from failed_login_counter where user_id=?");
			preparedStatement.setInt(1, userId);
			ResultSet result=preparedStatement.executeQuery();
			if(result.next())
			{
				int counter=result.getInt("counter");
				if(counter>=5)
				{
					String waitingTime=result.getString("time_to_wait");
					CommonFunctions.closeConnection(preparedStatement);
					//Now we need to check the waiting time and make sure to wait for that amount before proceeding 
					Date date1 = null;
					Date now=null;
					Calendar cal2 = Calendar.getInstance();
					DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy h:m");
					date1 = dateFormat.parse(waitingTime);
					//System.out.println("WatingTime: "+date1);
					
					now=dateFormat.parse(dateFormat.format(cal2.getTime()));
					//System.out.println("Now: "+now);
					if(now.after(date1))
					{
						System.out.println("true");
						return true;
					}else{
						System.out.println("User needs to wait before another attempt");
						//System.out.println("false");
						return false;
					}
					
					
				}
			}
			CommonFunctions.closeConnection(preparedStatement);
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			e.printStackTrace();
		}
		CommonFunctions.closeConnection(preparedStatement);
		return true;
	}
	
	@SuppressWarnings("resource")
	public static void increaseErrorCountForLogin(int userId)
	{
		//Check the current counter if it's 3 or more then add more time to counter if not then just increase the counter by 1
		PreparedStatement preparedStatement=null;
		try{
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("select * from failed_login_counter where user_id=?");
			preparedStatement.setInt(1, userId);
			ResultSet result=preparedStatement.executeQuery();
		
			if(result.next())
			{
				//We check for an existing counter to increase the number and if it's 3 or more then increase the counter and add a waiting time
				int counter=result.getInt("counter");
				CommonFunctions.closeConnection(preparedStatement);
				if(counter==5)
				{
					//initiate the time to wait and increase the timer 
					preparedStatement = DBUtility.getConnection()
							.prepareStatement("update failed_login_counter set time_to_wait=?,counter=? where user_id=?");
					preparedStatement.setString(1, getNextMinutes(5));
					counter++;
					preparedStatement.setInt(2, counter);
					preparedStatement.setInt(3, userId);
					preparedStatement.executeUpdate();
					CommonFunctions.closeConnection(preparedStatement);
					
				}else if(counter>5)
				{
					//increase the time by 10 minutes + the counter 
					System.out.println("increase the time by 10 minutes + the counter");
					preparedStatement = DBUtility.getConnection()
							.prepareStatement("update failed_login_counter set time_to_wait=?,counter=? where user_id=?");
					preparedStatement.setString(1, getNextMinutes(10));
					counter++;
					preparedStatement.setInt(2, counter);
					preparedStatement.setInt(3, userId);
					preparedStatement.executeUpdate();
					CommonFunctions.closeConnection(preparedStatement);
				}else{
					//increase by 1
					preparedStatement = DBUtility.getConnection()
							.prepareStatement("update failed_login_counter set counter=? where user_id=?");
					counter++;
					preparedStatement.setInt(1, counter);
					preparedStatement.setInt(2, userId);
					preparedStatement.executeUpdate();
					CommonFunctions.closeConnection(preparedStatement);
				}
				
			}else{
				//Here we need to create a new entry in the database and initiate the counter by 1
				preparedStatement = DBUtility.getConnection()
						.prepareStatement("insert failed_login_counter(user_id,counter) values(?,?) ");
				preparedStatement.setInt(1, userId);
				preparedStatement.setInt(2,1);
				preparedStatement.executeUpdate();
				CommonFunctions.closeConnection(preparedStatement);
			}
			
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
		}
		
	}

	public static void removeFailedTries(int userId)
	{	PreparedStatement preparedStatement=null;
	
		try{
			
			preparedStatement = DBUtility.getConnection()
					.prepareStatement("delete from  failed_login_counter where user_id=?");
			preparedStatement.setInt(1, userId);
			preparedStatement.executeUpdate();
		}catch(Exception e)
		{
			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
			CommonFunctions.closeConnection(preparedStatement);
			
		}
	}

	public static void deleteAllRequestForPasswordForUser(int userId)
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

	public static Response validateToken(String token,int userId)
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
				User user=UsersService.userExistsCheckById(user_id);
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
	public static void increaseTokenCounter(int userId)
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

	public static void removeToken(String token)
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

	//http://stackoverflow.com/questions/415953/how-can-i-generate-an-md5-hash
	public static String GenerateSession() {
		do{
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
				//We make sure we always get a unique session 
				if(UsersService.getUserIdFromSession(sb.toString())==-1 || UsersService.getUserIdFromSession(sb.toString())==0)
				{
					return sb.toString();
				}else{
					System.out.println("Not a unique session ");
				}

			} catch (java.security.NoSuchAlgorithmException e) 
			{
				class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage())); log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());


			}
		}while(true);

	}

	
	
	
}
