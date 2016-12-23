package entity;


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.nio.file.*;
public class CommonFunctions {
	public static String clean(String temp)
	{
		try {
			return temp.trim().replace(" ", "");
		}catch(Exception e)
		{

		}
		return null;
	}

	public static ArrayList<String> clean(ArrayList<String> input)
	{
		try {
			ArrayList<String> output=new ArrayList<String>();
			for(String temp:input)
			{
				output.add(clean(temp));
			}
			return output;
		}catch(Exception e)
		{

		}
		return null;
	}

	public static void closeConnection(PreparedStatement preparedStatement)
	{
		Runnable run=new Runnable()
				{

					@Override
					public void run() {
						try{
						//	preparedStatement.getConnection().close();
						preparedStatement.close();
							//System.out.println("Connection has been closed");
						}catch(Exception e)
						{
							System.out.println("closeConnection: "+e.getMessage());
						}
					}
					
				};
				new Thread(run).start();
		
	}
	public static void ErrorLogger(String message)
	{
		try{
			//  message=message+" \r\n";      
			boolean exists =Files.exists(Paths.get("log.txt"));
			// System.out.println("Does file exists: "+exists);
			if(exists==false)
			{  	
				Files.createFile(Paths.get("log.txt"));
				//FileOutputStream fileOuputStream = new FileOutputStream("log.txt");     
				Files.setAttribute(Paths.get("log.txt"), "dos:hidden", true);
				//fileOuputStream.close();
			}



			{
				message=message.trim()+"\r\n";
				Files.write(Paths.get("log.txt"), message.getBytes(), StandardOpenOption.APPEND);
				System.out.println("Check this: "+message);
			}

		}catch(Exception e)
		{
			System.out.println("ErrorLogger sub: "+e.getMessage());
			// class Local {}; Functions.ErrorLogger("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			e.printStackTrace();
		}

	}


}
