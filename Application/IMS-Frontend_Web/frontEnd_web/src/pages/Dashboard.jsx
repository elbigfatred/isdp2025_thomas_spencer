import { useState } from "react";
import UsersTable from "../components/UsersTable";
import SitesTable from "../components/SitesTable";
import OrdersPage from "../components/OrdersPage"; // New Orders Page
import { Button } from "@mui/material";

const Dashboard = ({ user, onLogout }) => {
  const [activePage, setActivePage] = useState("sites");

  // Extract role names from the user's roles array
  const userRoles = user.roles.map((role) => role.posn.permissionLevel);

  console.log("User Roles:", userRoles);

  // Check if user is Warehouse Manager or Store Manager
  const hasOrdersAccess =
    userRoles.includes("Warehouse Manager") ||
    userRoles.includes("Store Manager");

  const pages = {
    sites: <SitesTable />,
    users: <UsersTable />,
    orders: hasOrdersAccess ? <OrdersPage user={user} /> : <p>Access Denied</p>,
  };

  return (
    <div style={{ display: "flex", height: "100vh" }}>
      {/* Sidebar */}
      <div
        style={{
          width: "200px",
          background: "#f4f4f4",
          padding: "20px",
          borderRight: "1px solid #ccc",
        }}
      >
        <h3>Dashboard</h3>
        <Button
          variant="contained"
          onClick={() => setActivePage("sites")}
          style={{ display: "block", marginBottom: "10px" }}
        >
          Sites
        </Button>
        <Button
          variant="contained"
          onClick={() => setActivePage("users")}
          style={{ display: "block", marginBottom: "10px" }}
        >
          Users
        </Button>

        {/* Only show Orders button if user has permission */}
        {hasOrdersAccess && (
          <Button
            variant="contained"
            onClick={() => setActivePage("orders")}
            style={{ display: "block", marginBottom: "10px" }}
          >
            Orders
          </Button>
        )}

        <Button
          variant="contained"
          color="error"
          onClick={onLogout}
          style={{ marginTop: "20px" }}
        >
          Logout
        </Button>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, padding: "20px" }}>
        <h2>Welcome, {user.username}!</h2>
        <h2>Location: {user.site.siteName}</h2>
        {pages[activePage]}
      </div>
    </div>
  );
};

export default Dashboard;
