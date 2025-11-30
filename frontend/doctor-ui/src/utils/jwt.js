import { jwtDecode } from 'jwt-decode';

export function decodeToken(token) {
  try {
    return jwtDecode(token);
  } catch (e) {
    console.error('Failed to decode JWT', e);
    return null;
  }
}

export function getRoleFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  return decoded.role || decoded['roles'] || null;
}

export function getUserIdFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;

  if (decoded.userId != null) return Number(decoded.userId);
  if (/^\d+$/.test(decoded.sub || '')) return Number(decoded.sub);
  return null;
}

export function getUsernameFromToken(token) {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  return decoded.sub || decoded.username || null;
}
