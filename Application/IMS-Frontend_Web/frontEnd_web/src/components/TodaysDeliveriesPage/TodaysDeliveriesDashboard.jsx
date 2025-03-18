import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  CircularProgress,
  Paper,
  Button,
  Tooltip,
  IconButton,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon
import PickupStoreOrders from "./PickupStoreOrders";
import DeliverStoreOrders from "./DeliverStoreOrders";

const TodaysDeliveriesDashboard = ({ user }) => {
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  // ✅ Fetch today's deliveries
  const fetchTodaysOrders = () => {
    setLoading(true);
    fetch("http://localhost:8080/api/delivery/shipping-today")
      .then((res) => res.json())
      .then((data) => {
        setOrders(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("[ERROR] Fetching today's deliveries:", err);
        setError("Failed to load today's deliveries.");
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchTodaysOrders();
  }, []);

  // ✅ Function to refresh orders after pickup
  const onConfirmPickup = () => {
    console.log("[DEBUG] Refreshing orders after pickup...");
    fetchTodaysOrders(); // Re-fetch orders to update the UI
  };

  // ✅ Filter orders by status
  const readyForPickup = orders.filter(
    (order) => order.txnStatus.statusName === "ASSEMBLED"
  );
  const inTransitOrders = orders.filter(
    (order) => order.txnStatus.statusName === "IN TRANSIT"
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Today's Deliveries
        <Tooltip title="Confirm pickup and delivery of today's orders" arrow>
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>{" "}
      </Typography>

      {loading ? (
        <CircularProgress />
      ) : error ? (
        <Typography color="error">{error}</Typography>
      ) : (
        <>
          {/* ✅ Only show Pickup section to authorized roles */}
          {["Administrator", "Delivery", "Warehouse Manager"].includes(
            user.mainrole
          ) && (
            <Paper sx={{ padding: 3, marginBottom: 3 }}>
              <PickupStoreOrders
                orders={readyForPickup}
                user={user}
                onRefresh={onConfirmPickup}
              />
            </Paper>
          )}

          {/* ✅ Only show Delivery section to authorized roles */}
          {[
            "Administrator",
            "Delivery",
            "Store Worker",
            "Store Manager",
          ].includes(user.mainrole) && (
            <Paper sx={{ padding: 3 }}>
              <DeliverStoreOrders
                orders={inTransitOrders}
                user={user}
                onRefresh={onConfirmPickup}
              />
            </Paper>
          )}
        </>
      )}
    </Box>
  );
};

export default TodaysDeliveriesDashboard;
