/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Box, Typography, Button } from "@mui/material";
import OrderItemsManager from "./OrderItemsManager";

const OrderForm = ({ user, selectedOrder, refreshOrders }) => {
  const [activeOrder, setActiveOrder] = useState(null);
  const [checkingActiveOrder, setCheckingActiveOrder] = useState(false);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  useEffect(() => {
    if (!user?.site?.id) return;

    setCheckingActiveOrder(true);
    axios
      .get(`http://localhost:8080/api/orders/check-active?siteId=${user.site.id}`)
      .then((response) => {
        if (response.data) {
          axios.get(`http://localhost:8080/api/orders/site/${user.site.id}`).then((ordersResponse) => {
            const newOrder = ordersResponse.data.find((order) => order.txnStatus?.statusName === "NEW");
            if (newOrder) {
              console.log("[DEBUG] Active Order fetched:", newOrder);
              setActiveOrder(newOrder);
            }
          });
        }
      })
      .catch(() => alert("Failed to check active order"))
      .finally(() => setCheckingActiveOrder(false));
  }, [user, selectedOrder]);

  const handleCreateOrder = () => {
    axios
      .post(`http://localhost:8080/api/orders`, {
        employeeID: user.id,
        siteIDTo: user.site.id,
        siteIDFrom: 1, // Default warehouse
        notes: "New order created",
      })
      .then((response) => {
        setActiveOrder(response.data);
        refreshOrders();
      })
      .catch((error) => {
        if (error.response && error.response.status === 409) {
          alert("An active order is ongoing for this store!");
        } else {
          alert("Failed to create order. Please try again.");
        }
      });
  };

  const clearActiveOrder = () => {
    console.log("[DEBUG] Clearing active order.");
    setActiveOrder(null);
  };

  return (
    <Box sx={{ marginTop: 3 }}>
      <Typography variant="h6">Order Editor</Typography>

      {checkingActiveOrder && <Typography>Checking for active orders...</Typography>}

      {!activeOrder ? (
        <Button variant="contained" color="primary" onClick={handleCreateOrder}>
          Create New Order
        </Button>
      ) : (
        <OrderItemsManager order={activeOrder} setHasUnsavedChanges={setHasUnsavedChanges} refreshOrders={refreshOrders} clearActiveOrder={clearActiveOrder} />
      )}
    </Box>
  );
};

export default OrderForm;
