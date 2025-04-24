<!-- filepath: c:\Users\Lenovo\IdeaProjects\exam-scheduler\src\main\webapp\WEB-INF\views\layout\header.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${pageTitle}</title>
    <!-- CSRF Protection -->
    <meta name="_csrf" content="${_csrf.token}" />
    <meta name="_csrf_header" content="${_csrf.headerName}" />
    <!-- Bootstrap CSS -->
    <link
      rel="stylesheet"
      href="/webjars/bootstrap/5.2.3/css/bootstrap.min.css"
    />
    <!-- Font Awesome -->
    <link rel="stylesheet" href="/webjars/font-awesome/6.3.0/css/all.min.css" />
    <!-- Custom CSS -->
    <link rel="stylesheet" href="/css/styles.css" />
  </head>
  <body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <a class="navbar-brand" href="/">
          <i class="fas fa-calendar-alt me-2"></i>Exam Scheduler
        </a>
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <a
                class="nav-link ${pageContext.request.requestURI.contains('/exams') ? 'active' : ''}"
                href="/exams"
              >
                <i class="fas fa-file-alt me-1"></i> Exams
              </a>
            </li>
            <li class="nav-item">
              <a
                class="nav-link ${pageContext.request.requestURI.contains('/students') ? 'active' : ''}"
                href="/students"
              >
                <i class="fas fa-user-graduate me-1"></i> Students
              </a>
            </li>
            <li class="nav-item">
              <a
                class="nav-link ${pageContext.request.requestURI.contains('/rooms') ? 'active' : ''}"
                href="/rooms"
              >
                <i class="fas fa-door-open me-1"></i> Rooms
              </a>
            </li>
            <li class="nav-item">
              <a
                class="nav-link ${pageContext.request.requestURI.contains('/schedule') ? 'active' : ''}"
                href="/schedule"
              >
                <i class="fas fa-calendar-check me-1"></i> Schedule
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <div class="container mt-4"></div>
  </body>
</html>
