import { useState } from "react";
import axios from "axios";
import {
  Container,
  TextField,
  Button,
  Typography,
  Paper,
  Box,
  Alert,
} from "@mui/material";

const LoginPage = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(""); // Clear previous errors

    try {
      const response = await axios.post("http://localhost:8080/api/login", {
        username,
        password,
      });

      console.log("Login Response:", response.data);

      // Store user data in localStorage (temporary)
      localStorage.setItem("user", JSON.stringify(response.data));

      // Notify parent component of successful login
      onLogin(response.data);
    } catch (err) {
      console.error("Login failed:", err);
      setError(err.response?.data || "Invalid username or password");
    }
  };

  return (
    <Container maxWidth="xs">
      <Paper
        elevation={3}
        sx={{ padding: 4, marginTop: 8, textAlign: "center" }}
      >
        <Typography variant="h5" gutterBottom>
          Login
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

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
    </Container>
  );
};

export default LoginPage;
