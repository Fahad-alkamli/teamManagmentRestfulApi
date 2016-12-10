package controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import db.task.TaskService;
import db.user.UsersService;
import requests_entities.Response;
import requests_entities.task.*;

@RestController
public class TaskController {

	@RequestMapping(value="/create_task", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> createTask(@Valid @RequestBody CreateTaskRequest request)
	{

		//validate that the request came from an admin account
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=TaskService.createTask(request);
		if(response.getState())
		{
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.CREATED);	
		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}		
	}


	@RequestMapping(value="/get_all_tasks", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> getAllTasks(@Valid @RequestBody GetAllTasksRequest request)
	{
		//validate that the request came from a user account
		if(UsersService.userExistsCheckById(UsersService.getUserIdFromSession(request.getSession()))==null)
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=TaskService.getAllTasks(request.getSession(),UsersService.getUserIdFromSession(request.getSession()));
		if(response.getState())
		{
			////System.out.println(response.getMessage());
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}


	}


	@RequestMapping(value="/update_task", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> updateTask(@Valid @RequestBody UpdateTaskRequest request)
	{
		//validate that the request came from a user account
		if(UsersService.userExistsCheckById(UsersService.getUserIdFromSession(request.getSession()))==null)
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=TaskService.updateTask(request);
		if(response.getState())
		{
			////System.out.println(response.getMessage());
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}
	@RequestMapping(value="/delete_task", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> deleteTask(@Valid @RequestBody DeleteTaskRequest request)
	{
		//validate that the request came from a admin account
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		Response response=TaskService.deleteTask(request.getTaskId());
		if(response.getState())
		{
			//System.out.println(response.getMessage());
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}

	@RequestMapping(value="/assign_task_to_user", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> assignTaskToUser(@Valid @RequestBody AssignTaskToUserRequest request)
	{

				if(!UsersService.validateAdminSession(request.getAdminSession()))
				{
					//System.out.println("Trying to throw an exception UNAUTHORIZED");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
				}

				Response response=TaskService.assignTaskToUser(request);
				if(response.getState())
				{
					//System.out.println(response.getMessage());
					return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

				}else{
					return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
				}
	}

	@RequestMapping(value="/get_task_members", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> getTaskMembers(@Valid @RequestBody GetTaskMembersRequest request)
	{
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=TaskService.getTaskMembers(request.getTaskId());
		if(response.getState())
		{
			//System.out.println(response.getMessage());
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}

	@RequestMapping(value="/remove_task_from_user", method = RequestMethod.POST, consumes = "application/json",produces="application/json")
	public ResponseEntity<String> removeUserFromTask(@Valid @RequestBody RemoveUsersFromTaskRequest request)
	{
		if(!UsersService.validateAdminSession(request.getAdminSession()))
		{
			//System.out.println("Trying to throw an exception UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		Response response=TaskService.removeUsersFromTask(request);
		if(response.getState())
		{
			//System.out.println(response.getMessage());
			return new ResponseEntity<String>(response.getMessage(),HttpStatus.OK);	

		}else{
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.getJson(response));
		}
	}

}
