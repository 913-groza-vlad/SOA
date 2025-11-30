import { apiClient } from "./apiClient";

export async function fetchPatients(page = 0, size = 50) {
  return apiClient.get(`/api/patients/?page=${page}&size=${size}`);
}