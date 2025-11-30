import { apiClient } from './apiClient';

export async function fetchNotifications(page = 0, size = 20) {
  return apiClient.get(`/api/notifications?page=${page}&size=${size}`);
}

export async function fetchUnreadCount() {
  return apiClient.get(`/api/notifications/unread-count`);
}

export async function markNotificationRead(id) {
  return apiClient.post(`/api/notifications/${id}/read`, {});
}
