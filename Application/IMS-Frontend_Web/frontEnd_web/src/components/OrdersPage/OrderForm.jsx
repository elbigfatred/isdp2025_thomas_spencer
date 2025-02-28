/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import {
  Box,
  Typography,
  Button,
  ToggleButton,
  ToggleButtonGroup,
  IconButton,
  Tooltip,
} from "@mui/material";
import OrderItemsManager from "./OrderItemsManager";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; //  Help Icon

const OrderForm = ({
  user,
  selectedOrder, // ✅ Passed down from parent component
  refreshOrders,
  inventory,
  selectedSite,
}) => {
  const [activeOrder, setActiveOrder] = useState(null);
  const [checkingActiveOrder, setCheckingActiveOrder] = useState(false);
  const [isEmergencyMode, setIsEmergencyMode] = useState(false);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  // ✅ Fetch Active Order when `selectedSite` or `isEmergencyMode` changes
  useEffect(() => {
    if (!selectedSite) return;

    console.log("[DEBUG] Fetching active orders for site ID:", selectedSite);
    setCheckingActiveOrder(true);

    axios
      .get(`http://localhost:8080/api/orders/site/${selectedSite}`)
      .then((ordersResponse) => {
        console.log("[DEBUG] All Active Orders:", ordersResponse.data);

        // ✅ Separate STORE and EMERGENCY orders
        const storeOrders = ordersResponse.data.filter(
          (order) => order.txnType?.txnType === "Store Order"
        );
        const emergencyOrders = ordersResponse.data.filter(
          (order) => order.txnType?.txnType === "Emergency Order"
        );

        console.log("[DEBUG] Filtered Store Orders:", storeOrders);
        console.log("[DEBUG] Filtered Emergency Orders:", emergencyOrders);

        // ✅ Select active order based on mode
        const newOrder = isEmergencyMode
          ? emergencyOrders.find(
              (order) => order.txnStatus?.statusName === "NEW"
            )
          : storeOrders.find((order) => order.txnStatus?.statusName === "NEW");

        if (newOrder) {
          console.log("[DEBUG] Selected Active Order:", newOrder);
          setActiveOrder(newOrder);
        } else {
          console.log(
            "[DEBUG] No active order found. Setting activeOrder to null."
          );
          setActiveOrder(null);
        }
      })
      .catch(() => {
        console.log(
          "[ERROR] Failed to check active order. Setting activeOrder to null."
        );
        setActiveOrder(null); // ✅ Ensure we reset activeOrder if API call fails
      })
      .finally(() => setCheckingActiveOrder(false));
  }, [selectedSite, isEmergencyMode, refreshOrders]); // ✅ Trigger refresh when orders are refreshed

  // ✅ Keep `activeOrder` in sync with `selectedOrder`
  useEffect(() => {
    console.log("[DEBUG] Selected Order Changed:", selectedOrder);
    if (selectedOrder) {
      setActiveOrder(selectedOrder);
    }
  }, [selectedOrder]);

  // ✅ Create Store Order
  const handleCreateStoreOrder = () => {
    console.log("[DEBUG] Creating new Store Order...");

    axios
      .post(`http://localhost:8080/api/orders`, {
        employeeID: user.id,
        siteIDTo: selectedSite, // ✅ Use selected site
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
        siteIDTo: selectedSite, // ✅ Use selected site
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
      setActiveOrder(null); // ✅ Reset active order on switch
    }
  };

  return (
    <Box sx={{ marginTop: 3 }}>
      <Box display="flex" alignItems="center" gap={0} mb={1}>
        <Typography variant="h6">Order Editor</Typography>
        <Tooltip
          title="Here you can create and manage store orders and emergency orders for your store.
          Toggle between Store Order and Emergency Order to create different types of orders.
          Orders will automatically be shown if there is an active order for the selected store."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Box>
      {checkingActiveOrder && (
        <Typography>Checking for active orders...</Typography>
      )}

      {/* ✅ Toggle for Order Type */}
      <ToggleButtonGroup
        value={isEmergencyMode ? "emergency" : "store"}
        exclusive
        onChange={handleToggleMode}
        sx={{ marginBottom: 2 }}
      >
        <ToggleButton value="store">Store Order</ToggleButton>
        <ToggleButton value="emergency">Emergency Order</ToggleButton>
      </ToggleButtonGroup>

      {/* ✅ Show Create Order Button if no active order */}
      {!activeOrder ? (
        <Button
          variant="contained"
          color="primary"
          sx={{ marginLeft: 2 }}
          onClick={
            isEmergencyMode
              ? handleCreateEmergencyOrder
              : handleCreateStoreOrder
          }
        >
          {isEmergencyMode ? "Create Emergency Order" : "Create Store Order"}
        </Button>
      ) : (
        <OrderItemsManager
          user={user}
          order={activeOrder}
          setHasUnsavedChanges={setHasUnsavedChanges}
          refreshOrders={refreshOrders}
          clearActiveOrder={() => setActiveOrder(null)}
          inventory={inventory}
        />
      )}
    </Box>
  );
};

export default OrderForm;
