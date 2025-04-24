// Custom JavaScript for Exam Scheduler

document.addEventListener("DOMContentLoaded", function () {
  // Search functionality for tables
  document.querySelectorAll("#searchExam").forEach((input) => {
    input.addEventListener("keyup", function () {
      const searchTerm = this.value.toLowerCase();
      const table = this.closest(".card").querySelector("table");
      const rows = table.querySelectorAll("tbody tr");

      rows.forEach((row) => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? "" : "none";
      });
    });
  });

  // Initialize tooltips
  var tooltipTriggerList = [].slice.call(
    document.querySelectorAll('[data-bs-toggle="tooltip"]')
  );
  var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl);
  });

  // Add active class to current menu item
  const currentLocation = window.location.pathname;
  const navLinks = document.querySelectorAll(".navbar-nav .nav-link");

  navLinks.forEach((link) => {
    const href = link.getAttribute("href");
    if (
      href === currentLocation ||
      (href !== "/" && currentLocation.startsWith(href))
    ) {
      link.classList.add("active");
    }
  });

  // Load data for different pages
  if (currentLocation.includes("/students")) {
    loadStudents();
  } else if (currentLocation.includes("/rooms")) {
    loadRooms();
    loadRoomStats();
  } else if (currentLocation.includes("/exams")) {
    loadExams();
  } else if (currentLocation.includes("/schedule")) {
    loadScheduleData();
  }

  // Submit form handling for various forms
  setupFormHandling();

  // Setup schedule page functionality
  setupSchedulePage();
});

function loadStudents() {
  fetch("http://localhost:8080http://localhost:8080/api/students")
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      const tableBody = document.querySelector("#studentsTable tbody");
      if (!tableBody) return;

      tableBody.innerHTML = "";

      data.forEach((student, index) => {
        const statusClass =
          student.status === "active"
            ? "bg-success"
            : student.status === "inactive"
            ? "bg-danger"
            : "bg-secondary";

        const row = `
          <tr>
            <td>${index + 1}</td>
            <td>${student.studentId}</td>
            <td>${student.name}</td>
            <td>${student.program ? student.program.name : "N/A"}</td>
            <td>${student.semester}</td>
            <td><span class="badge ${statusClass}">${student.status}</span></td>
            <td>
              <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-outline-primary edit-student" data-id="${
                  student.id
                }">
                  <i class="fas fa-edit"></i>
                </button>
                <button type="button" class="btn btn-outline-danger delete-student" data-id="${
                  student.id
                }">
                  <i class="fas fa-trash"></i>
                </button>
                <button type="button" class="btn btn-outline-info view-student" data-id="${
                  student.id
                }">
                  <i class="fas fa-info-circle"></i>
                </button>
              </div>
            </td>
          </tr>
        `;

        tableBody.innerHTML += row;
      });

      // Add event listeners for edit and delete buttons
      document.querySelectorAll(".edit-student").forEach((button) => {
        button.addEventListener("click", function () {
          const studentId = this.getAttribute("data-id");
          editStudent(studentId);
        });
      });

      document.querySelectorAll(".delete-student").forEach((button) => {
        button.addEventListener("click", function () {
          const studentId = this.getAttribute("data-id");
          deleteStudent(studentId);
        });
      });

      document.querySelectorAll(".view-student").forEach((button) => {
        button.addEventListener("click", function () {
          const studentId = this.getAttribute("data-id");
          viewStudent(studentId);
        });
      });
    })
    .catch((error) => {
      console.error("Error fetching students:", error);
    });
}

