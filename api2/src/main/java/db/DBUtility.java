package db;

import java.sql.Connection;

import java.sql.DriverManager;
import java.util.ArrayList;

import logger.Logger;



//https://dzone.com/articles/crud-using-spring-mvc-40
public class DBUtility  {

	// private static Connection connection = null;
	final static Logger log=new Logger(DBUtility.class.getName());
	 private static ArrayList<Connection> connectctionsList;
	 private static String driver ="com.mysql.jdbc.Driver";
	 private static String user = "root";
	// private static  String url = "jdbc:mysql://172.16.42.42:3306/TeamManagment?autoReconnect=true&useSSL=true";
	 private static  String url = "jdbc:mysql://localhost:3306/TeamManagment?autoReconnect=true&useSSL=false";
	   
     private static  String password ="console";
	static int pointer=0;
	    public static synchronized Connection getConnection() 
	    {
	            try {

	            	//reset the pointer
	            	if(pointer==29)
	            	{
	            		pointer=0;
	            	}
	            	if(connectctionsList==null)
	            	{
	            		System.out.println("Create the first connection");
	            		//create the first connection and create a thread that does the rest don't forget to increase the counter
	            		Class.forName(driver);
						
	            		connectctionsList=new ArrayList<Connection>();
	            		connectctionsList.add(DriverManager.getConnection(url, user, password));
	
								setupConnections();
	
	            	}
	            	//System.out.println("Check this:"+connectctionsList.size());
	            	if(connectctionsList.size()<29)
	            	{
	            		System.out.println("Give connection number: "+0);
	            	
	            		System.out.println("Check this:"+connectctionsList.size());
	            		return connectctionsList.get(0);
	            	}
	            	
	            	 if(connectctionsList.get(pointer)==null)
	            	{
	            		System.out.println("Connection is null");
	            	}
	            	 else if(connectctionsList.get(pointer).isClosed() || connectctionsList.get(pointer).isValid(2)==false)
	            	{
	            		 pointer=0;
	            		System.out.println("Connection is broken");
	            		connectctionsList.clear();
	            		connectctionsList.add(DriverManager.getConnection(url, user, password));
						setupConnections();
						
	            		return connectctionsList.get(0);
	            	}
	            	Connection connection=connectctionsList.get(pointer);
	            	System.out.println("Give connection number: "+pointer);
	            	pointer++;
	            	return connection;
	            }catch (Exception e) {
	        		class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
	    			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
	    	
	            }
	            return null;
	    }
	
	    
	    
	    public static synchronized void fixConnections()
	    {
	    	
	    	for(Connection con:connectctionsList)
	    	{
	    		try{
	    		if(con.isClosed() || con.isValid(2)==false)
	    		{
	    			connectctionsList.remove(con);
            		connectctionsList.add(DriverManager.getConnection(url, user, password));
	    		}
	    		}catch(Exception e)
	    		{
	    			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
	    			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
	    	
	    		}
	    	}
	    }


	    
	    public static synchronized void setupConnections()
	    {
	    	try{
	    	
	    	for(int i=1;i<30;i++)
	    	{
	    		new Thread(new Runnable(){

					@Override
					public void run() {
						try {
							Class.forName(driver);
					
		            	
		            	//System.out.println("Count :"+count);
		            	connectctionsList.add(DriverManager.getConnection(url, user, password));
		            	System.out.println("Create new connection");
						}		            	
		            	 catch (Exception e) {
								
		            			class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
		    	    			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
		    	    	
							}
					}
	    			
	    		}).start();;
	    		   
	    	}
	    	}catch(Exception e)
	    	{
	    		class Local {}; //System.out.println("Sub: "+Local.class.getEnclosingMethod().getName()+" Error code: "+e.getMessage());
    			log.error(e.getMessage(),Local.class.getEnclosingMethod().getName());
    	
	    	}
	    }
}
