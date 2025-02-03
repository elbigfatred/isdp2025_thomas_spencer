/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState } from "react";
import axios from "axios";
import { Container, TextField, Button, Typography, Paper, Box } from "@mui/material";

const LoginPage = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await axios.post("http://localhost:8080/api/login", {
        username,
        password,
      });

      localStorage.setItem("user", JSON.stringify(response.data));
      onLogin(response.data);
    } catch (err) {
      setError(err.response?.data || "Invalid username or password");
    }
  };

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh", // ✅ Centers login box in the middle of the screen
        backgroundColor: "#f0f2f5", // Light background color (optional)
      }}
    >
      <Paper elevation={3} sx={{ padding: 4, textAlign: "center", width: 350 }}>
        <Typography variant="h5" gutterBottom>
          Login
        </Typography>

        <Box component="form" onSubmit={handleLogin} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <TextField label="Username" variant="outlined" fullWidth value={username} onChange={(e) => setUsername(e.target.value)} required />
          <TextField label="Password" type="password" variant="outlined" fullWidth value={password} onChange={(e) => setPassword(e.target.value)} required />
          <Button variant="contained" color="primary" type="submit" fullWidth>
            Login
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default LoginPage;
