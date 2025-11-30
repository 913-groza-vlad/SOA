import { apiClient } from './apiClient';

export async function fetchAppointmentsForDoctor(doctorId) {
  return apiClient.get(`/api/appointments/doctor/${doctorId}`);
}

export async function cancelAppointment(id) {
  return apiClient.post(`/api/appointments/${id}/cancel`);
}
