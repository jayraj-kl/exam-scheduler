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

  // Submit form handling for various forms
  setupFormHandling();

  // Setup schedule page functionality
  setupSchedulePage();
});

function setupFormHandling() {
  // Exam Form
  const examForm = document.getElementById("examForm");
  if (examForm) {
    const saveButton = examForm.closest(".modal").querySelector(".btn-primary");
    saveButton.addEventListener("click", function () {
      if (examForm.checkValidity()) {
        // Collect form data
        const formData = new FormData(examForm);
        const data = Object.fromEntries(formData.entries());

        // TODO: Send to backend API
        console.log("Exam form data to be submitted:", data);

        // Close modal
        const modal = bootstrap.Modal.getInstance(
          document.getElementById("addExamModal")
        );
        modal.hide();

        // Show success message
        alert("Exam saved successfully!");
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
        const data = Object.fromEntries(formData.entries());

        // TODO: Send to backend API
        console.log("Student form data to be submitted:", data);

        // Close modal
        const modal = bootstrap.Modal.getInstance(
          document.getElementById("addStudentModal")
        );
        modal.hide();

        // Show success message
        alert("Student saved successfully!");
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
          const data = Object.fromEntries(formData.entries());

          // TODO: Send to backend API
          console.log("Room form data to be submitted:", data);

          // Close modal
          const modal = bootstrap.Modal.getInstance(
            document.getElementById("addRoomModal")
          );
          modal.hide();

          // Show success message
          alert("Room saved successfully!");
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
        // Collect form data
        const formData = new FormData(generateScheduleForm);
        const data = Object.fromEntries(formData.entries());

        // Show loading state
        generateButton.innerHTML =
          '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Generating...';
        generateButton.disabled = true;

        // Simulate API call
        setTimeout(() => {
          console.log("Schedule generation data:", data);

          // Reset button state
          generateButton.innerHTML = "Generate Schedule";
          generateButton.disabled = false;

          // Close modal
          const modal = bootstrap.Modal.getInstance(
            document.getElementById("generateScheduleModal")
          );
          modal.hide();

          // Show success message
          alert("Schedule generated successfully!");
        }, 2000); // Simulate 2 second API call
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
      // Simulate export action
      console.log("Exporting schedule...");
      alert("Schedule exported successfully!");
    });
  }

  const sendEmailsBtn = document.getElementById("sendEmailsBtn");
  if (sendEmailsBtn) {
    sendEmailsBtn.addEventListener("click", function () {
      const confirmSend = confirm(
        "Are you sure you want to send schedule emails to all faculty members?"
      );
      if (confirmSend) {
        // Show loading state
        sendEmailsBtn.innerHTML =
          '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Sending...';
        sendEmailsBtn.disabled = true;

        // Simulate API call
        setTimeout(() => {
          console.log("Sending schedule emails...");

          // Reset button state
          sendEmailsBtn.innerHTML =
            '<i class="fas fa-envelope me-1"></i>Send Schedule Emails';
          sendEmailsBtn.disabled = false;

          // Show success message
          alert("Schedule emails sent successfully!");
        }, 2000); // Simulate 2 second API call
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
      console.log("Calendar updated to previous month");
      // TODO: Fetch and update calendar data
    });

    nextMonthBtn.addEventListener("click", function () {
      currentDate = new Date(
        currentDate.getFullYear(),
        currentDate.getMonth() + 1,
        1
      );
      updateCalendarHeader();
      console.log("Calendar updated to next month");
      // TODO: Fetch and update calendar data
    });
  }

  // Print schedule functionality
  const printScheduleBtn = document.getElementById("printScheduleBtn");
  if (printScheduleBtn) {
    printScheduleBtn.addEventListener("click", function () {
      window.print();
    });
  }
}
