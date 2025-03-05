import React, { useState } from "react";
import {
  Box,
  Typography,
  Button,
  Modal,
  TextField,
  List,
  ListItem,
  ListItemText,
} from "@mui/material";

const CustomerConfirmOrderModal = ({
  open,
  onClose,
  subtotal,
  tax,
  grandTotal,
  onConfirm,
  cart,
}) => {
  // Customer Details State
  const [customerInfo, setCustomerInfo] = useState({
    name: "",
    phone: "",
    email: "",
  });

  // Handles Input Change
  const handleChange = (e) => {
    setCustomerInfo({ ...customerInfo, [e.target.name]: e.target.value });
  };

  // Checks if form is valid
  const isFormValid =
    customerInfo.name.trim() !== "" &&
    customerInfo.phone.trim() !== "" &&
    customerInfo.email.trim() !== "";

  return (
    <Modal open={open} onClose={onClose}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 450,
          bgcolor: "background.paper",
          boxShadow: 24,
          p: 4,
          borderRadius: 2,
        }}
      >
        <Typography variant="h6" gutterBottom>
          Confirm Order
        </Typography>

        {/* 📌 Order Item List */}
        <List sx={{ maxHeight: 200, overflowY: "auto", marginBottom: 2 }}>
          {cart.map((item) => (
            <ListItem key={item.id.itemID} divider>
              <ListItemText
                primary={item.item.name}
                secondary={`Quantity: ${item.quantity} | Price: $${(
                  item.item.retailPrice * item.quantity
                ).toFixed(2)}`}
              />
            </ListItem>
          ))}
        </List>

        {/* 📌 Customer Info Inputs */}
        <TextField
          fullWidth
          label="Full Name"
          name="name"
          variant="outlined"
          size="small"
          sx={{ marginBottom: 2 }}
          onChange={handleChange}
          value={customerInfo.name}
        />
        <TextField
          fullWidth
          label="Phone Number"
          name="phone"
          variant="outlined"
          size="small"
          sx={{ marginBottom: 2 }}
          onChange={handleChange}
          value={customerInfo.phone}
        />
        <TextField
          fullWidth
          label="Email Address"
          name="email"
          type="email"
          variant="outlined"
          size="small"
          sx={{ marginBottom: 2 }}
          onChange={handleChange}
          value={customerInfo.email}
        />

        {/* 📌 Order Total */}
        <Typography variant="body1">
          Subtotal: <strong>${subtotal.toFixed(2)}</strong>
        </Typography>
        <Typography variant="body1">
          Tax (15%): <strong>${tax.toFixed(2)}</strong>
        </Typography>
        <Typography variant="h6">
          Total: <strong>${grandTotal.toFixed(2)}</strong>
        </Typography>

        {/* 📌 Buttons */}
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            marginTop: 2,
          }}
        >
          <Button variant="outlined" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={() => onConfirm(customerInfo)}
            disabled={!isFormValid} // Prevents confirming if fields are empty
          >
            Place Order
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default CustomerConfirmOrderModal;