function loadRooms() {
  // Using our enhanced apiFetch utility
  apiFetch("http://localhost:8080http://localhost:8080/api/rooms")
    .then((data) => {
      console.log("Rooms data received:", data); // Debug logging
      const tableBody = document.querySelector(".table tbody");
      if (!tableBody) {
        console.warn("Room table body not found in DOM");
        return;
      }

      tableBody.innerHTML = "";

      data.forEach((room, index) => {
        // Fix property access - use isAvailable instead of available
        const statusClass = room.isAvailable ? "bg-success" : "bg-warning";
        const statusText = room.isAvailable ? "Available" : "Booked";

        const row = `
          <tr>
            <td>${index + 1}</td>
            <td>${room.roomNumber}</td>
            <td>${room.building || "N/A"}</td>
            <td>${room.floor || "N/A"}</td>
            <td>${room.seatingCapacity}</td>
            <td><span class="badge ${statusClass}">${statusText}</span></td>
            <td>
              <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-outline-primary edit-room" data-id="${
                  room.id
                }">
                  <i class="fas fa-edit"></i>
                </button>
                <button type="button" class="btn btn-outline-danger delete-room" data-id="${
                  room.id
                }">
                  <i class="fas fa-trash"></i>
                </button>
                <button type="button" class="btn btn-outline-info view-room" data-id="${
                  room.id
                }">
                  <i class="fas fa-info-circle"></i>
                </button>
              </div>
            </td>
          </tr>
        `;

        tableBody.innerHTML += row;
      });

      // Add event listeners for edit and delete buttons
      document.querySelectorAll(".edit-room").forEach((button) => {
        button.addEventListener("click", function () {
          const roomId = this.getAttribute("data-id");
          editRoom(roomId);
        });
      });

      document.querySelectorAll(".delete-room").forEach((button) => {
        button.addEventListener("click", function () {
          const roomId = this.getAttribute("data-id");
          deleteRoom(roomId);
        });
      });

      document.querySelectorAll(".view-room").forEach((button) => {
        button.addEventListener("click", function () {
          const roomId = this.getAttribute("data-id");
          viewRoom(roomId);
        });
      });
    })
    .catch((error) => {
      console.error("Error fetching rooms:", error);
      // Display user-friendly error message on the page
      const tableBody = document.querySelector(".table tbody");
      if (tableBody) {
        tableBody.innerHTML = `
          <tr>
            <td colspan="7" class="text-center text-danger">
              <i class="fas fa-exclamation-triangle me-2"></i>
              Error loading rooms: ${error.message}
            </td>
          </tr>
        `;
      }
    });
}

function loadRoomStats() {
  apiFetch("http://localhost:8080http://localhost:8080/api/rooms/stats")
    .then((data) => {
      console.log("Room stats received:", data); // Debug logging

      // Find the stat cards and update their content
      const cards = document.querySelectorAll(".stats-card .card");
      if (cards.length >= 4) {
        cards[0].querySelector("h2").textContent = data.totalRooms || "0";
        cards[1].querySelector("h2").textContent = data.availableRooms || "0";
        cards[2].querySelector("h2").textContent = data.bookedRooms || "0";
        cards[3].querySelector("h2").textContent = data.totalCapacity || "0";
      } else {
        console.warn("Stats cards not found in DOM");
      }
    })
    .catch((error) => {
      console.error("Error fetching room stats:", error);
      // Optionally, display error message in the UI
    });
}

function loadExams() {
  fetch("http://localhost:8080/api/exams")
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((data) => {
      const tableBody = document.querySelector(".table tbody");
      if (!tableBody) return;

      tableBody.innerHTML = "";

      data.forEach((exam, index) => {
        const statusClass =
          exam.status === "scheduled"
            ? "bg-success"
            : exam.status === "pending"
            ? "bg-warning"
            : exam.status === "completed"
            ? "bg-info"
            : "bg-secondary";

        const row = `
          <tr>
            <td>${index + 1}</td>
            <td>${exam.examName}</td>
            <td>${exam.subject}</td>
            <td>${exam.examDate}</td>
            <td>${exam.duration} minutes</td>
            <td><span class="badge ${statusClass}">${exam.status}</span></td>
            <td>
              <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-outline-primary edit-exam" data-id="${
                  exam.id
                }">
                  <i class="fas fa-edit"></i>
                </button>
                <button type="button" class="btn btn-outline-danger delete-exam" data-id="${
                  exam.id
                }">
                  <i class="fas fa-trash"></i>
                </button>
                <button type="button" class="btn btn-outline-info view-exam" data-id="${
                  exam.id
                }">
                  <i class="fas fa-info-circle"></i>
                </button>
              </div>
            </td>
          </tr>
        `;

        tableBody.innerHTML += row;
      });

      // Add event listeners for edit and delete buttons
      document.querySelectorAll(".edit-exam").forEach((button) => {
        button.addEventListener("click", function () {
          const examId = this.getAttribute("data-id");
          editExam(examId);
        });
      });

      document.querySelectorAll(".delete-exam").forEach((button) => {
        button.addEventListener("click", function () {
          const examId = this.getAttribute("data-id");
          deleteExam(examId);
        });
      });

      document.querySelectorAll(".view-exam").forEach((button) => {
        button.addEventListener("click", function () {
          const examId = this.getAttribute("data-id");
          viewExam(examId);
        });
      });
    })
    .catch((error) => {
      console.error("Error fetching exams:", error);
    });
}

