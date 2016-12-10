package db.user.user_extra_functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import entity.CommonFunctions;
import logger.Logger;

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




}
