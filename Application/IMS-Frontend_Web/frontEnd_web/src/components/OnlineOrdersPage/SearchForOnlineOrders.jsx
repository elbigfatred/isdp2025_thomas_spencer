import { useState } from "react";
import {
  Box,
  Typography,
  TextField,
  Button,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import OrderDetailsModal from "./OrderDetailsModal"; // Import the modal

const SearchForOnlineOrders = () => {
  const [searchValue, setSearchValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const handleSearch = () => {
    if (!searchValue) return;
    setLoading(true);
    fetch(`http://localhost:8080/api/orders/searchOrders?query=${searchValue}`)
      .then((res) => res.json())
      .then((data) => {
        setOrders(data || []); // Handle null response
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching orders:", err);
        setLoading(false);
      });
  };

  return (
    <Box>
      <Typography variant="h6">Search for an Online Order</Typography>
      <TextField
        label="Txn ID or Email"
        value={searchValue}
        onChange={(e) => setSearchValue(e.target.value)}
        fullWidth
        sx={{ marginBottom: 2 }}
      />
      <Button variant="contained" onClick={handleSearch}>
        Search
      </Button>

      {loading && <CircularProgress sx={{ marginTop: 2 }} />}

      {orders.length > 0 && (
        <TableContainer component={Paper} sx={{ marginTop: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Order ID</TableCell>
                <TableCell>Pickup Location</TableCell>
                <TableCell>Pickup Time</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell>{order.id}</TableCell>
                  <TableCell>{order.siteIDTo.siteName}</TableCell>
                  <TableCell>
                    {new Date(order.shipDate).toLocaleString("en-US", {
                      weekday: "long",
                      hour: "numeric",
                      minute: "2-digit",
                      hour12: true,
                      month: "long",
                      day: "numeric",
                    })}
                  </TableCell>
                  <TableCell>
                    {order.txnStatus.statusName === "ASSEMBLED"
                      ? "READY FOR PICKUP"
                      : order.txnStatus.statusName}
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => setSelectedOrder(order)}
                    >
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Order Details Modal */}
      {selectedOrder && (
        <OrderDetailsModal
          open={!!selectedOrder}
          onClose={() => setSelectedOrder(null)}
          order={selectedOrder}
        />
      )}
    </Box>
  );
};

export default SearchForOnlineOrders;
