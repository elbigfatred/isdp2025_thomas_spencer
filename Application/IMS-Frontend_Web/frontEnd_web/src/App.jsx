import { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";
import axios from "axios";

function App() {
  const [user, setUser] = useState(
    JSON.parse(localStorage.getItem("user")) || null
  );

  const [darkMode, setDarkMode] = useState(
    JSON.parse(localStorage.getItem("darkMode")) || false
  );

  useEffect(() => {
    localStorage.setItem("darkMode", JSON.stringify(darkMode));
  }, [darkMode]);

  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  const CustomerPortal = () => {
    const navigate = useNavigate();

    useEffect(() => {
      handleOnlineCustomerLogin().then((data) => {
        if (data) {
          navigate("/dashboard"); // Redirect only after login success
        }
      });
    }, []);

    return <p>Logging in as Online Customer...</p>;
  };

  const handleOnlineCustomerLogin = async () => {
    try {
      const response = await axios.post("http://localhost:8080/api/login", {
        username: "online",
        password: "Something!",
      });

      localStorage.setItem("user", JSON.stringify(response.data));
      setUser(response.data); // Ensure user state is updated

      return response.data;
    } catch (err) {
      console.error("Login Error:", err);

      if (!err.response) {
        alert(
          "Network error: Unable to connect to the server. Please contact a Bullseye Administrator."
        );
      } else if (err.response.status === 428) {
        alert(
          "Password change required. Please update your password using the secure desktop app."
        );
      } else {
        alert(err.response?.data || "Invalid username or password");
      }
    }
  };

  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={
            user ? (
              <Navigate to="/dashboard" />
            ) : (
              <LoginPage
                onLogin={setUser}
                darkMode={darkMode}
                setDarkMode={setDarkMode}
              />
            )
          }
        />
        <Route
          path="/dashboard"
          element={
            user ? (
              <Dashboard
                user={user}
                onLogout={handleLogout}
                darkMode={darkMode}
                setDarkMode={setDarkMode}
              />
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route path="/customerPortal" element={<CustomerPortal />} />
      </Routes>
    </Router>
  );
}

export default App;
