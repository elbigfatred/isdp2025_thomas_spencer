/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Box, Typography, Button, Alert } from "@mui/material";
import InventoryList from "./InventoryList";
import OrderItemsList from "./OrderItemsList";

const OrderItemsManager = ({ order, refreshOrders, clearActiveOrder }) => {
  const [orderItems, setOrderItems] = useState([]);
  const [availableItems, setAvailableItems] = useState([]);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!order?.id) return;

    setLoading(true);

    // ✅ Fetch order items
    axios
      .get(`http://localhost:8080/api/orders/${order.id}/items`)
      .then((response) => {
        console.log("[DEBUG] Fetched order items:", response.data);
        setOrderItems(response.data);
      })
      .catch(() => setError("Failed to load order items"));

    // ✅ Fetch available inventory
    axios
      .get(`http://localhost:8080/api/inventory/site/${order.siteIDTo.id}`)
      .then((response) => {
        console.log("[DEBUG] Fetched store inventory:", response.data);
        setAvailableItems(response.data);
      })
      .catch(() => setError("Failed to load available items"))
      .finally(() => setLoading(false));
  }, [order]);

  // ✅ Handle quantity changes (increments by case size)
  const handleQuantityChange = (index, newQuantity) => {
    setOrderItems((prevItems) => {
      const updatedItems = [...prevItems];
      const caseSize = updatedItems[index].itemID.caseSize || 1;
      updatedItems[index].quantity = Math.ceil(newQuantity / caseSize) * caseSize;
      setHasUnsavedChanges(true);
      return updatedItems;
    });
  };

  // ✅ Handle adding an item
  const handleAddItem = (item) => {
    setOrderItems((prevItems) => {
      const currentItems = prevItems || []; // ✅ Ensure prevItems is always an array

      if (currentItems.some((i) => i.itemID.id === item.id)) {
        alert("Item already in order.");
        return currentItems; // Prevent duplicates
      }

      setHasUnsavedChanges(true);

      return [...currentItems, { txnID: order.id, itemID: item, quantity: item.caseSize || 1 }];
    });
  };

  // ✅ Handle removing an item
  const handleRemoveItem = (index) => {
    setOrderItems((prevItems) => prevItems.filter((_, i) => i !== index));
    setHasUnsavedChanges(true);
  };

  // ✅ Handle saving order changes
  const handleSaveChanges = () => {
    const savePayload = {
      txnID: order.id,
      items: orderItems.map((item) => ({
        itemID: item.itemID.id,
        quantity: item.quantity,
      })),
    };

    console.log("[DEBUG] Saving Order Changes:", savePayload);

    axios
      .put(`http://localhost:8080/api/orders/${order.id}/update-items`, savePayload)
      .then((response) => {
        console.log("[DEBUG] Save Success, Response:", response.data);
        setHasUnsavedChanges(false);
        alert("Order saved successfully!");
        refreshOrders();
      })
      .catch((error) => {
        console.error("[ERROR] Failed to update order:", error);
        alert("Failed to update order");
      });
  };

  // ✅ Handle submitting order
  const handleSubmitOrder = () => {
    console.log(`[DEBUG] Submitting order ID: ${order.id}`);

    axios
      .put(`http://localhost:8080/api/orders/${order.id}/submit`)
      .then(() => {
        alert("Order submitted successfully!");
        refreshOrders(); // Refresh orders list
        clearActiveOrder();
      })
      .catch(() => alert("Failed to submit order"));
  };

  return (
    <Box sx={{ marginTop: 3 }}>
      <Typography variant="h6">Edit Order Items</Typography>

      {error && <Typography color="error">{error}</Typography>}
      {loading && <Typography>Loading...</Typography>}

      {/* ✅ Show warning banner if there are unsaved changes */}
      {hasUnsavedChanges && (
        <Alert severity="warning" sx={{ marginBottom: 2 }}>
          You have unsaved changes! Be sure to save before submitting.
        </Alert>
      )}

      <Box sx={{ display: "flex", gap: 2, marginTop: 3 }}>
        <InventoryList availableItems={availableItems} onAddItem={handleAddItem} />
        <OrderItemsList orderItems={orderItems} onQuantityChange={handleQuantityChange} onRemoveItem={handleRemoveItem} />
      </Box>

      {/* ✅ Buttons for Saving and Submitting */}
      <Box sx={{ marginTop: 2, display: "flex", gap: 2 }}>
        <Button variant="contained" color="primary" onClick={handleSaveChanges} disabled={orderItems.length === 0 || !hasUnsavedChanges}>
          Save Changes
        </Button>

        <Button variant="contained" color="secondary" onClick={handleSubmitOrder} disabled={orderItems.length === 0 || hasUnsavedChanges}>
          Submit Order
        </Button>
      </Box>
    </Box>
  );
};

export default OrderItemsManager;
