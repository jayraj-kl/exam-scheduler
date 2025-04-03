<!-- filepath: c:\Users\Lenovo\IdeaProjects\exam-scheduler\src\main\webapp\WEB-INF\views\index.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ include file="layout/header.jsp" %>

<div class="row mb-4">
  <div class="col-12">
    <div class="card bg-light">
      <div class="card-body text-center py-5">
        <h1 class="display-4">Welcome to Exam Scheduler</h1>
        <p class="lead">
          Efficiently manage your examination schedule with our powerful tools.
        </p>
        <div class="mt-4">
          <a href="/schedule" class="btn btn-primary btn-lg me-2">
            <i class="fas fa-calendar-check me-2"></i>View Schedule
          </a>
          <a href="/exams" class="btn btn-outline-primary btn-lg">
            <i class="fas fa-file-alt me-2"></i>Manage Exams
          </a>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="row mb-4">
  <div class="col-md-4 mb-4 mb-md-0">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-file-alt fa-3x text-primary mb-3"></i>
        <h3>Manage Exams</h3>
        <p>
          Create, edit, and organize examination details including subjects,
          dates, and durations.
        </p>
        <a href="/exams" class="btn btn-outline-primary">Go to Exams</a>
      </div>
    </div>
  </div>
  <div class="col-md-4 mb-4 mb-md-0">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-user-graduate fa-3x text-primary mb-3"></i>
        <h3>Student Management</h3>
        <p>
          Track student information, enrollments, and examination assignments.
        </p>
        <a href="/students" class="btn btn-outline-primary">Manage Students</a>
      </div>
    </div>
  </div>
  <div class="col-md-4">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-door-open fa-3x text-primary mb-3"></i>
        <h3>Room Allocation</h3>
        <p>Manage examination venues, seating arrangements, and capacities.</p>
        <a href="/rooms" class="btn btn-outline-primary">Manage Rooms</a>
      </div>
    </div>
  </div>
</div>

<%@ include file="layout/footer.jsp" %>
