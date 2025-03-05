import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  CircularProgress,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
} from "@mui/material";

const ReviewStoreOnlineOrders = ({ user }) => {
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    fetch(`http://localhost:8080/api/online-orders?storeID=${user.site.id}`)
      .then((res) => res.json())
      .then((data) => {
        setOrders(data);
        setLoading(false);
      })
      .catch((err) => console.error("Error fetching orders:", err));
  }, [user.site.id]);

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Online Orders for {user.site.siteName}
      </Typography>

      {loading ? (
        <CircularProgress />
      ) : (
        <Paper>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Txn ID</TableCell>
                <TableCell>Customer</TableCell>
                <TableCell>Total Items</TableCell>
                <TableCell>Order Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.txnID}>
                  <TableCell>{order.txnID}</TableCell>
                  <TableCell>{order.customerInfo?.email}</TableCell>
                  <TableCell>{order.totalItems}</TableCell>
                  <TableCell>{order.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
};

export default ReviewStoreOnlineOrders;
