import { useState, useMemo } from "react";
import UsersTable from "../components/UsersTable";
import SitesTable from "../components/SitesTable";
import OrdersPage from "../components/OrdersPage/OrdersDashboard";
import DeliveriesDashboard from "../components/DeliveriesPage/DeliveriesDashboard";
import OnlineOrdersDashboard from "../components/OnlineOrdersPage/OnlineOrdersDashboard";

import {
  Button,
  Box,
  Typography,
  Switch,
  FormControlLabel,
  createTheme,
  ThemeProvider,
  IconButton,
  Tooltip,
} from "@mui/material";

import bullseyeLogo from "../assets/bullseye1.png";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline";

const Dashboard = ({ user, onLogout, darkMode, setDarkMode }) => {
  const userRoles = user.roles.map((role) => role.posn.permissionLevel);

  // ✅ Updated Role-Based Access Controls
  const hasOrdersAccess =
    userRoles.includes("Warehouse Manager") ||
    userRoles.includes("Administrator") ||
    userRoles.includes("Store Manager");

  const hasDeliveryAccess =
    userRoles.includes("Delivery") || userRoles.includes("Administrator");

  const hasOnlineOrdersAccess =
    userRoles.includes("Administrator") ||
    userRoles.includes("Store Worker") ||
    userRoles.includes("Store Manager") ||
    userRoles.includes("Online Customer");

  const hasUsersAccess =
    userRoles.includes("Regional Manager") ||
    userRoles.includes("Financial Manager") ||
    userRoles.includes("Warehouse Manager") ||
    userRoles.includes("Store Manager") ||
    userRoles.includes("Warehouse Worker") ||
    userRoles.includes("Administrator");

  const hasSitesAccess = hasUsersAccess; // Same roles as user access

  // ✅ Auto-Pick the First Available Tab
  const availableTabs = [];
  if (hasSitesAccess) availableTabs.push("sites");
  if (hasUsersAccess) availableTabs.push("users");
  if (hasOrdersAccess) availableTabs.push("orders");
  if (hasDeliveryAccess) availableTabs.push("deliveries");

  const [activePage, setActivePage] = useState(availableTabs[0] || null);

  const theme = useMemo(
    () =>
      createTheme({
        palette: {
          mode: darkMode ? "dark" : "light",
        },
      }),
    [darkMode]
  );

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
          {/* Logo */}
          <Box sx={{ width: "80%", marginBottom: 1 }}>
            <img
              src={bullseyeLogo}
              alt="Bullseye Logo"
              style={{ width: "100%" }}
            />
          </Box>

          <Typography variant="h6" sx={{ marginBottom: 2 }}>
            Dashboard
          </Typography>

          {/* Help Tooltip */}
          <Tooltip title="Select a tab on the left to navigate." arrow>
            <IconButton sx={{ marginBottom: 1 }}>
              <HelpOutlineIcon />
            </IconButton>
          </Tooltip>

          {/* Dark Mode Toggle */}
          <FormControlLabel
            control={
              <Switch
                checked={darkMode}
                onChange={() => {
                  setDarkMode(!darkMode);
                  localStorage.setItem("darkMode", JSON.stringify(!darkMode));
                }}
              />
            }
            label="Dark Mode"
            sx={{ marginBottom: 2 }}
          />

          {/* Navigation Buttons */}
          {hasSitesAccess && (
            <Button
              variant={activePage === "sites" ? "contained" : "outlined"}
              onClick={() => setActivePage("sites")}
              sx={{ width: "100%", marginBottom: 1 }}
            >
              Sites
            </Button>
          )}

          {hasUsersAccess && (
            <Button
              variant={activePage === "users" ? "contained" : "outlined"}
              onClick={() => setActivePage("users")}
              sx={{ width: "100%", marginBottom: 1 }}
            >
              Users
            </Button>
          )}

          {hasOrdersAccess && (
            <Button
              variant={activePage === "orders" ? "contained" : "outlined"}
              onClick={() => setActivePage("orders")}
              sx={{ width: "100%", marginBottom: 1 }}
            >
              Store Orders
            </Button>
          )}

          {hasDeliveryAccess && (
            <Button
              variant={activePage === "deliveries" ? "contained" : "outlined"}
              onClick={() => setActivePage("deliveries")}
              sx={{ width: "100%", marginBottom: 1 }}
            >
              Deliveries
            </Button>
          )}

          {hasOnlineOrdersAccess && (
            <Button
              variant={
                activePage === "online-orders" ? "contained" : "outlined"
              }
              onClick={() => setActivePage("online-orders")}
              sx={{ width: "100%", marginBottom: 1 }}
            >
              Online Orders
            </Button>
          )}

          <Box sx={{ flexGrow: 1 }} />

          <Button
            variant="contained"
            color="error"
            onClick={onLogout}
            sx={{ width: "100%", marginBottom: 2 }}
          >
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
            {/* Dynamic Page Rendering */}
            {activePage === "sites" && hasSitesAccess && <SitesTable />}
            {activePage === "users" && hasUsersAccess && <UsersTable />}
            {activePage === "orders" && hasOrdersAccess ? (
              <OrdersPage user={user} />
            ) : (
              activePage === "orders" && <Typography>Access Denied</Typography>
            )}
            {activePage === "deliveries" && hasDeliveryAccess ? (
              <DeliveriesDashboard />
            ) : (
              activePage === "deliveries" && (
                <Typography>Access Denied</Typography>
              )
            )}
            {activePage === "online-orders" && hasOnlineOrdersAccess ? (
              <OnlineOrdersDashboard user={user} />
            ) : (
              activePage === "online-orders" && (
                <Typography>Access Denied</Typography>
              )
            )}

            {/* ✅ If no page is selected, show this message */}
            {!activePage && (
              <Typography
                variant="h6"
                sx={{ textAlign: "center", marginTop: 4 }}
              >
                Select a tab on the left to get started.
              </Typography>
            )}
          </Box>
        </Box>
      </Box>
    </ThemeProvider>
  );
};

export default Dashboard;
