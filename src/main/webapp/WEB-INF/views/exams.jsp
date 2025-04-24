<!-- filepath: c:\Users\Lenovo\IdeaProjects\exam-scheduler\src\main\webapp\WEB-INF\views\exams.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ include file="layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
  <h1><i class="fas fa-file-alt me-2"></i>Exam Management</h1>
  <button
    type="button"
    class="btn btn-primary"
    data-bs-toggle="modal"
    data-bs-target="#addExamModal"
  >
    <i class="fas fa-plus me-2"></i>Add New Exam
  </button>
</div>

<div class="card mb-4">
  <div class="card-header bg-light">
    <div class="row">
      <div class="col-md-6">
        <div class="input-group">
          <span class="input-group-text"><i class="fas fa-search"></i></span>
          <input
            type="text"
            id="searchExam"
            class="form-control"
            placeholder="Search exams..."
          />
        </div>
      </div>
      <div class="col-md-6">
        <div class="d-flex justify-content-md-end mt-3 mt-md-0">
          <div class="btn-group">
            <button type="button" class="btn btn-outline-secondary">
              <i class="fas fa-filter me-1"></i>Filter
            </button>
            <button type="button" class="btn btn-outline-secondary">
              <i class="fas fa-sort me-1"></i>Sort
            </button>
            <button type="button" class="btn btn-outline-secondary">
              <i class="fas fa-download me-1"></i>Export
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="card-body p-0">
    <div class="table-responsive">
      <table class="table table-hover mb-0" id="examsTable">
        <thead class="table-light">
          <tr>
            <th scope="col">#</th>
            <th scope="col">Exam Name</th>
            <th scope="col">Subject</th>
            <th scope="col">Date</th>
            <th scope="col">Duration</th>
            <th scope="col">Status</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <!-- Data will be loaded dynamically with JavaScript -->
        </tbody>
      </table>
    </div>
  </div>
  <div class="card-footer bg-light">
    <nav aria-label="Page navigation">
      <ul class="pagination justify-content-center mb-0">
        <li class="page-item disabled">
          <a class="page-link" href="#" tabindex="-1" aria-disabled="true"
            >Previous</a
          >
        </li>
        <li class="page-item active"><a class="page-link" href="#">1</a></li>
        <li class="page-item"><a class="page-link" href="#">2</a></li>
        <li class="page-item"><a class="page-link" href="#">3</a></li>
        <li class="page-item">
          <a class="page-link" href="#">Next</a>
        </li>
      </ul>
    </nav>
  </div>
</div>

<!-- Add Exam Modal -->
<div
  class="modal fade"
  id="addExamModal"
  tabindex="-1"
  aria-labelledby="addExamModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="addExamModalLabel">Add New Exam</h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
          aria-label="Close"
        ></button>
      </div>
      <div class="modal-body">
        <form id="examForm">
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="examName" class="form-label">Exam Name</label>
              <input type="text" class="form-control" id="examName" required />
            </div>
            <div class="col-md-6">
              <label for="subject" class="form-label">Subject</label>
              <input type="text" class="form-control" id="subject" required />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="examDate" class="form-label">Date</label>
              <input type="date" class="form-control" id="examDate" required />
            </div>
            <div class="col-md-6">
              <label for="duration" class="form-label"
                >Duration (minutes)</label
              >
              <input
                type="number"
                class="form-control"
                id="duration"
                required
              />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="startTime" class="form-label">Start Time</label>
              <input type="time" class="form-control" id="startTime" required />
            </div>
            <div class="col-md-6">
              <label for="status" class="form-label">Status</label>
              <select class="form-select" id="status" required>
                <option value="">Select status</option>
                <option value="scheduled">Scheduled</option>
                <option value="pending">Pending</option>
                <option value="completed">Completed</option>
                <option value="cancelled">Cancelled</option>
              </select>
            </div>
          </div>
          <div class="mb-3">
            <label for="description" class="form-label">Description</label>
            <textarea class="form-control" id="description" rows="3"></textarea>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          Cancel
        </button>
        <button type="button" class="btn btn-primary">Save Exam</button>
      </div>
    </div>
  </div>
</div>

<%@ include file="layout/footer.jsp" %>