function loadScheduleData() {
  // First load the available schedules for the dropdown
  fetch("http://localhost:8080/api/schedule")
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((schedules) => {
      const scheduleSelect = document.getElementById("scheduleSelect");
      const listScheduleSelect = document.getElementById("listScheduleSelect");

      if (scheduleSelect) {
        scheduleSelect.innerHTML = "";
        schedules.forEach((schedule) => {
          const option = document.createElement("option");
          option.value = schedule.id;
          option.textContent = schedule.name;
          scheduleSelect.appendChild(option);
        });

        // Load schedule data for the first schedule
        if (schedules.length > 0) {
          loadScheduleDetails(schedules[0].id);
        }

        // Add event listener for schedule change
        scheduleSelect.addEventListener("change", function () {
          loadScheduleDetails(this.value);
        });
      }

      if (listScheduleSelect) {
        listScheduleSelect.innerHTML = "";
        schedules.forEach((schedule) => {
          const option = document.createElement("option");
          option.value = schedule.id;
          option.textContent = schedule.name;
          listScheduleSelect.appendChild(option);
        });
      }
    })
    .catch((error) => {
      console.error("Error fetching schedules:", error);
    });
}

function loadScheduleDetails(scheduleId) {
  // First fetch the schedule details
  fetch(`http://localhost:8080/api/schedule/${scheduleId}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((schedule) => {
      // Update schedule container with info
      const scheduleContainer = document.getElementById("scheduleContainer");
      if (scheduleContainer) {
        scheduleContainer.innerHTML = `
          <div class="alert alert-info mb-4">
            <h5 class="alert-heading">Schedule: ${schedule.name}</h5>
            <p class="mb-0">Duration: ${schedule.startDate} to ${schedule.endDate}</p>
          </div>
        `;
      }

      // Now fetch the exam slots for this schedule
      return fetch(`http://localhost:8080/api/schedule/${scheduleId}/slots`);
    })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    })
    .then((slots) => {
      if (!slots || slots.length === 0) {
        const scheduleContainer = document.getElementById("scheduleContainer");
        if (scheduleContainer) {
          scheduleContainer.innerHTML += `
            <div class="alert alert-warning">
              No exam slots found for this schedule. Add a slot to get started.
            </div>
          `;
        }
        return;
      }

      // Create a schedule object with the slots
      const scheduleWithSlots = {
        id: scheduleId,
        examSlots: slots,
      };

      // Update all views with the slots data
      updateCalendarView(scheduleWithSlots);
      updateListView(scheduleWithSlots);
      updateRoomView(scheduleWithSlots);
    })
    .catch((error) => {
      console.error("Error fetching schedule details:", error);
      const scheduleContainer = document.getElementById("scheduleContainer");
      if (scheduleContainer) {
        scheduleContainer.innerHTML += `
          <div class="alert alert-danger">
            Failed to load exam slots. Please try again later.
          </div>
        `;
      }
    });
}

function updateCalendarView(schedule) {
  // Implementation will depend on your calendar view structure
  // This is a simplified example
  const calendarBody = document.querySelector(".calendar-table tbody");
  if (!calendarBody) return;

  // Clear existing calendar events
  document.querySelectorAll(".calendar-event").forEach((event) => {
    event.remove();
  });

  // Add events to calendar based on schedule.examSlots
  schedule.examSlots.forEach((slot) => {
    const date = new Date(slot.examDate);
    const day = date.getDate();

    // Find the cell for this day
    const cells = calendarBody.querySelectorAll("td");
    let targetCell = null;

    cells.forEach((cell) => {
      if (cell.textContent.trim() == day) {
        targetCell = cell;
      }
    });

    if (targetCell) {
      const eventDiv = document.createElement("div");
      eventDiv.className = "calendar-event bg-primary";

      const timeText = slot.isMorningSlot ? "9:00 AM" : "2:00 PM";
      eventDiv.innerHTML = `<small>${timeText} - ${slot.subject.name}</small>`;

      targetCell.appendChild(eventDiv);
    }
  });
}

function updateListView(schedule) {
  const listViewTable = document.querySelector("#listView table tbody");
  if (!listViewTable) return;

  listViewTable.innerHTML = "";

  schedule.examSlots.forEach((slot) => {
    const row = document.createElement("tr");

    const timeFormat = slot.isMorningSlot
      ? "9:00 AM - 12:00 PM"
      : "2:00 PM - 5:00 PM";

    row.innerHTML = `
      <td>${slot.examDate}</td>
      <td>${timeFormat}</td>
      <td>${slot.subject.name}</td>
      <td>${slot.subject.program.name}</td>
      <td>${slot.room.roomNumber}</td>
      <td>${slot.faculty.name}</td>
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
    `;

    listViewTable.appendChild(row);
  });
}

