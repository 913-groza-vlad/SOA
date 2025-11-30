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
  return decoded.role || decoded["roles"] || null;
}

export function getUsernameFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  return decoded.sub || decoded.username || null;
}