/* eslint-disable react/prop-types */
import { useState, useMemo } from "react";
import axios from "axios";
import {
  TextField,
  Button,
  Typography,
  Paper,
  Box,
  Alert,
  Switch,
  FormControlLabel,
  createTheme,
  ThemeProvider,
  IconButton,
  Tooltip,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon
import bullseyeLogo from "../assets/bullseye1.png";

const LoginPage = ({ onLogin, darkMode, setDarkMode }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  // Dynamic theme switching
  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode: darkMode ? "dark" : "light",
        },
      }),
    [darkMode]
  );

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await axios.post("http://localhost:8080/api/login", {
        username,
        password,
      });

      localStorage.setItem("user", JSON.stringify(response.data));
      if (onLogin) {
        onLogin(response.data);
      } else {
        console.error("onLogin function is undefined!");
      }
    } catch (err) {
      console.error("Login Error:", err);

      if (!err.response) {
        setError(
          "Network error: Unable to connect to the server. Please contact a Bullseye Administrator."
        );
      } else if (err.response.status === 428) {
        setError(
          "Password change required. Please update your password using the secure desktop app."
        );
      } else {
        setError(err.response?.data || "Invalid username or password");
      }
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          height: "100vh",
          backgroundColor: darkMode ? "#1e1e1e" : "#f0f2f5",
        }}
      >
        {/* Dark Mode Toggle Positioned Above the Login Box */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            marginBottom: 2,
            color: darkMode ? "#ffffff" : "#333",
          }}
        >
          <FormControlLabel
            control={
              <Switch
                checked={darkMode}
                onChange={() => setDarkMode(!darkMode)}
              />
            }
            label={
              <Typography
                sx={{
                  fontSize: "0.9rem",
                  fontWeight: 500,
                  color: darkMode ? "#ffffff" : "#333",
                }}
              >
                Dark Mode
              </Typography>
            }
          />
        </Box>

        {/* Login Box */}
        <Paper
          elevation={3}
          sx={{
            padding: 4,
            textAlign: "center",
            width: 350,
            backgroundColor: darkMode ? "#333" : "#ffffff",
            color: darkMode ? "#fff" : "#000",
            position: "relative",
          }}
        >
          {/* ✅ Help Icon Positioned Top Right */}
          <Tooltip title="Enter your username and password to log in. If you forget your password, contact an administrator.">
            <IconButton
              sx={{
                position: "absolute",
                top: 8,
                right: 8,
                color: darkMode ? "#ffffff" : "#333",
              }}
            >
              <HelpOutlineIcon />
            </IconButton>
          </Tooltip>

          {/* Title */}
          <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: 1 }}>
            Bullseye Inventory Management System
          </Typography>

          {/* Logo */}
          <Box
            sx={{ display: "flex", justifyContent: "center", marginBottom: 2 }}
          >
            <img
              src={bullseyeLogo}
              alt="Bullseye Logo"
              style={{ width: "50%", maxWidth: "120px" }}
            />
          </Box>

          {/* Login Title */}
          <Typography variant="h5" gutterBottom>
            Login
          </Typography>

          {error && (
            <Alert severity="error" sx={{ marginBottom: 2 }}>
              {error}
            </Alert>
          )}

          <Box
            component="form"
            onSubmit={handleLogin}
            sx={{ display: "flex", flexDirection: "column", gap: 2 }}
          >
            <TextField
              label="Username"
              variant="outlined"
              fullWidth
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <TextField
              label="Password"
              type="password"
              variant="outlined"
              fullWidth
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Button variant="contained" color="primary" type="submit" fullWidth>
              Login
            </Button>
          </Box>
        </Paper>
      </Box>
    </ThemeProvider>
  );
};

export default LoginPage;
