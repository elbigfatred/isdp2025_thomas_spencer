import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Modal,
  Button,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Paper,
} from "@mui/material";

const OrderDetailsModal = ({ open, onClose, order }) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!order) return;

    setLoading(true);
    fetch(`http://localhost:8080/api/orders/${order.id}/items`)
      .then((res) => res.json())
      .then((data) => {
        setItems(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching order items:", err);
        setLoading(false);
      });
  }, [order]);

  if (!order) return null;

  return (
    <Modal open={open} onClose={onClose}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 500,
          bgcolor: "background.paper",
          boxShadow: 24,
          p: 4,
          borderRadius: 2,
        }}
      >
        <Typography variant="h6">Order Details</Typography>
        <Typography>
          <strong>Order ID:</strong> {order.id}
        </Typography>
        <Typography>
          <strong>Pickup Location:</strong> {order.siteIDTo.siteName},{" "}
          {order.siteIDTo.address}, {order.siteIDTo.city}
        </Typography>
        <Typography>
          <strong>Pickup:</strong>{" "}
          {new Date(order.shipDate).toLocaleString("en-US", {
            weekday: "long",
            month: "long",
            day: "numeric",
            year: "numeric",
          })}
        </Typography>
        <Typography>
          <strong>Status:</strong> {order.txnStatus.statusName}
        </Typography>

        {loading ? (
          <CircularProgress sx={{ marginTop: 2 }} />
        ) : (
          <Paper sx={{ maxHeight: 200, overflowY: "auto", marginTop: 2 }}>
            <List>
              {items.map((item) => (
                <ListItem key={item.id.itemID} divider>
                  <ListItemText
                    primary={item.itemID.name}
                    secondary={`SKU: ${item.itemID.sku} | Quantity: ${item.quantity} | Price: $${item.itemID.retailPrice}`}
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        <Box sx={{ display: "flex", justifyContent: "flex-end", marginTop: 2 }}>
          <Button variant="contained" onClick={onClose}>
            Close
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default OrderDetailsModal;
