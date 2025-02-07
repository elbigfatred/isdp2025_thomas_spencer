/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, TableContainer, Table, TableHead, TableRow, TableCell, TableBody, Paper } from "@mui/material";
import OrderItemTable from "./OrderItemsTable"; // New component for order items

const OrderDetailsModal = ({ open, onClose, order }) => {
  const [orderItems, setOrderItems] = useState([]);

  useEffect(() => {
    if (!order || !open) return;

    console.log(`[DEBUG] Fetching items for txnID: ${order.id}`);
    axios
      .get(`http://localhost:8080/api/orders/${order.id}/items`)
      .then((response) => {
        console.log("[DEBUG] Fetched Order Items:", response.data);
        setOrderItems(response.data);
      })
      .catch((error) => console.error("[ERROR] Failed to fetch order items:", error));
  }, [order, open]);

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Order Details (ID: {order?.id})</DialogTitle>
      <DialogContent>
        <Typography>
          <strong>Store:</strong> {order?.siteIDTo?.siteName}
        </Typography>
        <Typography>
          <strong>Status:</strong> {order?.txnStatus?.statusName}
        </Typography>
        <Typography>
          <strong>Ship Date:</strong> {new Date(order?.shipDate).toLocaleDateString()}
        </Typography>
        <Typography sx={{ marginTop: 2 }}>
          <strong>Items:</strong>
        </Typography>

        {/* Order Items Table */}
        <OrderItemTable items={orderItems} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default OrderDetailsModal;
