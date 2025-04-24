<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ include file="layout/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
  <h1><i class="fas fa-door-open me-2"></i>Room Management</h1>
  <button
    type="button"
    class="btn btn-primary"
    data-bs-toggle="modal"
    data-bs-target="#addRoomModal"
  >
    <i class="fas fa-plus me-2"></i>Add New Room
  </button>
</div>

<div class="row mb-4">
  <div class="col-md-3 mb-4 mb-md-0">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-door-open fa-3x text-primary mb-3"></i>
        <h2>15</h2>
        <p class="mb-0">Total Rooms</p>
      </div>
    </div>
  </div>
  <div class="col-md-3 mb-4 mb-md-0">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
        <h2>8</h2>
        <p class="mb-0">Available Rooms</p>
      </div>
    </div>
  </div>
  <div class="col-md-3 mb-4 mb-md-0">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-calendar-check fa-3x text-warning mb-3"></i>
        <h2>7</h2>
        <p class="mb-0">Booked Rooms</p>
      </div>
    </div>
  </div>
  <div class="col-md-3">
    <div class="card h-100">
      <div class="card-body text-center">
        <i class="fas fa-users fa-3x text-info mb-3"></i>
        <h2>420</h2>
        <p class="mb-0">Total Capacity</p>
      </div>
    </div>
  </div>
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
            placeholder="Search rooms..."
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
      <table class="table table-hover mb-0" id="roomsTable">
        <thead class="table-light">
          <tr>
            <th scope="col">#</th>
            <th scope="col">Room Number</th>
            <th scope="col">Building</th>
            <th scope="col">Floor</th>
            <th scope="col">Capacity</th>
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

<!-- Add Room Modal -->
<div
  class="modal fade"
  id="addRoomModal"
  tabindex="-1"
  aria-labelledby="addRoomModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="addRoomModalLabel">Add New Room</h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
          aria-label="Close"
        ></button>
      </div>
      <div class="modal-body">
        <form id="roomForm">
          <div class="mb-3">
            <label for="roomNumber" class="form-label">Room Number</label>
            <input type="text" class="form-control" id="roomNumber" required />
          </div>
          <div class="mb-3">
            <label for="building" class="form-label">Building</label>
            <input type="text" class="form-control" id="building" required />
          </div>
          <div class="mb-3">
            <label for="floor" class="form-label">Floor</label>
            <select class="form-select" id="floor" required>
              <option value="">Select floor</option>
              <option value="Ground Floor">Ground Floor</option>
              <option value="1st Floor">1st Floor</option>
              <option value="2nd Floor">2nd Floor</option>
              <option value="3rd Floor">3rd Floor</option>
              <option value="4th Floor">4th Floor</option>
            </select>
          </div>
          <div class="mb-3">
            <label for="capacity" class="form-label">Seating Capacity</label>
            <input
              type="number"
              class="form-control"
              id="capacity"
              min="1"
              required
            />
          </div>
          <div class="mb-3">
            <label for="roomType" class="form-label">Room Type</label>
            <select class="form-select" id="roomType" required>
              <option value="">Select room type</option>
              <option value="classroom">Classroom</option>
              <option value="lab">Laboratory</option>
              <option value="lecture-hall">Lecture Hall</option>
              <option value="seminar-room">Seminar Room</option>
            </select>
          </div>
          <div class="mb-3">
            <div class="form-check">
              <input
                class="form-check-input"
                type="checkbox"
                value=""
                id="isAvailable"
                checked
              />
              <label class="form-check-label" for="isAvailable">
                Available for booking
              </label>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          Cancel
        </button>
        <button type="button" class="btn btn-primary" id="saveRoomBtn">
          Save Room
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="layout/footer.jsp" %>
