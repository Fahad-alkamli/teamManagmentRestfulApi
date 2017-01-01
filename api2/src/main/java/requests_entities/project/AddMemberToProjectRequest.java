package requests_entities.project;

import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class AddMemberToProjectRequest {

	@NotNull
	private String adminSession,memberId;
	@NotNull
	private ArrayList<String>  projectId;
	


	
    public AddMemberToProjectRequest() {
		super();
		// TODO Auto-generated constructor stub
	}


	public AddMemberToProjectRequest(String adminSession, String memberId, ArrayList<String> projectId) {
		super();
		this.adminSession = adminSession;
		this.memberId = memberId;
		this.projectId = projectId;
	}


	public String getAdminSession() {
		return adminSession;
	}

	public void setAdminSession(String adminSession) {
		if(CommonFunctions.clean(adminSession).length()<1)
		{
			System.out.println("Admin session size1: "+CommonFunctions.clean(adminSession).length());
			return;
		}
		this.adminSession = adminSession;
		System.out.println("Admin session size2: "+CommonFunctions.clean(adminSession).length());
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		if(CommonFunctions.clean(memberId).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.memberId = memberId;
	}

	public ArrayList<String> getProjectId() {
		return projectId;
	}

	public void setProjectId(ArrayList<String> projectId) 
	{
		if(projectId == null || projectId.size()<1)
		{
			return;
		}
		
		this.projectId = projectId;
	}

	public String getJson(AddMemberToProjectRequest request)
    {
        try{
            ObjectMapper mapper = new ObjectMapper();

            String jsonInString = mapper.writeValueAsString(request);
            return jsonInString;

        }catch(Exception e)
        {
        	System.out.println(e.getMessage());
           
        }
        return null;
    }
}

