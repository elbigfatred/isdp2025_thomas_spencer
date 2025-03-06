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
      .then((data) =>
        setSites(
          data.filter(
            (site) =>
              site.active && site.siteName.toLowerCase().includes("retail")
          )
        )
      )
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

  const handleConfirmOrder = async (customerInfo) => {
    if (!user || !activeSite || cart.length === 0) {
      console.error("Invalid order submission: Missing required data.");
      return;
    }

    const orderPayload = {
      customer: {
        name: customerInfo.name,
        phone: customerInfo.phone,
        email: customerInfo.email,
      },
      items: cart.map((item) => ({
        itemID: item.id.itemID,
        quantity: item.quantity,
      })),
      createdByUserID: user.id,
      siteID: activeSite.id,
    };

    console.log("Submitting Order Payload:", orderPayload);

    try {
      const response = await fetch(
        "http://localhost:8080/api/orders/submitOnlineOrder",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(orderPayload),
        }
      );

      const text = await response.text();
      let data;
      try {
        data = JSON.parse(text);
      } catch {
        data = { message: text };
      }

      if (!response.ok) {
        throw new Error(data.message || "Failed to submit order");
      }

      console.log("Order submitted successfully:", data);

      // ✅ Determine Pickup Time and Day
      const now = new Date();
      const orderHour = now.getHours();

      let pickupDate;
      let pickupDay = "today";

      if (orderHour >= 17) {
        // After 5:00 PM → Ready at 9:00 AM tomorrow
        pickupDate = new Date(now);
        pickupDate.setDate(now.getDate() + 1);
        pickupDate.setHours(9, 0, 0);
        pickupDay = "tomorrow";
      } else if (orderHour < 9) {
        // Before 9:00 AM → Ready at 11:00 AM today
        pickupDate = new Date(now);
        pickupDate.setHours(11, 0, 0);
      } else {
        // Otherwise → Ready exactly 2 hours from now
        pickupDate = new Date(now.getTime() + 2 * 60 * 60 * 1000);
      }

      // ✅ Format Time in 12-hour AM/PM format
      const pickupHour = pickupDate.getHours();
      const pickupMinutes = pickupDate.getMinutes().toString().padStart(2, "0");
      const period = pickupHour >= 12 ? "PM" : "AM";
      const formattedHour = ((pickupHour + 11) % 12) + 1; // Convert 24-hour to 12-hour

      const pickupTime = `${formattedHour}:${pickupMinutes} ${period}`;

      // ✅ Show confirmation message with "today" or "tomorrow"
      window.alert(
        `Order placed successfully! Your order will be ready for pickup at ${pickupTime} ${pickupDay}.\n\nOrder ID: ${
          data.orderID || "N/A"
        }`
      );

      // ✅ Optionally clear the cart after successful order
      setCart([]);
      setOrderModalOpen(false);
      setActiveSite(null);
    } catch (error) {
      console.error("Error submitting order:", error);

      // ✅ Show error message
      window.alert(
        "There was an error processing your order. Please try again later."
      );
    }
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
              {site.siteName} - {site.address}, {site.city},{" "}
              {site.province?.provinceID}
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
                color="error"
                sx={{ marginTop: 2 }}
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
        onConfirm={handleConfirmOrder}
        cart={cart}
      />
    </Box>
  );
};

export default OnlineOrderManagerPanel;
