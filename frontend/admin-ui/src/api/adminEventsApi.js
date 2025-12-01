import { apiClient } from './apiClient';

export function getAllEvents(page = 0, size = 20) {
  return apiClient.get(`/api/admin/events/?page=${page}&size=${size}`);
}

export function getAppointmentEvents(page = 0, size = 20) {
  return apiClient.get(
    `/api/admin/events/appointments?page=${page}&size=${size}`
  );
}

export function getNotificationEvents(page = 0, size = 20) {
  return apiClient.get(
    `/api/admin/events/notifications?page=${page}&size=${size}`
  );
}
