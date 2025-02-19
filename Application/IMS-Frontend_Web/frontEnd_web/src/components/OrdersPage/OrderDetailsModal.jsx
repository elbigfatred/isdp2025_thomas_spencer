/* eslint-disable react/prop-types */
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Typography,
  Box,
} from "@mui/material";

const OrderDetailsModal = ({ open, onClose, order, items }) => {
  if (!order) return null;

  // ✅ Ensure items is always an array (prevent errors)
  const orderItems = items || [];

  // ✅ Calculate total weight (defaults to 0 if no items)
  const totalWeight = orderItems.length
    ? orderItems.reduce((sum, item) => {
        const itemWeight = item.itemID?.weight || 0; // Default weight: 0
        return sum + itemWeight * item.quantity;
      }, 0)
    : 0;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Order Details</DialogTitle>
      <DialogContent>
        <Typography variant="h6">Order ID: {order.id}</Typography>
        <Typography variant="body1">
          Status: {order.txnStatus?.statusName || "Unknown"}
        </Typography>
        <Typography variant="body1">
          Ship Date:{" "}
          {new Date(order.shipDate).toLocaleDateString("en-US", {
            weekday: "long",
            year: "numeric",
            month: "long",
            day: "numeric",
          })}
        </Typography>

        {/* ✅ Show total order weight (ensuring it never breaks) */}
        <Typography
          variant="body1"
          sx={{
            fontWeight: "bold",
            marginTop: 1,
            color: totalWeight ? "inherit" : "gray",
          }}
        >
          Total Order Weight: {totalWeight} kg
        </Typography>

        {/* ✅ Order Items Table */}
        {orderItems.length > 0 ? (
          <Table sx={{ marginTop: 2 }}>
            <TableHead>
              <TableRow>
                <TableCell>
                  <strong>SKU</strong>
                </TableCell>
                <TableCell>
                  <strong>Item Name</strong>
                </TableCell>
                <TableCell>
                  <strong>Quantity</strong>
                </TableCell>
                <TableCell>
                  <strong>Weight (kg)</strong>
                </TableCell>
                <TableCell>
                  <strong>Total Weight (kg)</strong>
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orderItems.map((item) => (
                <TableRow key={item.itemID?.id}>
                  <TableCell>{item.itemID?.sku || "N/A"}</TableCell>
                  <TableCell>{item.itemID?.name || "Unknown Item"}</TableCell>
                  <TableCell>{item.quantity}</TableCell>
                  <TableCell>{item.itemID?.weight || 0}</TableCell>
                  <TableCell>
                    {((item.itemID?.weight || 0) * item.quantity).toFixed(2)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : (
          <Typography
            variant="body2"
            sx={{ marginTop: 2, fontStyle: "italic", color: "gray" }}
          >
            No items in this order.
          </Typography>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} variant="contained" color="primary">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default OrderDetailsModal;
