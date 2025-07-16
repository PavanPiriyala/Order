import {jwtDecode} from "jwt-decode";

export function getUserFromToken() {
  const cookieToken = document.cookie
    .split("; ")
    .find((row) => row.startsWith("token="));

  if (!cookieToken) return null;

  try {
    const token = cookieToken.split("=")[1];
    const decoded = jwtDecode(token);
    return decoded; // Contains { sub, name, email, etc. }
  } catch (err) {
    console.error("Failed to decode token", err);
    return null;
  }
}
 