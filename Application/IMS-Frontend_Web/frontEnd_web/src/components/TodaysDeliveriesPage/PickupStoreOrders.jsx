import { useState, useMemo } from "react";
import {
  Box,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Button,
  Paper,
  IconButton,
  Tooltip,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon

import OrderDetailsModal from "./OrderDetailsModal";
import BeginPickupModal from "./BeginPickupModal"; // ✅ Import modal

const PickupStoreOrders = ({ orders, user, onRefresh }) => {
  const [loading, setLoading] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null); // ✅ Track selected order for details modal
  const [pickupTxnIds, setPickupTxnIds] = useState([]); // ✅ Track Txns for pickup modal

  // ✅ Format today's date for title
  const today = new Date().toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });

  // ✅ Group orders by delivery vehicle
  const groupedOrders = useMemo(() => {
    const groups = {};
    orders.forEach((order) => {
      const vehicleType = order.deliveryID.vehicle.vehicleType;
      if (!groups[vehicleType]) {
        groups[vehicleType] = { txnIds: [], orders: [] };
      }
      groups[vehicleType].txnIds.push(order.id);
      groups[vehicleType].orders.push(order);
    });
    return groups;
  }, [orders]);

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Pickup Store Orders - {today}
        <Tooltip
          title="Confirm today's pickup(s) and update order status."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>{" "}
      </Typography>
      <Paper sx={{ padding: 2 }}>
        {Object.keys(groupedOrders).length === 0 ? (
          <Typography>No orders ready for pickup.</Typography>
        ) : (
          Object.entries(groupedOrders).map(
            ([vehicleType, { txnIds, orders }]) => (
              <Box key={vehicleType} sx={{ marginBottom: 3 }}>
                <Typography variant="h6">Vehicle: {vehicleType}</Typography>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Order ID</TableCell>
                      <TableCell>Destination</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {orders.map((order) => (
                      <TableRow key={order.id}>
                        <TableCell>{order.id}</TableCell>
                        <TableCell>{order.siteIDTo.siteName}</TableCell>
                        <TableCell>
                          <Button
                            variant="outlined"
                            color="primary"
                            sx={{ marginRight: 1 }}
                            onClick={() => setSelectedOrder(order)}
                          >
                            View Details
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                <Button
                  variant="contained"
                  color="primary"
                  disabled={loading}
                  onClick={() => setPickupTxnIds(txnIds)} // ✅ Open pickup modal
                  sx={{ marginTop: 2 }}
                >
                  BEGIN PICKUP
                </Button>
              </Box>
            )
          )
        )}
      </Paper>

      {/* ✅ Order Details Modal */}
      {selectedOrder && (
        <OrderDetailsModal
          open={!!selectedOrder}
          onClose={() => setSelectedOrder(null)}
          order={selectedOrder}
        />
      )}

      {/* ✅ Begin Pickup Modal */}
      {pickupTxnIds.length > 0 && (
        <BeginPickupModal
          open={pickupTxnIds.length > 0}
          onClose={() => setPickupTxnIds([])}
          txnIds={pickupTxnIds}
          user={user}
          onConfirmPickup={onRefresh} // ✅ Refresh page after pickup
        />
      )}
    </Box>
  );
};

export default PickupStoreOrders;
