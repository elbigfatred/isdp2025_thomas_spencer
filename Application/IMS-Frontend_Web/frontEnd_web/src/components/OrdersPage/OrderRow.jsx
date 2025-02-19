/* eslint-disable react/prop-types */
import { TableRow, TableCell, Button } from "@mui/material";

// Helper function to format dates nicely
const formatDate = (dateString) => {
  if (!dateString) return "N/A";
  return new Date(dateString).toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

const OrderHistoryRow = ({ order, onViewOrder, setIsModalOpen }) => {
  console.log("[DEBUG] Rendering Order History Row:", order);
  return (
    <TableRow>
      <TableCell>{order.id}</TableCell>
      <TableCell>{order.txnStatus?.statusName || "Unknown"}</TableCell>
      <TableCell>{formatDate(order.createdDate)}</TableCell>
      <TableCell>{formatDate(order.shipDate)}</TableCell>
      <TableCell>
        <Button
          variant="contained"
          size="small"
          onClick={() => {
            onViewOrder(order);
            setIsModalOpen(true);
          }}
        >
          View Order
        </Button>
      </TableCell>
    </TableRow>
  );
};

export default OrderHistoryRow;
