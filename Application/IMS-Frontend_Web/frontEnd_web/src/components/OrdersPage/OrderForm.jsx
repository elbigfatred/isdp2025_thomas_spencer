/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Box, Typography, Button, ToggleButton, ToggleButtonGroup } from "@mui/material";
import OrderItemsManager from "./OrderItemsManager";

const OrderForm = ({ user, selectedOrder, refreshOrders }) => {
  const [activeOrder, setActiveOrder] = useState(null);
  const [checkingActiveOrder, setCheckingActiveOrder] = useState(false);
  const [isEmergencyMode, setIsEmergencyMode] = useState(false); // ✅ NEW: Emergency Order Toggle
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  useEffect(() => {
    if (!user?.site?.id) return;

    setCheckingActiveOrder(true);
    console.log("[DEBUG] Fetching active orders for site ID:", user.site.id);

    axios
      .get(`http://localhost:8080/api/orders/site/${user.site.id}`)
      .then((ordersResponse) => {
        console.log("[DEBUG] All Active Orders:", ordersResponse.data);

        // ✅ Separate STORE and EMERGENCY orders
        const storeOrders = ordersResponse.data.filter((order) => order.txnType?.txnType === "Store Order");
        const emergencyOrders = ordersResponse.data.filter((order) => order.txnType?.txnType === "Emergency Order");

        console.log("[DEBUG] Filtered Store Orders:", storeOrders);
        console.log("[DEBUG] Filtered Emergency Orders:", emergencyOrders);

        // ✅ Select active order based on mode
        const newOrder = isEmergencyMode ? emergencyOrders.find((order) => order.txnStatus?.statusName === "NEW") : storeOrders.find((order) => order.txnStatus?.statusName === "NEW");

        if (newOrder) {
          console.log("[DEBUG] Selected Active Order:", newOrder);
          setActiveOrder(newOrder);
        } else {
          console.log("[DEBUG] No active order found.");
          setActiveOrder(null);
        }
      })
      .catch(() => alert("Failed to check active order"))
      .finally(() => setCheckingActiveOrder(false));
  }, [user, selectedOrder, isEmergencyMode]); // ✅ Refresh when toggling emergency mode

  // ✅ Create Store Order
  const handleCreateStoreOrder = () => {
    console.log("[DEBUG] Creating new Store Order...");

    axios
      .post(`http://localhost:8080/api/orders`, {
        employeeID: user.id,
        siteIDTo: user.site.id,
        siteIDFrom: 1, // Default warehouse
        notes: "New store order created",
      })
      .then((response) => {
        console.log("[DEBUG] Store Order Created:", response.data);
        setActiveOrder(response.data);
        refreshOrders();
      })
      .catch((error) => {
        if (error.response?.status === 409) {
          alert("An active order is ongoing for this store!");
        } else {
          alert("Failed to create order. Please try again.");
        }
      });
  };

  // ✅ Create Emergency Order
  const handleCreateEmergencyOrder = () => {
    console.log("[DEBUG] Creating new Emergency Order...");

    axios
      .post(`http://localhost:8080/api/orders/emergency`, {
        employeeID: user.id,
        siteIDTo: user.site.id,
        siteIDFrom: 1, // Default warehouse
        notes: "New emergency order created",
      })
      .then((response) => {
        console.log("[DEBUG] Emergency Order Created:", response.data);
        setActiveOrder(response.data);
        refreshOrders();
      })
      .catch((error) => {
        if (error.response?.status === 409) {
          alert("An active emergency order is ongoing for this store!");
        } else {
          alert("Failed to create emergency order. Please try again.");
        }
      });
  };

  // ✅ Toggle between Store Order & Emergency Order
  const handleToggleMode = (event, newMode) => {
    if (newMode !== null) {
      console.log("[DEBUG] Switching Order Mode:", newMode);
      setIsEmergencyMode(newMode === "emergency");
      setActiveOrder(null); // Reset active order on switch
    }
  };

  return (
    <Box sx={{ marginTop: 3 }}>
      <Typography variant="h6">Order Editor</Typography>

      {checkingActiveOrder && <Typography>Checking for active orders...</Typography>}

      {/* ✅ Toggle for Order Type */}
      <ToggleButtonGroup value={isEmergencyMode ? "emergency" : "store"} exclusive onChange={handleToggleMode} sx={{ marginBottom: 2 }}>
        <ToggleButton value="store">Store Order</ToggleButton>
        <ToggleButton value="emergency">Emergency Order</ToggleButton>
      </ToggleButtonGroup>

      {/* ✅ Show Create Order Button if no active order */}
      {!activeOrder ? (
        <Button variant="contained" color="primary" onClick={isEmergencyMode ? handleCreateEmergencyOrder : handleCreateStoreOrder}>
          {isEmergencyMode ? "Create Emergency Order" : "Create Store Order"}
        </Button>
      ) : (
        <OrderItemsManager order={activeOrder} setHasUnsavedChanges={setHasUnsavedChanges} refreshOrders={refreshOrders} clearActiveOrder={() => setActiveOrder(null)} />
      )}
    </Box>
  );
};

export default OrderForm;
