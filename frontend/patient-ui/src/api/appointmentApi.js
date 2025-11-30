import { apiClient } from "./apiClient";

export async function fetchAppointmentsForPatient(patientId) {
  return apiClient.get(`/api/appointments/patient/${patientId}`);
}

export async function createAppointment(payload) {
  return apiClient.post("/api/appointments/", payload);
}

export async function updateAppointment(id, payload) {
  return apiClient.put(`/api/appointments/${id}`, payload);
}

export async function cancelAppointment(id) {
  return apiClient.post(`/api/appointments/${id}/cancel`);
}