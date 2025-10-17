// Custom error class for API-related errors
export class ApiError extends Error {
  status: number;   // HTTP status code (e.g., 400, 404, 500)
  details?: any;    // Extra details from backend error response to be displayed in frontned

  constructor(status: number, message: string, details?: any) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

// Generic HTTP function to make API requests
export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  // Get token (if logged in) from localStorage
  const token = localStorage.getItem("token");

  // Build request headers
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(init?.headers || {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}), //This is being used in postman for testing
  };
  // Make the fetch call to the API
  const res = await fetch(path.startsWith("/api") ? path : `/api${path}`, {
    ...init,
    headers,
  });
  // Parse the response text
  const text = await res.text();
  if (!res.ok) {
    let details: any;
    try {
      details = text ? JSON.parse(text) : undefined;
    } catch { }
    const message = details?.message || text || res.statusText;
    throw new ApiError(res.status, message, details);
  }
  
  // For successful responses, only parse as JSON if there's content and it looks like JSON
  if (!text) {
    return undefined as T;
  }
  
  try {
    return JSON.parse(text) as T;
  } catch {
    // If JSON parsing fails, return the text as is (for plain text responses)
    return text as T;
  }
}

