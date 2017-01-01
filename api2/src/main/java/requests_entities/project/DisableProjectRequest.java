package requests_entities.project;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CommonFunctions;

public class DisableProjectRequest {
	@NotNull
	private String adminSession,projectId;

	public String getAdminSession() {
		return adminSession;
	}

	public void setAdminSession(String adminSession) {
		if(CommonFunctions.clean(adminSession).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.adminSession = adminSession;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		if(CommonFunctions.clean(projectId).length()<1)
		{
		//	System.out.println("memberId  size1: "+CommonFunctions.clean(memberId).length());
			return;
		}
		this.projectId = projectId;
	}

	public DisableProjectRequest(String adminSession, String projectId) 
	{
		this.adminSession = adminSession;
		this.projectId = projectId;
	}
	public DisableProjectRequest() {
	}	
	
	public String getJson(EnableProjectRequest request)
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
