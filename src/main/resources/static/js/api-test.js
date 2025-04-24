// // API Test Script for Exam Scheduler
// document.addEventListener("DOMContentLoaded", function () {
//   // Create test container
//   const container = document.createElement("div");
//   container.id = "api-test-container";
//   container.style.padding = "20px";
//   container.style.backgroundColor = "#f8f9fa";
//   container.style.borderRadius = "5px";
//   container.style.marginBottom = "20px";

//   // Add title
//   const title = document.createElement("h4");
//   title.textContent = "API Test Panel";
//   container.appendChild(title);

//   // Create test buttons
//   const endpoints = [
//     { name: "Get All Rooms", method: "GET", url: "/api/rooms" },
//     { name: "Get Room Stats", method: "GET", url: "/api/rooms/stats" },
//     { name: "Get All Students", method: "GET", url: "/api/students" },
//     { name: "Get All Exams", method: "GET", url: "/api/exams" },
//     { name: "Get Schedules", method: "GET", url: "/api/schedule" },
//   ];

//   // Output area
//   const output = document.createElement("div");
//   output.style.marginTop = "10px";
//   output.style.padding = "10px";
//   output.style.backgroundColor = "#fff";
//   output.style.border = "1px solid #ddd";
//   output.style.borderRadius = "3px";
//   output.style.maxHeight = "300px";
//   output.style.overflow = "auto";
//   output.style.fontFamily = "monospace";
//   output.style.fontSize = "12px";

//   // Create buttons
//   endpoints.forEach((endpoint) => {
//     const btn = document.createElement("button");
//     btn.className = "btn btn-sm btn-outline-primary me-2 mb-2";
//     btn.textContent = endpoint.name;
//     btn.addEventListener("click", function () {
//       testEndpoint(endpoint.method, endpoint.url, output);
//     });
//     container.appendChild(btn);
//   });

//   // Add POST test form for room creation
//   const form = document.createElement("div");
//   form.className = "mt-3 mb-3";
//   form.innerHTML = `
//         <h5>Test POST Room</h5>
//         <div class="input-group mb-2">
//             <input type="text" class="form-control form-control-sm" id="test-room-number" placeholder="Room Number" value="T101">
//             <input type="text" class="form-control form-control-sm" id="test-building" placeholder="Building" value="Test Building">
//             <input type="number" class="form-control form-control-sm" id="test-capacity" placeholder="Capacity" value="30">
//             <button class="btn btn-sm btn-outline-success" id="test-create-room">Create Room</button>
//         </div>
//     `;
//   container.appendChild(form);

//   // Add the output area
//   container.appendChild(output);

//   // Add to page
//   document.querySelector(".container").prepend(container);

//   // Add event listener for room creation
//   document
//     .getElementById("test-create-room")
//     .addEventListener("click", function () {
//       const roomData = {
//         roomNumber: document.getElementById("test-room-number").value,
//         building: document.getElementById("test-building").value,
//         seatingCapacity: parseInt(
//           document.getElementById("test-capacity").value
//         ),
//         floor: "1st Floor",
//         roomType: "classroom",
//         isAvailable: true,
//       };

//       testPostEndpoint("/api/rooms", roomData, output);
//     });
// });

// function testEndpoint(method, url, outputElement) {
//   outputElement.innerHTML = `<div>Testing ${method} ${url}...</div>`;

//   fetch(url, {
//     method: method,
//     headers: {
//       Accept: "application/json",
//     },
//   })
//     .then((response) => {
//       const statusLine = `<div>Status: ${response.status} ${response.statusText}</div>`;
//       outputElement.innerHTML = statusLine;

//       if (!response.ok) {
//         throw new Error(`HTTP error: ${response.status}`);
//       }
//       return response.json();
//     })
//     .then((data) => {
//       const resultJson = JSON.stringify(data, null, 2);
//       outputElement.innerHTML += `<pre>${resultJson}</pre>`;
//     })
//     .catch((error) => {
//       outputElement.innerHTML += `<div style="color: red;">Error: ${error.message}</div>`;
//       console.error("API test error:", error);
//     });
// }

// function testPostEndpoint(url, data, outputElement) {
//   outputElement.innerHTML = `<div>Testing POST ${url}...</div>`;
//   outputElement.innerHTML += `<div>Request body: ${JSON.stringify(data)}</div>`;

//   fetch(url, {
//     method: "POST",
//     headers: {
//       "Content-Type": "application/json",
//       Accept: "application/json",
//     },
//     body: JSON.stringify(data),
//   })
//     .then((response) => {
//       const statusLine = `<div>Status: ${response.status} ${response.statusText}</div>`;
//       outputElement.innerHTML += statusLine;

//       if (!response.ok) {
//         throw new Error(`HTTP error: ${response.status}`);
//       }
//       return response.json();
//     })
//     .then((data) => {
//       const resultJson = JSON.stringify(data, null, 2);
//       outputElement.innerHTML += `<pre>${resultJson}</pre>`;
//     })
//     .catch((error) => {
//       outputElement.innerHTML += `<div style="color: red;">Error: ${error.message}</div>`;
//       console.error("API test error:", error);
//     });
// }
