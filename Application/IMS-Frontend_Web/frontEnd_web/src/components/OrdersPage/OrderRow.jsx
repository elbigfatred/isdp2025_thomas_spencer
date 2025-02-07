/* eslint-disable react/prop-types */
import { TableRow, TableCell, Button } from "@mui/material";

const OrderHistoryRow = ({ order, onViewOrder, setIsModalOpen }) => {
  return (
    <TableRow>
      <TableCell>{order.id}</TableCell>
      <TableCell>{order.txnStatus?.statusName || "Unknown"}</TableCell>
      <TableCell>{new Date(order.createdDate).toLocaleString()}</TableCell>
      <TableCell>{new Date(order.shipDate).toLocaleDateString()}</TableCell>
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