function updateRoomView(schedule) {
  // This would be more complex in a real application
  // For simplicity, I'm showing a basic implementation
  const roomViewContainer = document.querySelector("#roomView .row");
  if (!roomViewContainer) return;

  // Get unique rooms from schedule
  const rooms = new Map();
  schedule.examSlots.forEach((slot) => {
    if (!rooms.has(slot.room.id)) {
      rooms.set(slot.room.id, {
        room: slot.room,
        slots: [],
      });
    }
    rooms.get(slot.room.id).slots.push(slot);
  });

  roomViewContainer.innerHTML = "";

  rooms.forEach((roomData) => {
    const room = roomData.room;
    const slots = roomData.slots;

    const roomCard = document.createElement("div");
    roomCard.className = "col-md-6 col-lg-4 mb-4";

    const status = room.isAvailable
      ? '<span class="badge bg-success">Available</span>'
      : '<span class="badge bg-warning">Partially Booked</span>';

    let morningSlot = "";
    let afternoonSlot = "";

    slots.forEach((slot) => {
      const slotInfo = `
        <div>${slot.subject.name} - ${slot.subject.program.name}</div>
        <div class="text-muted">Faculty: ${slot.faculty.name}</div>
      `;

      if (slot.isMorningSlot) {
        morningSlot = slotInfo;
      } else {
        afternoonSlot = slotInfo;
      }
    });

    if (!morningSlot) {
      morningSlot = "<div>Not scheduled</div>";
    }

    if (!afternoonSlot) {
      afternoonSlot = "<div>Not scheduled</div>";
    }

    roomCard.innerHTML = `
      <div class="card h-100">
        <div class="card-header bg-light d-flex justify-content-between align-items-center">
          <h5 class="mb-0">Room ${room.roomNumber}</h5>
          ${status}
        </div>
        <div class="card-body">
          <p class="card-text mb-1">
            <strong>Building:</strong> ${room.building || "N/A"}
          </p>
          <p class="card-text mb-1"><strong>Floor:</strong> ${
            room.floor || "N/A"
          }</p>
          <p class="card-text mb-1">
            <strong>Capacity:</strong> ${room.seatingCapacity} students
          </p>

          <h6 class="mt-3 mb-2">Scheduled Exams:</h6>
          <div class="list-group list-group-flush small">
            <div class="list-group-item px-0">
              <div class="d-flex justify-content-between">
                <span><i class="fas fa-clock text-muted me-1"></i> 9:00 AM -
                  12:00 PM</span>
                <span class="badge bg-primary">Morning</span>
              </div>
              ${morningSlot}
            </div>
            <div class="list-group-item px-0">
              <div class="d-flex justify-content-between">
                <span><i class="fas fa-clock text-muted me-1"></i> 2:00 PM -
                  5:00 PM</span>
                <span class="badge bg-danger">Afternoon</span>
              </div>
              ${afternoonSlot}
            </div>
          </div>
        </div>
      </div>
    `;

    roomViewContainer.appendChild(roomCard);
  });
}

