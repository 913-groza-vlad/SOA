import { apiClient } from "./apiClient";

export async function fetchDoctors(page = 0, size = 50) {
  return apiClient.get(`/api/doctors/?page=${page}&size=${size}`);
}