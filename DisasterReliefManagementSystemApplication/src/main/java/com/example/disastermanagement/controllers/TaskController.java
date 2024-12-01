package com.example.disastermanagement.controllers;

import com.example.disastermanagement.models.*;
import com.example.disastermanagement.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final ReliefRequestService reliefRequestService;
    private final UserService userService;
    private final ResourceService resourceService;
    private final TaskService taskService;

    @Autowired
    public TaskController(ReliefRequestService reliefRequestService, UserService userService,
                          ResourceService resourceService, TaskService taskService) {
        this.reliefRequestService = reliefRequestService;
        this.userService = userService;
        this.resourceService = resourceService;
        this.taskService = taskService;
    }

    // View Pending Relief Requests
    @GetMapping("/pending-requests")
    public String viewPendingRequests(Model model) {
        model.addAttribute("pendingRequests", reliefRequestService.getPendingRequests());
        return "tasks/pending-requests";
    }

    // Show Create Task Form
    @GetMapping("/create/{reliefRequestId}")
    public String showCreateTaskForm(@PathVariable Long reliefRequestId, Model model) {
        model.addAttribute("reliefRequestId", reliefRequestId);
        model.addAttribute("volunteers", userService.getUsersByRole(Role.VOLUNTEER)); // Fetch volunteers
        model.addAttribute("resources", resourceService.getAllResources()); // Fetch resources
        model.addAttribute("task", new Task());
        return "tasks/create-task";
    }

    // Create Task
    @PostMapping
    public String createTask(@ModelAttribute Task task, 
                             @RequestParam Long reliefRequestId,
                             @RequestParam Long resourceId,
                             @RequestParam Long volunteerId,  // Add volunteerId parameter
                             @RequestParam int resourceQuantity) {

        // Fetch the resource
        Resource resource = resourceService.getResourceById(resourceId);
        if (resource.getQuantity() >= resourceQuantity) {
            // Deduct resource quantity
            resource.setQuantity(resource.getQuantity() - resourceQuantity);
            resourceService.updateResource(resource);

            // Fetch the relief request
            ReliefRequest reliefRequest = reliefRequestService.getReliefRequestById(reliefRequestId);
            task.setReliefRequest(reliefRequest);

            // Fetch the volunteer and set it in the task
            User volunteer = userService.getUserById(volunteerId);
            task.setVolunteer(volunteer);

            // Set resource and status
            task.setResource(resource);
            task.setStatus(TaskStatus.PENDING);

            // Save the task
            taskService.createTask(task);
        } else {
            // Redirect to form with an error message if insufficient resources
            return "redirect:/tasks/create/" + reliefRequestId + "?error=InsufficientResources";
        }

        // Redirect to task list after successful creation
        return "redirect:/tasks";
    }

    // View Tasks
    @GetMapping
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks/list-tasks";
    }
}