function setupFormHandling() {
  // Exam Form
  const examForm = document.getElementById("examForm");
  if (examForm) {
    const saveButton = examForm.closest(".modal").querySelector(".btn-primary");
    saveButton.addEventListener("click", function () {
      if (examForm.checkValidity()) {
        // Collect form data
        const formData = new FormData(examForm);
        const data = {
          examName: formData.get("examName"),
          subject: formData.get("subject"),
          examDate: formData.get("examDate"),
          duration: parseInt(formData.get("duration")),
          startTime: formData.get("startTime"),
          status: formData.get("status"),
          description: formData.get("description"),
        };

        // Send to backend API
        fetch("http://localhost:8080/api/exams", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            // Close modal
            const modal = bootstrap.Modal.getInstance(
              document.getElementById("addExamModal")
            );
            modal.hide();

            // Show success message
            alert("Exam saved successfully!");

            // Reload exams to show the new one
            loadExams();
          })
          .catch((error) => {
            console.error("Error saving exam:", error);
            alert("Error saving exam: " + error.message);
          });
      } else {
        examForm.reportValidity();
      }
    });
  }

  // Student Form
  const studentForm = document.getElementById("studentForm");
  if (studentForm) {
    const saveButton = studentForm
      .closest(".modal")
      .querySelector(".btn-primary");
    saveButton.addEventListener("click", function () {
      if (studentForm.checkValidity()) {
        // Collect form data
        const formData = new FormData(studentForm);
        const data = {
          studentId: formData.get("studentId"),
          name: formData.get("studentName"),
          email: formData.get("email"),
          phone: formData.get("phone"),
          program: {
            id: parseInt(formData.get("program")),
          },
          semester: parseInt(formData.get("semester")),
          status: formData.get("status"),
          enrollmentDate: formData.get("enrollmentDate"),
          address: formData.get("address"),
        };

        // Send to backend API
        fetch("http://localhost:8080/api/students", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(data),
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            // Close modal
            const modal = bootstrap.Modal.getInstance(
              document.getElementById("addStudentModal")
            );
            modal.hide();

            // Show success message
            alert("Student saved successfully!");

            // Reload students to show the new one
            loadStudents();
          })
          .catch((error) => {
            console.error("Error saving student:", error);
            alert("Error saving student: " + error.message);
          });
      } else {
        studentForm.reportValidity();
      }
    });
  }

  // Room Form
  const roomForm = document.getElementById("roomForm");
  if (roomForm) {
    const saveButton = document.getElementById("saveRoomBtn");
    if (saveButton) {
      saveButton.addEventListener("click", function () {
        if (roomForm.checkValidity()) {
          // Collect form data
          const formData = new FormData(roomForm);
          const data = {
            roomNumber: formData.get("roomNumber"),
            building: formData.get("building"),
            floor: formData.get("floor"),
            seatingCapacity: parseInt(formData.get("capacity")),
            isAvailable: document.getElementById("isAvailable").checked,
          };

          // Send to backend API
          fetch("http://localhost:8080/api/rooms", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => {
              if (!response.ok) {
                throw new Error("Network response was not ok");
              }
              return response.json();
            })
            .then((data) => {
              // Close modal
              const modal = bootstrap.Modal.getInstance(
                document.getElementById("addRoomModal")
              );
              modal.hide();

              // Show success message
              alert("Room saved successfully!");

              // Reload rooms to show the new one
              loadRooms();
              loadRoomStats();
            })
            .catch((error) => {
              console.error("Error saving room:", error);
              alert("Error saving room: " + error.message);
            });
        } else {
          roomForm.reportValidity();
        }
      });
    }
  }

  // Generate Schedule Form
  const generateScheduleForm = document.getElementById("generateScheduleForm");
  if (generateScheduleForm) {
    const generateButton = document.getElementById("generateScheduleBtn");
    generateButton.addEventListener("click", function () {
      if (generateScheduleForm.checkValidity()) {
        // Show loading state
        generateButton.innerHTML =
          '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Generating...';
        generateButton.disabled = true;

        // Get selected program IDs (checkboxes)
        const programIds = [];
        ["csProgram", "itProgram", "eeProgram", "meProgram"].forEach((id) => {
          const checkbox = document.getElementById(id);
          if (checkbox && checkbox.checked) {
            programIds.push(checkbox.value);
          }
        });

        // Get form data
        const scheduleName = document.getElementById("scheduleName").value;
        const startDate = document.getElementById("startDate").value;
        const endDate = document.getElementById("endDate").value;
        const includeWeekends =
          document.getElementById("includeWeekends").checked;

        // Prepare API call
        const url = `http://localhost:8080/api/schedule/generate?startDate=${startDate}&endDate=${endDate}&scheduleName=${encodeURIComponent(
          scheduleName
        )}`;

        fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            programIds: programIds,
            includeWeekends: includeWeekends,
          }),
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            // Reset button state
            generateButton.innerHTML = "Generate Schedule";
            generateButton.disabled = false;

            // Close modal
            const modal = bootstrap.Modal.getInstance(
              document.getElementById("generateScheduleModal")
            );
            modal.hide();

            // Show success message and reload schedule data
            alert("Schedule generated successfully!");
            loadScheduleData();
          })
          .catch((error) => {
            // Reset button state
            generateButton.innerHTML = "Generate Schedule";
            generateButton.disabled = false;

            console.error("Error generating schedule:", error);
            alert("Error generating schedule: " + error.message);
          });
      } else {
        generateScheduleForm.reportValidity();
      }
    });
  }
}

