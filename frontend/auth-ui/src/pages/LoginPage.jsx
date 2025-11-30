import React, { useState } from "react";
import { login } from "../api/authApi.js";
import { getRoleFromToken } from "../utils/jwt.js";

const TOKEN_KEY = "authToken";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const token = await login(username, password);

      if (!token) {
        throw new Error("Empty token from server");
      }

      // store globally so patient-ui / doctor-ui can also use it
      localStorage.setItem(TOKEN_KEY, token);

      const role = getRoleFromToken(token);
      console.log("Decoded role:", role);

      if (!role) {
        throw new Error("No role claim in token");
      }

      // Redirect based on role
      if (role === "PATIENT") {
        window.location.href = "/patient/";
      } else if (role === "DOCTOR") {
        window.location.href = "/doctor/";
      } else if (role === "ADMIN") {
        window.location.href = "/admin/";
      } else {
        throw new Error(`Unknown role ${role}`);
      }
    } catch (err) {
      console.error(err);
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="login-title">MedApp Portal</h1>
        <p className="login-subtitle">Sign in with your account</p>

        <form onSubmit={handleSubmit} className="login-form">
          <label className="login-label">
            Username
            <input
              type="text"
              className="login-input"
              value={username}
              onChange={e => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </label>

          <label className="login-label">
            Password
            <div className="password-wrapper">
              <input
                type={showPassword ? "text" : "password"}
                className="login-input"
                value={password}
                onChange={e => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="toggle-password"
                onClick={() => setShowPassword(v => !v)}
              >
                {showPassword ? "Hide" : "Show"}
              </button>
            </div>
          </label>

          {error && <div className="login-error">{error}</div>}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}