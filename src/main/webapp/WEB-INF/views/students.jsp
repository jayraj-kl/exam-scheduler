<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ include file="layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
  <h1><i class="fas fa-user-graduate me-2"></i>Student Management</h1>
  <button
    type="button"
    class="btn btn-primary"
    data-bs-toggle="modal"
    data-bs-target="#addStudentModal"
  >
    <i class="fas fa-plus me-2"></i>Add New Student
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
            placeholder="Search students..."
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
      <table class="table table-hover mb-0" id="studentsTable">
        <thead class="table-light">
          <tr>
            <th scope="col">#</th>
            <th scope="col">Student ID</th>
            <th scope="col">Name</th>
            <th scope="col">Program</th>
            <th scope="col">Semester</th>
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

<!-- Add Student Modal -->
<div
  class="modal fade"
  id="addStudentModal"
  tabindex="-1"
  aria-labelledby="addStudentModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="addStudentModalLabel">Add New Student</h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
          aria-label="Close"
        ></button>
      </div>
      <div class="modal-body">
        <form id="studentForm">
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="studentId" class="form-label">Student ID</label>
              <input type="text" class="form-control" id="studentId" required />
            </div>
            <div class="col-md-6">
              <label for="studentName" class="form-label">Full Name</label>
              <input
                type="text"
                class="form-control"
                id="studentName"
                required
              />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="email" class="form-label">Email</label>
              <input type="email" class="form-control" id="email" required />
            </div>
            <div class="col-md-6">
              <label for="phone" class="form-label">Phone Number</label>
              <input type="tel" class="form-control" id="phone" required />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="program" class="form-label">Program</label>
              <select class="form-select" id="program" required>
                <option value="">Select program</option>
                <option value="computer-science">Computer Science</option>
                <option value="information-technology">
                  Information Technology
                </option>
                <option value="electronics">Electronics Engineering</option>
                <option value="mechanical">Mechanical Engineering</option>
              </select>
            </div>
            <div class="col-md-6">
              <label for="semester" class="form-label">Semester</label>
              <select class="form-select" id="semester" required>
                <option value="">Select semester</option>
                <option value="1">1</option>
                <option value="2">2</option>
                <option value="3">3</option>
                <option value="4">4</option>
                <option value="5">5</option>
                <option value="6">6</option>
                <option value="7">7</option>
                <option value="8">8</option>
              </select>
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="status" class="form-label">Status</label>
              <select class="form-select" id="status" required>
                <option value="">Select status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
                <option value="graduated">Graduated</option>
              </select>
            </div>
            <div class="col-md-6">
              <label for="enrollmentDate" class="form-label"
                >Enrollment Date</label
              >
              <input
                type="date"
                class="form-control"
                id="enrollmentDate"
                required
              />
            </div>
          </div>
          <div class="mb-3">
            <label for="address" class="form-label">Address</label>
            <textarea class="form-control" id="address" rows="3"></textarea>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          Cancel
        </button>
        <button type="button" class="btn btn-primary">Save Student</button>
      </div>
    </div>
  </div>
</div>

<%@ include file="layout/footer.jsp" %>
