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

  const [errors, setErrors] = useState({
    name: "",
    phone: "",
    email: "",
  });

  // Regex Patterns
  const nameRegex = /^[A-Za-z\s]{2,50}$/; // Only letters and spaces, 2-50 chars
  const phoneRegex = /^(?:\d{3}-\d{3}-\d{4}|\d{10})$/; // Accepts "123-456-7890" or "1234567890"
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Standard email format

  // Handles Input Change & Validation
  const handleChange = (e) => {
    const { name, value } = e.target;
    setCustomerInfo({ ...customerInfo, [name]: value });

    // Validate Fields
    let errorMsg = "";
    if (name === "name" && !nameRegex.test(value)) {
      errorMsg = "Enter a valid full name (letters only, 2-50 chars).";
    } else if (name === "phone" && !phoneRegex.test(value)) {
      errorMsg = "Enter a valid phone number (e.g., 123-456-7890).";
    } else if (name === "email" && !emailRegex.test(value)) {
      errorMsg = "Enter a valid email address.";
    }

    setErrors({ ...errors, [name]: errorMsg });
  };

  // Checks if form is valid
  const isFormValid =
    customerInfo.name.trim() !== "" &&
    customerInfo.phone.trim() !== "" &&
    customerInfo.email.trim() !== "" &&
    !errors.name &&
    !errors.phone &&
    !errors.email;

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
          error={!!errors.name}
          helperText={errors.name}
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
          error={!!errors.phone}
          helperText={errors.phone}
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
          error={!!errors.email}
          helperText={errors.email}
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
            disabled={!isFormValid} // Prevents confirming if invalid
          >
            Place Order
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default CustomerConfirmOrderModal;
