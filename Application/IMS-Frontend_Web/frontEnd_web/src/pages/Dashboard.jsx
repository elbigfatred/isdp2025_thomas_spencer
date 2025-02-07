/* eslint-disable react/prop-types */
import { useState, useMemo } from "react";
import UsersTable from "../components/UsersTable";
import SitesTable from "../components/SitesTable";
import OrdersPage from "../components/OrdersPage/OrdersDashboard";
import { Button, Box, Typography, Switch, FormControlLabel, createTheme, ThemeProvider } from "@mui/material";

const Dashboard = ({ user, onLogout }) => {
  const [activePage, setActivePage] = useState("sites");
  const [darkMode, setDarkMode] = useState(false);

  const userRoles = user.roles.map((role) => role.posn.permissionLevel);
  const hasOrdersAccess = userRoles.includes("Warehouse Manager") || userRoles.includes("Store Manager");

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode: darkMode ? "dark" : "light",
        },
      }),
    [darkMode]
  );

  const pages = {
    sites: <SitesTable />,
    users: <UsersTable />,
    orders: hasOrdersAccess ? <OrdersPage user={user} /> : <p>Access Denied</p>,
  };

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ display: "flex", height: "100vh" }}>
        {/* Sidebar */}
        <Box
          sx={{
            width: 220,
            background: darkMode ? "#2c2c2c" : "#f4f4f4",
            color: darkMode ? "#fff" : "#000",
            padding: 2,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            height: "100vh",
            boxShadow: "2px 0 5px rgba(0,0,0,0.2)",
          }}
        >
          <Typography variant="h6" sx={{ marginBottom: 2 }}>
            Dashboard
          </Typography>

          {/* Dark Mode Toggle */}
          <FormControlLabel control={<Switch checked={darkMode} onChange={() => setDarkMode(!darkMode)} />} label="Dark Mode" sx={{ marginBottom: 2 }} />

          <Button variant={activePage === "sites" ? "contained" : "outlined"} onClick={() => setActivePage("sites")} sx={{ width: "100%", marginBottom: 1 }}>
            Sites
          </Button>

          <Button variant={activePage === "users" ? "contained" : "outlined"} onClick={() => setActivePage("users")} sx={{ width: "100%", marginBottom: 1 }}>
            Users
          </Button>

          {hasOrdersAccess && (
            <Button variant={activePage === "orders" ? "contained" : "outlined"} onClick={() => setActivePage("orders")} sx={{ width: "100%", marginBottom: 1 }}>
              Orders
            </Button>
          )}

          <Box sx={{ flexGrow: 1 }} />

          <Button variant="contained" color="error" onClick={onLogout} sx={{ width: "100%", marginBottom: 2 }}>
            Logout
          </Button>
        </Box>

        {/* Main Content */}
        <Box
          sx={{
            flex: 1,
            padding: 4,
            overflow: "auto",
            background: darkMode ? "#1e1e1e" : "#ffffff",
            color: darkMode ? "#fff" : "#000",
          }}
        >
          <Typography variant="h4" gutterBottom>
            Welcome, {user.username}!
          </Typography>
          <Typography variant="h6" gutterBottom>
            Location: {user.site.siteName}
          </Typography>

          <Box
            sx={{
              background: darkMode ? "#333" : "#fff",
              padding: 3,
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            {pages[activePage]}
          </Box>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default Dashboard;
