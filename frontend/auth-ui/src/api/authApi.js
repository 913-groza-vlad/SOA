const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:4000";

export async function login(username, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ username, password })
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `Login failed with status ${res.status}`);
  }

  const data = await res.json().catch(() => null);

  if (!data) {
    throw new Error("Empty or invalid JSON from server");
  }

  // 👇 this matches your backend exactly
  const token = data.accessToken;

  if (!token || !token.trim()) {
    throw new Error("Empty token from server (no accessToken in response)");
  }

  return token.trim();
}