/* eslint-disable react/prop-types */
import { useState } from "react";
import { Box, Typography } from "@mui/material";
import OrdersList from "./OrdersList"; // Component for viewing orders
import OrderEditor from "./OrderEditor"; // Component for creating/editing orders

const OrdersPage = ({ user }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);

  return (
    <Box>
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        Store Orders
      </Typography>

      {/* View All Orders Section */}
      <OrdersList user={user} onSelectOrder={setSelectedOrder} />

      {/* Create or Edit Order Section */}
      <OrderEditor user={user} selectedOrder={selectedOrder} />
    </Box>
  );
};

export default OrdersPage;
