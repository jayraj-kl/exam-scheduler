package com.exam.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Exam Scheduler - Home");
        return "index";
    }
    
    @GetMapping("/exams")
    public String exams(Model model) {
        model.addAttribute("pageTitle", "Manage Exams");
        return "exams";
    }
    
    @GetMapping("/students")
    public String students(Model model) {
        model.addAttribute("pageTitle", "Manage Students");
        return "students";
    }
    
    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("pageTitle", "Manage Rooms");
        return "rooms";
    }
    
    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("pageTitle", "View Schedule");
        return "schedule";
    }
}
