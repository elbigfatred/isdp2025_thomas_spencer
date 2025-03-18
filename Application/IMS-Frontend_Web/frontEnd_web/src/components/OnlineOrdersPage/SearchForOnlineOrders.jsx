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
  Tooltip,
  IconButton,
} from "@mui/material";
import OrderDetailsModal from "./OrderDetailsModal"; // Import the modal
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon

const SearchForOnlineOrders = () => {
  const [searchValue, setSearchValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const handleSearch = () => {
    if (!searchValue) return;

    setLoading(true);

    fetch(`http://localhost:8080/api/orders/searchOrders?query=${searchValue}`)
      .then(async (res) => {
        if (!res.ok) {
          throw new Error(`HTTP error! Status: ${res.status}`);
        }

        const text = await res.text(); // Read response as text first
        try {
          return text ? JSON.parse(text) : []; // Safely parse JSON or return []
        } catch (error) {
          console.error("Invalid JSON response:", text);
          throw new Error("Invalid JSON received from server");
        }
      })
      .then((data) => {
        setOrders(Array.isArray(data) ? data : []); // Ensure orders is always an array
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching orders:", err);
        setOrders([]); // Reset orders to empty array on failure
        setLoading(false);
      });
  };

  return (
    <Box>
      <Typography variant="h6">
        Search for an Online Order
        <Tooltip
          title="Input a transaction ID or email to search for an order"
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Typography>
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
