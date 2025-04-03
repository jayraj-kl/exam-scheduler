<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ include file="layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
  <h1><i class="fas fa-calendar-check me-2"></i>Exam Schedule</h1>
  <div>
    <button
      type="button"
      class="btn btn-outline-primary me-2"
      id="exportScheduleBtn"
    >
      <i class="fas fa-download me-1"></i>Export Schedule
    </button>
    <button
      type="button"
      class="btn btn-primary"
      data-bs-toggle="modal"
      data-bs-target="#generateScheduleModal"
    >
      <i class="fas fa-calendar-plus me-2"></i>Generate New Schedule
    </button>
  </div>
</div>

<!-- Add container for schedule data and alerts -->
<div id="scheduleContainer"></div>

<div class="card mb-4">
  <div class="card-header bg-light">
    <ul class="nav nav-tabs card-header-tabs" id="scheduleTab" role="tablist">
      <li class="nav-item" role="presentation">
        <button
          class="nav-link active"
          id="calendarView-tab"
          data-bs-toggle="tab"
          data-bs-target="#calendarView"
          type="button"
          role="tab"
          aria-controls="calendarView"
          aria-selected="true"
        >
          <i class="fas fa-calendar-alt me-1"></i> Calendar View
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          id="listView-tab"
          data-bs-toggle="tab"
          data-bs-target="#listView"
          type="button"
          role="tab"
          aria-controls="listView"
          aria-selected="false"
        >
          <i class="fas fa-list me-1"></i> List View
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          id="roomView-tab"
          data-bs-toggle="tab"
          data-bs-target="#roomView"
          type="button"
          role="tab"
          aria-controls="roomView"
          aria-selected="false"
        >
          <i class="fas fa-door-open me-1"></i> Room View
        </button>
      </li>
    </ul>
  </div>
  <div class="card-body">
    <div class="tab-content" id="scheduleTabContent">
      <div
        class="tab-pane fade show active"
        id="calendarView"
        role="tabpanel"
        aria-labelledby="calendarView-tab"
      >
        <!-- Calendar header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="mb-0" id="currentMonthYear">May 2023</h4>
          </div>
          <div class="btn-group">
            <button
              type="button"
              class="btn btn-outline-secondary btn-sm"
              id="prevMonth"
            >
              <i class="fas fa-chevron-left"></i>
            </button>
            <button
              type="button"
              class="btn btn-outline-secondary btn-sm"
              id="nextMonth"
            >
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>

        <!-- Schedule filter and controls -->
        <div class="row mb-4">
          <div class="col-md-8">
            <div class="input-group">
              <span class="input-group-text">Schedule:</span>
              <select class="form-select" id="scheduleSelect">
                <option value="1" selected>
                  Midterm Examination Schedule - May 2023
                </option>
                <option value="2">
                  Final Examination Schedule - June 2023
                </option>
              </select>
            </div>
          </div>
          <div class="col-md-4 mt-3 mt-md-0">
            <div class="input-group">
              <span class="input-group-text">Filter:</span>
              <select class="form-select" id="filterSelect">
                <option value="all" selected>All Programs</option>
                <option value="cs">Computer Science</option>
                <option value="it">Information Technology</option>
                <option value="ee">Electronics Engineering</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Calendar view -->
        <div class="table-responsive">
          <table class="table table-bordered calendar-table">
            <thead>
              <tr>
                <th>Sunday</th>
                <th>Monday</th>
                <th>Tuesday</th>
                <th>Wednesday</th>
                <th>Thursday</th>
                <th>Friday</th>
                <th>Saturday</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="text-muted">30</td>
                <td>
                  1
                  <div class="calendar-event bg-primary">
                    <small>9:00 AM - Mathematics</small>
                  </div>
                </td>
                <td>
                  2
                  <div class="calendar-event bg-success">
                    <small>9:00 AM - Physics</small>
                  </div>
                </td>
                <td>3</td>
                <td>
                  4
                  <div class="calendar-event bg-danger">
                    <small>9:00 AM - Chemistry</small>
                  </div>
                  <div class="calendar-event bg-info">
                    <small>2:00 PM - Biology</small>
                  </div>
                </td>
                <td>5</td>
                <td>6</td>
              </tr>
              <tr>
                <td>7</td>
                <td>
                  8
                  <div class="calendar-event bg-warning">
                    <small>9:00 AM - Computer Science</small>
                  </div>
                </td>
                <td>9</td>
                <td>
                  10
                  <div class="calendar-event bg-primary">
                    <small>2:00 PM - English</small>
                  </div>
                </td>
                <td>11</td>
                <td>12</td>
                <td>13</td>
              </tr>
              <tr>
                <td>14</td>
                <td>15</td>
                <td>16</td>
                <td>17</td>
                <td>18</td>
                <td>19</td>
                <td>20</td>
              </tr>
              <tr>
                <td>21</td>
                <td>22</td>
                <td>23</td>
                <td>24</td>
                <td>25</td>
                <td>26</td>
                <td>27</td>
              </tr>
              <tr>
                <td>28</td>
                <td>29</td>
                <td>30</td>
                <td>31</td>
                <td class="text-muted">1</td>
                <td class="text-muted">2</td>
                <td class="text-muted">3</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div
        class="tab-pane fade"
        id="listView"
        role="tabpanel"
        aria-labelledby="listView-tab"
      >
        <!-- Schedule filter and search for list view -->
        <div class="row mb-4">
          <div class="col-md-4">
            <div class="input-group">
              <span class="input-group-text">Schedule:</span>
              <select class="form-select" id="listScheduleSelect">
                <option value="1" selected>
                  Midterm Examination Schedule - May 2023
                </option>
                <option value="2">
                  Final Examination Schedule - June 2023
                </option>
              </select>
            </div>
          </div>
          <div class="col-md-4 mt-3 mt-md-0">
            <div class="input-group">
              <span class="input-group-text">Program:</span>
              <select class="form-select" id="listFilterSelect">
                <option value="all" selected>All Programs</option>
                <option value="cs">Computer Science</option>
                <option value="it">Information Technology</option>
                <option value="ee">Electronics Engineering</option>
              </select>
            </div>
          </div>
          <div class="col-md-4 mt-3 mt-md-0">
            <div class="input-group">
              <span class="input-group-text"
                ><i class="fas fa-search"></i
              ></span>
              <input
                type="text"
                class="form-control"
                placeholder="Search exams..."
              />
            </div>
          </div>
        </div>

        <!-- List view table -->
        <div class="table-responsive">
          <table class="table table-hover table-striped">
            <thead class="table-light">
              <tr>
                <th>Date</th>
                <th>Time</th>
                <th>Subject</th>
                <th>Program</th>
                <th>Room</th>
                <th>Faculty</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>May 1, 2023</td>
                <td>9:00 AM - 12:00 PM</td>
                <td>Mathematics</td>
                <td>Computer Science</td>
                <td>A-101</td>
                <td>Dr. Smith</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="btn btn-outline-info">
                      <i class="fas fa-info-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td>May 2, 2023</td>
                <td>9:00 AM - 12:00 PM</td>
                <td>Physics</td>
                <td>Electronics Engineering</td>
                <td>B-203</td>
                <td>Dr. Johnson</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="btn btn-outline-info">
                      <i class="fas fa-info-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td>May 4, 2023</td>
                <td>9:00 AM - 12:00 PM</td>
                <td>Chemistry</td>
                <td>Information Technology</td>
                <td>A-101</td>
                <td>Dr. Williams</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="btn btn-outline-info">
                      <i class="fas fa-info-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td>May 4, 2023</td>
                <td>2:00 PM - 5:00 PM</td>
                <td>Biology</td>
                <td>Information Technology</td>
                <td>B-203</td>
                <td>Dr. Brown</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="btn btn-outline-info">
                      <i class="fas fa-info-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td>May 8, 2023</td>
                <td>9:00 AM - 12:00 PM</td>
                <td>Computer Science</td>
                <td>Computer Science</td>
                <td>A-101</td>
                <td>Dr. Davis</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="btn btn-outline-info">
                      <i class="fas fa-info-circle"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div
        class="tab-pane fade"
        id="roomView"
        role="tabpanel"
        aria-labelledby="roomView-tab"
      >
        <!-- Room view filters -->
        <div class="row mb-4">
          <div class="col-md-4">
            <div class="input-group">
              <span class="input-group-text">Date:</span>
              <input
                type="date"
                class="form-control"
                id="roomViewDate"
                value="2023-05-01"
              />
            </div>
          </div>
          <div class="col-md-4 mt-3 mt-md-0">
            <div class="input-group">
              <span class="input-group-text">Building:</span>
              <select class="form-select" id="buildingFilter">
                <option value="all" selected>All Buildings</option>
                <option value="main">Main Building</option>
                <option value="engineering">Engineering Block</option>
              </select>
            </div>
          </div>
          <div class="col-md-4 mt-3 mt-md-0">
            <div class="input-group">
              <span class="input-group-text"
                ><i class="fas fa-search"></i
              ></span>
              <input
                type="text"
                class="form-control"
                placeholder="Search rooms..."
              />
            </div>
          </div>
        </div>

        <!-- Room view grid -->
        <div class="row">
          <div class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100">
              <div
                class="card-header bg-light d-flex justify-content-between align-items-center"
              >
                <h5 class="mb-0">Room A-101</h5>
                <span class="badge bg-success">Available</span>
              </div>
              <div class="card-body">
                <p class="card-text mb-1">
                  <strong>Building:</strong> Main Building
                </p>
                <p class="card-text mb-1"><strong>Floor:</strong> 1st Floor</p>
                <p class="card-text mb-1">
                  <strong>Capacity:</strong> 40 students
                </p>

                <h6 class="mt-3 mb-2">Scheduled Exams:</h6>
                <div class="list-group list-group-flush small">
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 9:00 AM -
                        12:00 PM</span
                      >
                      <span class="badge bg-primary">Morning</span>
                    </div>
                    <div>Mathematics - Computer Science</div>
                    <div class="text-muted">Faculty: Dr. Smith</div>
                  </div>
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 2:00 PM -
                        5:00 PM</span
                      >
                      <span class="badge bg-danger">Afternoon</span>
                    </div>
                    <div>Not scheduled</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100">
              <div
                class="card-header bg-light d-flex justify-content-between align-items-center"
              >
                <h5 class="mb-0">Room B-203</h5>
                <span class="badge bg-warning">Partially Booked</span>
              </div>
              <div class="card-body">
                <p class="card-text mb-1">
                  <strong>Building:</strong> Engineering Block
                </p>
                <p class="card-text mb-1"><strong>Floor:</strong> 2nd Floor</p>
                <p class="card-text mb-1">
                  <strong>Capacity:</strong> 35 students
                </p>

                <h6 class="mt-3 mb-2">Scheduled Exams:</h6>
                <div class="list-group list-group-flush small">
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 9:00 AM -
                        12:00 PM</span
                      >
                      <span class="badge bg-primary">Morning</span>
                    </div>
                    <div>Not scheduled</div>
                  </div>
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 2:00 PM -
                        5:00 PM</span
                      >
                      <span class="badge bg-danger">Afternoon</span>
                    </div>
                    <div>Biology - Information Technology</div>
                    <div class="text-muted">Faculty: Dr. Brown</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100">
              <div
                class="card-header bg-light d-flex justify-content-between align-items-center"
              >
                <h5 class="mb-0">Room C-105</h5>
                <span class="badge bg-success">Available</span>
              </div>
              <div class="card-body">
                <p class="card-text mb-1">
                  <strong>Building:</strong> Main Building
                </p>
                <p class="card-text mb-1"><strong>Floor:</strong> 1st Floor</p>
                <p class="card-text mb-1">
                  <strong>Capacity:</strong> 30 students
                </p>

                <h6 class="mt-3 mb-2">Scheduled Exams:</h6>
                <div class="list-group list-group-flush small">
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 9:00 AM -
                        12:00 PM</span
                      >
                      <span class="badge bg-primary">Morning</span>
                    </div>
                    <div>Not scheduled</div>
                  </div>
                  <div class="list-group-item px-0">
                    <div class="d-flex justify-content-between">
                      <span
                        ><i class="fas fa-clock text-muted me-1"></i> 2:00 PM -
                        5:00 PM</span
                      >
                      <span class="badge bg-danger">Afternoon</span>
                    </div>
                    <div>Not scheduled</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="card-footer bg-light">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <button class="btn btn-outline-success btn-sm" id="sendEmailsBtn">
          <i class="fas fa-envelope me-1"></i>Send Schedule Emails
        </button>
      </div>
      <div>
        <button
          class="btn btn-outline-secondary btn-sm me-2"
          id="printScheduleBtn"
        >
          <i class="fas fa-print me-1"></i>Print Schedule
        </button>
        <button class="btn btn-outline-primary btn-sm" id="downloadPDFBtn">
          <i class="fas fa-file-pdf me-1"></i>Download PDF
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Generate Schedule Modal -->
<div
  class="modal fade"
  id="generateScheduleModal"
  tabindex="-1"
  aria-labelledby="generateScheduleModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="generateScheduleModalLabel">
          Generate New Schedule
        </h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
          aria-label="Close"
        ></button>
      </div>
      <div class="modal-body">
        <form id="generateScheduleForm">
          <div class="mb-3">
            <label for="scheduleName" class="form-label">Schedule Name</label>
            <input
              type="text"
              class="form-control"
              id="scheduleName"
              placeholder="e.g. Midterm Exams May 2023"
              required
            />
          </div>
          <div class="mb-3">
            <label for="startDate" class="form-label">Start Date</label>
            <input type="date" class="form-control" id="startDate" required />
          </div>
          <div class="mb-3">
            <label for="endDate" class="form-label">End Date</label>
            <input type="date" class="form-control" id="endDate" required />
          </div>
          <div class="mb-3">
            <label class="form-label">Programs to Include</label>
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value="cs"
                id="csProgram"
                checked
              />
              <label class="form-check-label" for="csProgram">
                Computer Science
              </label>
            </div>
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value="it"
                id="itProgram"
                checked
              />
              <label class="form-check-label" for="itProgram">
                Information Technology
              </label>
            </div>
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value="ee"
                id="eeProgram"
                checked
              />
              <label class="form-check-label" for="eeProgram">
                Electronics Engineering
              </label>
            </div>
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value="me"
                id="meProgram"
                checked
              />
              <label class="form-check-label" for="meProgram">
                Mechanical Engineering
              </label>
            </div>
          </div>
          <div class="mb-3">
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value=""
                id="includeWeekends"
              />
              <label class="form-check-label" for="includeWeekends">
                Include weekends in scheduling
              </label>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          Cancel
        </button>
        <button type="button" class="btn btn-primary" id="generateScheduleBtn">
          Generate Schedule
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="layout/footer.jsp" %>
