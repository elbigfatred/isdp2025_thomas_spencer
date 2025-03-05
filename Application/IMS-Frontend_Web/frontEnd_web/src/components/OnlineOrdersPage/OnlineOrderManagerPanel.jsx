import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Button,
  Paper,
} from "@mui/material";
import InventoryList from "./InventoryList";
import UserCart from "./UserCart";
import CustomerConfirmOrderModal from "./CustomerConfirmOrderModal.jsx"; // ✅ Import modal

const TAX_RATE = 0.15;

const OnlineOrderManagerPanel = ({ user }) => {
  const [sites, setSites] = useState([]);
  const [activeSite, setActiveSite] = useState(null);
  const [cart, setCart] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [orderModalOpen, setOrderModalOpen] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/sites")
      .then((res) => res.json())
      .then((data) => setSites(data))
      .catch((err) => console.error("Error fetching sites:", err));
  }, []);

  useEffect(() => {
    if (!activeSite) return;
    fetch(`http://localhost:8080/api/inventory/site/${activeSite.id}`)
      .then((res) => res.json())
      .then((data) => setInventory(data))
      .catch((err) => console.error("Error fetching inventory:", err));
  }, [activeSite]);

  const handleSiteChange = (event) => {
    const selectedSite = event.target.value;
    if (
      cart.length > 0 &&
      !window.confirm("Switching sites will clear your cart. Continue?")
    )
      return;
    setActiveSite(selectedSite);
    setCart([]);
    setInventory([]);
  };

  const addToCart = (item) => {
    setCart((prevCart) => {
      const existingItem = prevCart.find(
        (cartItem) => cartItem.id.itemID === item.id.itemID
      );

      if (existingItem) {
        return prevCart.map((cartItem) =>
          cartItem.id.itemID === item.id.itemID
            ? {
                ...cartItem,
                quantity: Math.min(cartItem.quantity + 1, item.quantity),
              }
            : cartItem
        );
      } else {
        return [...prevCart, { ...item, quantity: 1 }];
      }
    });
  };

  const updateCart = (item, newQuantity) => {
    setCart((prevCart) =>
      prevCart.map((cartItem) =>
        cartItem.id.itemID === item.id.itemID
          ? {
              ...cartItem,
              quantity: Math.max(
                1,
                Math.min(
                  newQuantity,
                  inventory.find((i) => i.id.itemID === item.id.itemID)
                    ?.quantity || 0
                )
              ),
            }
          : cartItem
      )
    );
  };

  const removeFromCart = (item) => {
    setCart((prevCart) =>
      prevCart.filter((cartItem) => cartItem.id.itemID !== item.id.itemID)
    );
  };

  // **🛒 Calculate Order Totals**
  const subtotal = cart.reduce(
    (sum, item) => sum + item.item.retailPrice * item.quantity,
    0
  );
  const tax = subtotal * TAX_RATE;
  const grandTotal = subtotal + tax;

  const handleOrderConfirm = () => {
    console.log("Order placed!");
    setOrderModalOpen(false);
    setCart([]); // Clear cart after order
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Create an Online Order
      </Typography>

      {/* Site Selection */}
      <FormControl fullWidth sx={{ marginBottom: 2 }}>
        <InputLabel>Select a Store</InputLabel>
        <Select value={activeSite || ""} onChange={handleSiteChange}>
          {sites.map((site) => (
            <MenuItem key={site.id} value={site}>
              {site.siteName} - {site.city}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Hide Inventory & Cart if no site is selected */}
      {activeSite && (
        <>
          <Grid container spacing={2}>
            <Grid item xs={7}>
              <InventoryList
                activeSite={activeSite}
                inventory={inventory}
                addToCart={addToCart}
              />
            </Grid>
            <Grid item xs={5}>
              <UserCart
                cart={cart}
                updateCart={updateCart}
                removeFromCart={removeFromCart}
                inventory={inventory}
              />
            </Grid>
          </Grid>

          {/* Order Summary - Positioned BELOW Both Inventory & Cart */}
          <Box sx={{ marginTop: 2 }}>
            <Paper sx={{ padding: 2 }}>
              <Typography variant="body1">
                Subtotal: ${subtotal.toFixed(2)}
              </Typography>
              <Typography variant="body1">
                Tax (15%): ${tax.toFixed(2)}
              </Typography>
              <Typography variant="h6">
                Total: ${grandTotal.toFixed(2)}
              </Typography>

              <Button
                variant="contained"
                color="primary"
                sx={{ marginTop: 2 }}
                fullWidth
                disabled={cart.length === 0}
                onClick={() => setOrderModalOpen(true)}
              >
                Complete Order
              </Button>
            </Paper>
          </Box>
        </>
      )}

      {/* Order Confirmation Modal */}
      <CustomerConfirmOrderModal
        open={orderModalOpen}
        onClose={() => setOrderModalOpen(false)}
        subtotal={subtotal}
        tax={tax}
        grandTotal={grandTotal}
        onConfirm={handleOrderConfirm}
        cart={cart}
      />
    </Box>
  );
};

export default OnlineOrderManagerPanel;