function setupSchedulePage() {
  // Schedule page specific functionality
  const exportScheduleBtn = document.getElementById("exportScheduleBtn");
  if (exportScheduleBtn) {
    exportScheduleBtn.addEventListener("click", function () {
      const scheduleSelect = document.getElementById("scheduleSelect");
      if (!scheduleSelect || !scheduleSelect.value) {
        alert("Please select a schedule to export");
        return;
      }

      const scheduleId = scheduleSelect.value;
      window.location.href = `http://localhost:8080/api/schedule/${scheduleId}/export`;
    });
  }

  const sendEmailsBtn = document.getElementById("sendEmailsBtn");
  if (sendEmailsBtn) {
    sendEmailsBtn.addEventListener("click", function () {
      const scheduleSelect = document.getElementById("scheduleSelect");
      if (!scheduleSelect || !scheduleSelect.value) {
        alert("Please select a schedule to send emails");
        return;
      }

      const scheduleId = scheduleSelect.value;

      const confirmSend = confirm(
        "Are you sure you want to send schedule emails to all faculty members?"
      );
      if (confirmSend) {
        // Show loading state
        sendEmailsBtn.innerHTML =
          '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Sending...';
        sendEmailsBtn.disabled = true;

        fetch(`http://localhost:8080/api/schedule/${scheduleId}/email`, {
          method: "POST",
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            // Reset button state
            sendEmailsBtn.innerHTML =
              '<i class="fas fa-envelope me-1"></i>Send Schedule Emails';
            sendEmailsBtn.disabled = false;

            // Show success message
            alert(
              `Schedule emails sent successfully! ${data.emailsSent} emails were sent.`
            );
          })
          .catch((error) => {
            // Reset button state
            sendEmailsBtn.innerHTML =
              '<i class="fas fa-envelope me-1"></i>Send Schedule Emails';
            sendEmailsBtn.disabled = false;

            console.error("Error sending emails:", error);
            alert("Error sending emails: " + error.message);
          });
      }
    });
  }

  // Calendar navigation
  const prevMonthBtn = document.getElementById("prevMonth");
  const nextMonthBtn = document.getElementById("nextMonth");
  const currentMonthYearElement = document.getElementById("currentMonthYear");

  if (prevMonthBtn && nextMonthBtn && currentMonthYearElement) {
    const months = [
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December",
    ];
    let currentDate = new Date();

    function updateCalendarHeader() {
      currentMonthYearElement.textContent = `${
        months[currentDate.getMonth()]
      } ${currentDate.getFullYear()}`;
    }

    prevMonthBtn.addEventListener("click", function () {
      currentDate = new Date(
        currentDate.getFullYear(),
        currentDate.getMonth() - 1,
        1
      );
      updateCalendarHeader();

      // Get the selected schedule and reload for the new month
      const scheduleSelect = document.getElementById("scheduleSelect");
      if (scheduleSelect && scheduleSelect.value) {
        loadScheduleDetails(scheduleSelect.value);
      }
    });

    nextMonthBtn.addEventListener("click", function () {
      currentDate = new Date(
        currentDate.getFullYear(),
        currentDate.getMonth() + 1,
        1
      );
      updateCalendarHeader();

      // Get the selected schedule and reload for the new month
      const scheduleSelect = document.getElementById("scheduleSelect");
      if (scheduleSelect && scheduleSelect.value) {
        loadScheduleDetails(scheduleSelect.value);
      }
    });
  }

  // Print schedule functionality
  const printScheduleBtn = document.getElementById("printScheduleBtn");
  if (printScheduleBtn) {
    printScheduleBtn.addEventListener("click", function () {
      window.print();
    });
  }

  // Download PDF button
  const downloadPDFBtn = document.getElementById("downloadPDFBtn");
  if (downloadPDFBtn) {
    downloadPDFBtn.addEventListener("click", function () {
      const scheduleSelect = document.getElementById("scheduleSelect");
      if (!scheduleSelect || !scheduleSelect.value) {
        alert("Please select a schedule to download");
        return;
      }

      alert("PDF download functionality would be implemented here.");
      // This would normally use a library like jsPDF or call a backend endpoint
    });
  }

  // Add faculty availability check function
  async function checkFacultyAvailability(date, startTime, endTime) {
    try {
      const response = await fetch(
        `http://localhost:8080/api/schedule/faculty/available?date=${date}&startTime=${startTime}&endTime=${endTime}`
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(
          errorData.message || "Failed to check faculty availability"
        );
      }

      return await response.json();
    } catch (error) {
      console.error("Error checking faculty availability:", error);
      // Show user-friendly error message in the UI
      const alertDiv = document.createElement("div");
      alertDiv.className = "alert alert-danger alert-dismissible fade show";
      alertDiv.innerHTML = `
        <strong>Error checking faculty availability:</strong> ${error.message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      `;

      const container = document.getElementById("scheduleContainer");
      if (container) {
        container.insertBefore(alertDiv, container.firstChild);
      }

      return [];
    }
  }
}

