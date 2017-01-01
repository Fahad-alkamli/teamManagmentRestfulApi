package controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import db.poject.ProjectService;
import db.user.UsersService;
import entities.CommonFunctions;
import entities.Project;
import requests_entities.Response;
import requests_entities.project.*;
@RestController
public class ProjectController {

	@RequestMapping(value="/create_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> createProject(@Valid @RequestBody CreateProjectRequest projectRequest)
	{
		//validate that the request came from an admin account
		if(!UsersService.validateAdminSession(projectRequest.getAdminSession()))
		{
			System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Project project;
		if((project=ProjectService.createProject(projectRequest))==null)
		{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(null);
		}


		System.out.println("project has been created ");
		return new ResponseEntity<String>(project.getJson(project),HttpStatus.CREATED);	
	}

	@RequestMapping(value="/enable_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> enableProject(@Valid @RequestBody EnableProjectRequest request)
	{
		try{
			//validate that the request came from an admin account
			if(!UsersService.validateAdminSession(request.getAdminSession()))
			{
				System.out.println("UNAUTHORIZED: Trying to throw an exception");
				System.out.println("Admin session:"+request.getAdminSession());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}
			Response response=ProjectService.enableProject(request.getProjectId());
			if(!response.getState())
			{
				System.out.println("METHOD_NOT_ALLOWED");
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
			}

			System.out.println("project has been enabled ");
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	
		}
		//System.out.println("project has been enabled ");

	}

	@RequestMapping(value="/disable_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> disableProject(@Valid @RequestBody DisableProjectRequest request)
	{
		try{
			//validate that the request came from an admin account
			if(!UsersService.validateAdminSession(request.getAdminSession()))
			{
				System.out.println("UNAUTHORIZED: Trying to throw an exception");
				System.out.println("Admin session:"+request.getAdminSession());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}
			Response response=ProjectService.disableProject(request.getProjectId());
			if(!response.getState())
			{
				System.out.println("METHOD_NOT_ALLOWED");
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
			}

			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	
		}
		//System.out.println("project has been disabled ");
		//return new ResponseEntity<String>("",HttpStatus.METHOD_NOT_ALLOWED);	
	}

	@RequestMapping(value="/add_member_to_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> addMemeberToProject(@Valid @RequestBody AddMemberToProjectRequest request)
	{
		//validate that the request came from an admin account
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			System.out.println("UNAUTHORIZED: Trying to throw an exception");
			System.out.println("Admin session:"+request.getAdminSession());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}


		//We need 
		//We do not know if the user exists we only know that the admin is a valid admin 
		if(UsersService.userExistsCheckById(Integer.parseInt(request.getMemberId()))==null)
		{
			//The user id doesn't exists
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("The user id doesn't exists");
		}

		//one User multiple projects 
		int countFalseActions=0;
		for(String projectId:request.getProjectId())
		{
			if(!ProjectService.projectExists(CommonFunctions.clean(projectId)))
			{
				//The project doesn't exists
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("The project doesn't exists");
			}
			Response response=ProjectService.addMemberToProject(projectId,request.getMemberId());

			//Start inserting to the database 
			if(!response.getState())
			{
				countFalseActions+=1;
				//We couldn't add a member to the project for some reason
				//return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
			}
		}

		if(countFalseActions>0)
		{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("");
		}

		System.out.println("member  has been added to a project ");
		return new ResponseEntity<String>("",HttpStatus.OK);	

	}

	@RequestMapping(value="/get_all_projects", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> getAllProjects(@Valid @RequestBody SessionOnlyRequest request)
	{
		try{
			//Before we call any function we need to validate the session that it actually belongs to a user 
			if(UsersService.getUserIdFromSession(request.getSession())==-1)
			{
				//this session doesn't exists 
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}
			Response response=ProjectService.getAllProjects(request);
			//System.out.println("getAllProjects ");
			if(response.getState())
			{
				//Response http://www.makeinjava.com/convert-list-objects-to-from-json-using-jackson-objectmapper/
				return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	
			}else
			{
				//We just couldn't find result
				return new ResponseEntity<String>(response.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	
			}
		}catch(Exception e)
		{
			class Local {}; CommonFunctions.ErrorLogger(("MethodName: "+Local.class.getEnclosingMethod().getName()+" || ErrorMessage: "+e.getMessage()));
		}
		return new ResponseEntity<String>("",HttpStatus.METHOD_NOT_ALLOWED);	

	}

	@RequestMapping(value="/getUsersBelongToProject", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> getUsersBelongToProject(@Valid @RequestBody GetUsersBelongToProjectRequest request)
	{
		try{
			//validate that the request came from an admin account
			if(!UsersService.validateAdminSession(request.getAdminSession()))
			{
				System.out.println("UNAUTHORIZED: Trying to throw an exception");
				System.out.println("Admin session:"+request.getAdminSession());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}

			Response response=ProjectService.getAllUsersThatBelongToProject(Integer.parseInt(request.getProjectId()));
			if(response.getState())
			{
				return ResponseEntity.status(HttpStatus.OK).body(response.getMessage());
			}else
			{
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
			}
		}catch(Exception e)
		{
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	

		}
		//return new ResponseEntity<String>("",HttpStatus.METHOD_NOT_ALLOWED);	

	}

	@RequestMapping(value="/removeUsersFromProject", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> removeUsersFromProject(@Valid @RequestBody RemoveUserFromProject request)
	{
		try{
			//validate that the request came from an admin account
			if(!UsersService.validateAdminSession(request.getAdminSession()))
			{
				System.out.println("UNAUTHORIZED: Trying to throw an exception");
				System.out.println("Admin session:"+request.getAdminSession());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			}

			Response response=ProjectService.removeUsersFromProject(request);
			if(response.getState())
			{
				return ResponseEntity.status(HttpStatus.OK).body(response.getJson(response));
			}else
			{
				return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
			}



		}catch(Exception e)
		{
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	

		}




	}

	@RequestMapping(value="/update_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> updateProject(@Valid @RequestBody UpdateProjectRequest projectRequest)
	{

		//validate that the request came from an admin account
		if(!UsersService.validateAdminSession(projectRequest.getAdminSession()))
		{
			System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Project project;
		if((project=ProjectService.updateProject(projectRequest))==null)
		{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(null);
		}

		System.out.println("project has been updated ");
		return new ResponseEntity<String>(project.getJson(project),HttpStatus.OK);	
	}

	@RequestMapping(value="/delete_project", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> deleteProject(@Valid @RequestBody DeleteProjectRequest request)
	{
		//validate that the request came from an admin account
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=ProjectService.deleteProject(request);
		if(response.getState())
		{
			return new ResponseEntity<String>("",HttpStatus.OK);	

		}else{

			return new ResponseEntity<String>(response.getMessage(),HttpStatus.METHOD_NOT_ALLOWED);	

		}
	}


}
