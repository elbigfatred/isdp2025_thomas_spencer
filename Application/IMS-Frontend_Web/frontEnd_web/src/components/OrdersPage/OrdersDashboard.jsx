/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import { Box, Typography, Select, MenuItem } from "@mui/material";
import OrderHistory from "./OrderHistory"; // View orders
import OrderForm from "./OrderForm"; // Create/edit orders
import axios from "axios";

const OrdersDashboard = ({ user }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0); // Used to trigger refresh
  const [selectedSite, setSelectedSite] = useState(user?.site?.id || ""); // Store selected site
  const [availableSites, setAvailableSites] = useState([]); // List of all sites

  useEffect(() => {
    // ✅ If user is a warehouse manager, fetch all sites
    console.log(user);
    if (user?.mainrole === "Warehouse Manager") {
      axios
        .get("http://localhost:8080/api/sites")
        .then((response) => {
          console.log("[DEBUG] Fetched sites:", response.data);
          setAvailableSites(response.data);
        })
        .catch(() => console.error("Failed to load sites"));
    }
  }, [user]);

  // Function to refresh the orders list
  const refreshOrders = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        {user?.mainrole === "Warehouse Manager" ? "Warehouse Order Management" : "Store Orders"}
      </Typography>

      {/* ✅ Show site selection ONLY for Warehouse Managers */}
      {user?.mainrole === "Warehouse Manager" && (
        <Box sx={{ marginBottom: 2 }}>
          <Typography variant="h6">Select a Site</Typography>
          <Select value={selectedSite} onChange={(e) => setSelectedSite(e.target.value)} fullWidth>
            {availableSites.map((site) => (
              <MenuItem key={site.id} value={site.id}>
                {site.siteName}
              </MenuItem>
            ))}
          </Select>
        </Box>
      )}

      {/* View All Orders Section */}
      <OrderHistory user={user} siteId={selectedSite} onSelectOrder={setSelectedOrder} refreshTrigger={refreshTrigger} />

      {/* Store Managers Only: Create/Edit Orders */}
      {user?.mainrole !== "Warehouse Manager" && <OrderForm user={user} selectedOrder={selectedOrder} refreshOrders={refreshOrders} />}
    </Box>
  );
};

export default OrdersDashboard;
