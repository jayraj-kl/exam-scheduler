/**
 * Enhanced fetch API for the Exam Scheduler application
 * Includes proper error handling and CSRF token support
 */

// Store the CSRF token and header name
let csrfToken = "";
let csrfHeaderName = "X-CSRF-TOKEN";

// Function to initialize CSRF token from meta tags (to be called on page load)
function initializeCsrfToken() {
  const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

  if (csrfTokenMeta && csrfHeaderMeta) {
    csrfToken = csrfTokenMeta.content;
    csrfHeaderName = csrfHeaderMeta.content;
    console.log("CSRF token initialized");
  } else {
    console.warn("CSRF meta tags not found, token not initialized");
  }
}

// Enhanced fetch function with automatic CSRF token inclusion and better error handling
function apiFetch(url, options = {}) {
  // Set default headers
  options.headers = options.headers || {};

  // Add CSRF token if available
  if (csrfToken) {
    options.headers[csrfHeaderName] = csrfToken;
  }

  // Add default content type for POST/PUT requests if not specified
  if (
    (options.method === "POST" || options.method === "PUT") &&
    !options.headers["Content-Type"] &&
    !(options.body instanceof FormData)
  ) {
    options.headers["Content-Type"] = "application/json";
  }

  // Add Accept header if not specified
  if (!options.headers["Accept"]) {
    options.headers["Accept"] = "application/json";
  }

  // Execute fetch with enhanced error handling
  return fetch(url, options)
    .then((response) => {
      if (!response.ok) {
        // For 4xx/5xx responses, try to get error details from response
        return response.text().then((text) => {
          try {
            // Try to parse as JSON
            const errorData = JSON.parse(text);
            throw new Error(
              errorData.message ||
                `API error: ${response.status} ${response.statusText}`
            );
          } catch (e) {
            // If not valid JSON, use text or status
            throw new Error(
              text || `API error: ${response.status} ${response.statusText}`
            );
          }
        });
      }

      // Check if response is empty
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return response.json();
      } else {
        return response.text();
      }
    })
    .catch((error) => {
      // Log error for debugging
      console.error("API fetch error:", error);

      // Rethrow for handling by the caller
      throw error;
    });
}

// Initialize CSRF token when the page loads
document.addEventListener("DOMContentLoaded", function () {
  initializeCsrfToken();
});
