package logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Logger {

	private String className;
	private String errorLogFilePath="error-log.txt";
	@SuppressWarnings("unused")
	private String infoLogFilePath="info-log.txt";
	private String warnLogFilePath="warn-log.txt";
	@SuppressWarnings("unused")
	private String debugLogFilePath="debug-log.txt";
	public Logger(String className)
	{
		this.className=className;
	}
	
	public void error(String errorMessage,String methodName)
	{
		
		String message="Class Name: "+className+" || MethodName: "+methodName+" || ErrorMessage: "+errorMessage;
		System.out.println(message);
		if(errorLogFilePath != null)
		{
			
			logToFile(message,errorLogFilePath);
			
		}
	}

	
	public void warn(String warnMessage,String methodName )
	{
		String message="Class Name: "+className+" || MethodName: "+methodName+" || WarnMessage: "+warnMessage;
		System.out.println(message);
		if(warnLogFilePath != null)
		{
			
			logToFile(message,warnLogFilePath);
			
		}

	}
	public void set(String path)
	{
		this.errorLogFilePath=path;
	}
	
	
	private void logToFile(String message,String path)
	{
		try{
			if(path==null)
			{
				return;
			}
			//  message=message+" \r\n";      
			boolean exists =Files.exists(Paths.get(path));
			// System.out.println("Does file exists: "+exists);
			if(exists==false)
			{  	
				Files.createFile(Paths.get(path));
				//FileOutputStream fileOuputStream = new FileOutputStream("log.txt");     
				//Files.setAttribute(Paths.get(filePath), "dos:hidden", true);
				//fileOuputStream.close();
			}



			{
				message=message.trim()+"\r\n";
				Files.write(Paths.get(path), message.getBytes(), StandardOpenOption.APPEND);
				//System.out.println("Check this: "+message);
			}

		}catch(Exception e)
		{
			System.out.println("ErrorLogger sub: "+e.getMessage());
			// class Local {}; Functions.ErrorLogger("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
}
