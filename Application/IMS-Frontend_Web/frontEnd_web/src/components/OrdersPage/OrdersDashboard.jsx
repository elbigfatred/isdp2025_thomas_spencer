/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Select,
  MenuItem,
  Button,
  Stack,
} from "@mui/material";
import OrderHistory from "./OrderHistory"; // View orders
import OrderForm from "./OrderForm"; // Create/edit orders
import axios from "axios";

const OrdersDashboard = ({ user }) => {
  const isAdminOrWHMgr =
    user?.mainrole === "Warehouse Manager" ||
    user?.mainrole === "Administrator";

  const [selectedOrder, setSelectedOrder] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [selectedSite, setSelectedSite] = useState(
    isAdminOrWHMgr ? "ALL" : user?.site?.id
  );
  const [availableSites, setAvailableSites] = useState([]);
  const [inventory, setInventory] = useState([]);

  useEffect(() => {
    if (isAdminOrWHMgr) {
      axios
        .get("http://localhost:8080/api/sites")
        .then((response) => {
          const activeSites = response.data.filter((site) => site.active);
          setAvailableSites(activeSites);
          console.log("[DEBUG] Loaded active sites:", activeSites);
        })
        .catch(() => console.error("[ERROR] Failed to load sites"));
    }
  }, [user]);

  useEffect(() => {
    if (!selectedSite) return;

    if (selectedSite === "ALL") {
      console.log("[DEBUG] Fetching all orders across all sites...");
    } else {
      console.log("[DEBUG] Loading inventory for site:", selectedSite);
      axios
        .get(`http://localhost:8080/api/inventory/site/${selectedSite}`)
        .then((response) => setInventory(response.data))
        .catch(() => console.error("[ERROR] Failed to load inventory"));
    }
  }, [selectedSite]);

  const refreshOrders = () => {
    console.log("[DEBUG] Refreshing dashboard...");
    setRefreshTrigger((prev) => prev + 1);
    setSelectedOrder(null);
  };

  const handleSiteChange = (event) => {
    const newSite = event.target.value;
    setSelectedSite(newSite);
    refreshOrders();
  };

  return (
    <Box>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        sx={{ marginBottom: 2 }}
      >
        <Typography variant="h4">
          {isAdminOrWHMgr ? "Warehouse Order Management" : "Store Orders"}
        </Typography>

        <Button variant="contained" color="primary" onClick={refreshOrders}>
          Refresh Dashboard
        </Button>
      </Stack>

      {isAdminOrWHMgr && (
        <Box sx={{ marginBottom: 2 }}>
          <Typography variant="h6">Select a Site</Typography>
          <Select value={selectedSite} onChange={handleSiteChange} fullWidth>
            <MenuItem value="ALL">All Sites</MenuItem>
            {availableSites.map((site) => (
              <MenuItem key={site.id} value={site.id}>
                {site.siteName}
              </MenuItem>
            ))}
          </Select>
        </Box>
      )}

      {/* ✅ Display Order History for selected site or ALL */}
      <OrderHistory
        user={user}
        siteId={selectedSite === "ALL" ? null : selectedSite}
        refreshTrigger={refreshTrigger}
        fetchAllOrders={selectedSite === "ALL"}
      />

      {/* ✅ Disable Order Form if "ALL" is selected */}
      {selectedSite !== "ALL" && (
        <OrderForm
          user={user}
          selectedOrder={selectedOrder}
          refreshOrders={refreshOrders}
          inventory={inventory}
          selectedSite={selectedSite}
        />
      )}
    </Box>
  );
};

export default OrdersDashboard;
