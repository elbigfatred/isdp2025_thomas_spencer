import { useState } from "react";
import { Box, Typography, Paper, IconButton, TextField } from "@mui/material";
import RemoveCircleOutlineIcon from "@mui/icons-material/RemoveCircleOutline";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import DeleteIcon from "@mui/icons-material/Delete";

const UserCart = ({ cart, updateCart, removeFromCart, inventory }) => {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredCart = cart.filter((item) =>
    item.item.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Shopping Cart
      </Typography>

      <TextField
        fullWidth
        label="Search Cart..."
        variant="outlined"
        size="small"
        sx={{ marginBottom: 2 }}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {cart.length === 0 ? (
        <Typography>No items in the cart.</Typography>
      ) : (
        <Paper sx={{ padding: 2, maxHeight: "600px", overflowY: "auto" }}>
          {filteredCart.map((cartItem) => {
            const availableStock =
              inventory.find((i) => i.id.itemID === cartItem.id.itemID)
                ?.quantity || 0;

            return (
              <Box
                key={cartItem.id.itemID}
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "8px 0",
                  borderBottom: "1px solid rgba(255,255,255,0.1)",
                }}
              >
                {/* 📌 Updated Cart Item Layout */}
                <Typography sx={{ flex: 1 }}>
                  <strong>{cartItem.item.name}</strong> |{" "}
                  <span style={{ fontWeight: "bold" }}>QUANTITY:</span>{" "}
                  {cartItem.quantity} |{" "}
                  <span style={{ fontWeight: "bold" }}>PRICE:</span> $
                  {cartItem.item.retailPrice.toFixed(2)}
                </Typography>

                {/* ✅ Controls */}
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                  <IconButton
                    onClick={() => updateCart(cartItem, cartItem.quantity - 1)}
                    disabled={cartItem.quantity === 1}
                  >
                    <RemoveCircleOutlineIcon />
                  </IconButton>
                  <IconButton
                    onClick={() => updateCart(cartItem, cartItem.quantity + 1)}
                    disabled={cartItem.quantity >= availableStock} // ✅ Prevent exceeding stock
                  >
                    <AddCircleOutlineIcon />
                  </IconButton>
                  <IconButton onClick={() => removeFromCart(cartItem)}>
                    <DeleteIcon />
                  </IconButton>
                </Box>
              </Box>
            );
          })}
        </Paper>
      )}
    </Box>
  );
};

export default UserCart;
