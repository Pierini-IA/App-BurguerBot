import { defineConfig, devices } from "@playwright/test";

/**
 * Config de E2E. Requiere el backend corriendo en :8080
 * (ver `backend/docker-compose.local.yml`).
 *
 * El backend acepta CORS solo desde localhost:3000, así que el dev server
 * del front tiene que correr ahí.
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 45_000,
  reporter: [["list"]],
  use: {
    baseURL: "http://localhost:3000",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev -- --port 3000",
    url: "http://localhost:3000",
    timeout: 120_000,
    reuseExistingServer: true,
    env: {
      NEXT_PUBLIC_API_URL: "http://localhost:8080/api",
      NEXT_PUBLIC_WS_URL: "http://localhost:8080",
    },
  },
});
