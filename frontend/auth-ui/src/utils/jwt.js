import { jwtDecode } from "jwt-decode";

export function decodeToken(token) {
  try {
    return jwtDecode(token);
  } catch (e) {
    console.error("Failed to decode JWT", e);
    return null;
  }
}

export function getRoleFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  // your backend uses "role" claim
  return decoded.role || decoded["roles"] || null;
}