// Helper functions for CRUD operations
function editStudent(id) {
  fetch(`http://localhost:8080/api/students/${id}`)
    .then((response) => response.json())
    .then((student) => {
      // Populate the form with student data
      document.getElementById("studentId").value = student.studentId;
      document.getElementById("studentName").value = student.name;
      document.getElementById("email").value = student.email;
      document.getElementById("phone").value = student.phone;
      document.getElementById("program").value = student.program
        ? student.program.id
        : "";
      document.getElementById("semester").value = student.semester;
      document.getElementById("status").value = student.status;
      document.getElementById("enrollmentDate").value = student.enrollmentDate;
      document.getElementById("address").value = student.address;

      // Open the modal
      const modal = new bootstrap.Modal(
        document.getElementById("addStudentModal")
      );
      modal.show();

      // Change the save button to update
      const saveButton = document.querySelector(
        "#addStudentModal .btn-primary"
      );
      saveButton.textContent = "Update Student";
      saveButton.setAttribute("data-id", id);

      // Temporarily change the save button click handler
      const originalClickHandler = saveButton.onclick;
      saveButton.onclick = function () {
        // Update student logic
        if (document.getElementById("studentForm").checkValidity()) {
          const formData = new FormData(document.getElementById("studentForm"));
          const data = {
            studentId: formData.get("studentId"),
            name: formData.get("studentName"),
            email: formData.get("email"),
            phone: formData.get("phone"),
            program: {
              id: parseInt(formData.get("program")),
            },
            semester: parseInt(formData.get("semester")),
            status: formData.get("status"),
            enrollmentDate: formData.get("enrollmentDate"),
            address: formData.get("address"),
          };

          fetch(`http://localhost:8080/api/students/${id}`, {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => response.json())
            .then(() => {
              modal.hide();
              alert("Student updated successfully!");
              loadStudents();

              // Restore original behavior
              saveButton.onclick = originalClickHandler;
              saveButton.textContent = "Save Student";
              saveButton.removeAttribute("data-id");
            })
            .catch((error) => {
              console.error("Error updating student:", error);
              alert("Error updating student: " + error.message);
            });
        } else {
          document.getElementById("studentForm").reportValidity();
        }
      };
    })
    .catch((error) => {
      console.error("Error fetching student:", error);
      alert("Error fetching student: " + error.message);
    });
}

function deleteStudent(id) {
  if (confirm("Are you sure you want to delete this student?")) {
    fetch(`http://localhost:8080/api/students/${id}`, {
      method: "DELETE",
    })
      .then((response) => response.json())
      .then((data) => {
        alert("Student deleted successfully!");
        loadStudents();
      })
      .catch((error) => {
        console.error("Error deleting student:", error);
        alert("Error deleting student: " + error.message);
      });
  }
}

function viewStudent(id) {
  fetch(`http://localhost:8080/api/students/${id}`)
    .then((response) => response.json())
    .then((student) => {
      alert(
        `Student Details:\nID: ${student.studentId}\nName: ${
          student.name
        }\nProgram: ${
          student.program ? student.program.name : "N/A"
        }\nSemester: ${student.semester}\nStatus: ${student.status}`
      );
    })
    .catch((error) => {
      console.error("Error fetching student:", error);
      alert("Error fetching student: " + error.message);
    });
}

function editRoom(id) {
  fetch(`http://localhost:8080/api/rooms/${id}`)
    .then((response) => response.json())
    .then((room) => {
      // Populate the form with room data
      document.getElementById("roomNumber").value = room.roomNumber;
      document.getElementById("building").value = room.building || "";
      document.getElementById("floor").value = room.floor || "";
      document.getElementById("capacity").value = room.seatingCapacity;
      document.getElementById("roomType").value = room.roomType || "";
      document.getElementById("isAvailable").checked = room.isAvailable;

      // Open the modal
      const modal = new bootstrap.Modal(
        document.getElementById("addRoomModal")
      );
      modal.show();

      // Change the save button to update
      const saveButton = document.getElementById("saveRoomBtn");
      saveButton.textContent = "Update Room";
      saveButton.setAttribute("data-id", id);

      // Temporarily change the save button click handler
      const originalClickHandler = saveButton.onclick;
      saveButton.onclick = function () {
        // Update room logic
        if (document.getElementById("roomForm").checkValidity()) {
          const formData = new FormData(document.getElementById("roomForm"));
          const data = {
            roomNumber: formData.get("roomNumber"),
            building: formData.get("building"),
            floor: formData.get("floor"),
            seatingCapacity: parseInt(formData.get("capacity")),
            roomType: formData.get("roomType"),
            isAvailable: document.getElementById("isAvailable").checked,
          };

          fetch(`http://localhost:8080/api/rooms/${id}`, {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => response.json())
            .then(() => {
              modal.hide();
              alert("Room updated successfully!");
              loadRooms();
              loadRoomStats();

              // Restore original behavior
              saveButton.onclick = originalClickHandler;
              saveButton.textContent = "Save Room";
              saveButton.removeAttribute("data-id");
            })
            .catch((error) => {
              console.error("Error updating room:", error);
              alert("Error updating room: " + error.message);
            });
        } else {
          document.getElementById("roomForm").reportValidity();
        }
      };
    })
    .catch((error) => {
      console.error("Error fetching room:", error);
      alert("Error fetching room: " + error.message);
    });
}

function deleteRoom(id) {
  if (confirm("Are you sure you want to delete this room?")) {
    fetch(`http://localhost:8080/api/rooms/${id}`, {
      method: "DELETE",
    })
      .then((response) => response.json())
      .then((data) => {
        alert("Room deleted successfully!");
        loadRooms();
        loadRoomStats();
      })
      .catch((error) => {
        console.error("Error deleting room:", error);
        alert("Error deleting room: " + error.message);
      });
  }
}

function viewRoom(id) {
  fetch(`http://localhost:8080/api/rooms/${id}`)
    .then((response) => response.json())
    .then((room) => {
      alert(
        `Room Details:\nNumber: ${room.roomNumber}\nBuilding: ${
          room.building || "N/A"
        }\nFloor: ${room.floor || "N/A"}\nCapacity: ${
          room.seatingCapacity
        }\nStatus: ${room.isAvailable ? "Available" : "Booked"}`
      );
    })
    .catch((error) => {
      console.error("Error fetching room:", error);
      alert("Error fetching room: " + error.message);
    });
}

function editExam(id) {
  fetch(`http://localhost:8080/api/exams/${id}`)
    .then((response) => response.json())
    .then((exam) => {
      // Populate the form with exam data
      document.getElementById("examName").value = exam.examName;
      document.getElementById("subject").value = exam.subject;
      document.getElementById("examDate").value = exam.examDate;
      document.getElementById("duration").value = exam.duration;
      document.getElementById("startTime").value = exam.startTime;
      document.getElementById("status").value = exam.status;
      document.getElementById("description").value = exam.description || "";

      // Open the modal
      const modal = new bootstrap.Modal(
        document.getElementById("addExamModal")
      );
      modal.show();

      // Change the save button to update
      const saveButton = document.querySelector("#addExamModal .btn-primary");
      saveButton.textContent = "Update Exam";
      saveButton.setAttribute("data-id", id);

      // Temporarily change the save button click handler
      const originalClickHandler = saveButton.onclick;
      saveButton.onclick = function () {
        // Update exam logic
        if (document.getElementById("examForm").checkValidity()) {
          const formData = new FormData(document.getElementById("examForm"));
          const data = {
            examName: formData.get("examName"),
            subject: formData.get("subject"),
            examDate: formData.get("examDate"),
            duration: parseInt(formData.get("duration")),
            startTime: formData.get("startTime"),
            status: formData.get("status"),
            description: formData.get("description"),
          };

          fetch(`http://localhost:8080/api/exams/${id}`, {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
          })
            .then((response) => response.json())
            .then(() => {
              modal.hide();
              alert("Exam updated successfully!");
              loadExams();

              // Restore original behavior
              saveButton.onclick = originalClickHandler;
              saveButton.textContent = "Save Exam";
              saveButton.removeAttribute("data-id");
            })
            .catch((error) => {
              console.error("Error updating exam:", error);
              alert("Error updating exam: " + error.message);
            });
        } else {
          document.getElementById("examForm").reportValidity();
        }
      };
    })
    .catch((error) => {
      console.error("Error fetching exam:", error);
      alert("Error fetching exam: " + error.message);
    });
}

function deleteExam(id) {
  if (confirm("Are you sure you want to delete this exam?")) {
    fetch(`http://localhost:8080/api/exams/${id}`, {
      method: "DELETE",
    })
      .then((response) => response.json())
      .then((data) => {
        alert("Exam deleted successfully!");
        loadExams();
      })
      .catch((error) => {
        console.error("Error deleting exam:", error);
        alert("Error deleting exam: " + error.message);
      });
  }
}

function viewExam(id) {
  fetch(`http://localhost:8080/api/exams/${id}`)
    .then((response) => response.json())
    .then((exam) => {
      alert(
        `Exam Details:\nName: ${exam.examName}\nSubject: ${exam.subject}\nDate: ${exam.examDate}\nDuration: ${exam.duration} minutes\nTime: ${exam.startTime}\nStatus: ${exam.status}`
      );
    })
    .catch((error) => {
      console.error("Error fetching exam:", error);
      alert("Error fetching exam: " + error.message);
    });
}
