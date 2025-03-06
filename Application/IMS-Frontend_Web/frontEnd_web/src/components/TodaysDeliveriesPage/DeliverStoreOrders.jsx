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
} from "@mui/material";
import DeliverOrderModal from "./DeliverOrderModal"; // ✅ Import modal

const DeliverStoreOrders = ({ orders, user, onRefresh }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);

  // ✅ FILTER ORDERS BASED ON USER ROLE
  const filteredOrders = useMemo(() => {
    if (["Administrator", "Delivery"].includes(user.mainrole)) {
      return orders; // Admin & Driver see ALL orders
    } else if (["Store Manager", "Store Worker"].includes(user.mainrole)) {
      return orders.filter((order) => order.siteIDTo.id === user.site.id); // Only store's orders
    }
    return []; // Other roles should see nothing
  }, [orders, user]);

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Deliver Store Orders
      </Typography>
      <Paper sx={{ padding: 2 }}>
        {filteredOrders.length === 0 ? (
          <Typography>No orders in transit.</Typography>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Order ID</TableCell>
                <TableCell>Destination</TableCell>
                <TableCell>Vehicle</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredOrders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell>{order.barCode}</TableCell>
                  <TableCell>{order.siteIDTo.siteName}</TableCell>
                  <TableCell>{order.deliveryID.vehicle.vehicleType}</TableCell>
                  <TableCell>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => setSelectedOrder(order)} // ✅ Open modal
                    >
                      Deliver Order
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Paper>

      {/* ✅ Deliver Order Modal */}
      {selectedOrder && (
        <DeliverOrderModal
          open={!!selectedOrder}
          onClose={() => setSelectedOrder(null)}
          order={selectedOrder}
          user={user}
          onRefresh={onRefresh}
        />
      )}
    </Box>
  );
};

export default DeliverStoreOrders;
