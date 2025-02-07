/* eslint-disable react/prop-types */
import { useState } from "react";
import { Box, Typography } from "@mui/material";
import OrderHistory from "./OrderHistory"; // Component for viewing orders
import OrderForm from "./OrderForm"; // Component for creating/editing orders

const OrdersDashboard = ({ user }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0); // Used to trigger refresh

  // Function to refresh the orders list
  const refreshOrders = () => {
    setRefreshTrigger((prev) => prev + 1); // Increment to trigger useEffect in OrdersList
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        Store Orders
      </Typography>

      {/* View All Orders Section */}
      <OrderHistory user={user} onSelectOrder={setSelectedOrder} refreshTrigger={refreshTrigger} />

      {/* Create or Edit Order Section */}
      <OrderForm user={user} selectedOrder={selectedOrder} refreshOrders={refreshOrders} />
    </Box>
  );
};

export default OrdersDashboard;
