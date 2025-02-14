/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, Button, Box, Typography, TextField } from "@mui/material";
import OrderDetailsModal from "./OrderDetailsModal"; // ✅ Import modal component

const OrderHistory = ({ user, siteId, refreshTrigger }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderItems, setOrderItems] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (!siteId) return; // ✅ Prevent fetching if no site is selected

    setLoading(true);
    axios
      .get(`http://localhost:8080/api/orders/site/${siteId}`)
      .then((response) => {
        console.log("[DEBUG] Fetched Orders JSON:", response.data);
        setOrders(response.data);
      })
      .catch(() => setError("Failed to load orders"))
      .finally(() => setLoading(false));
  }, [siteId, refreshTrigger]); // ✅ Orders now reload when site changes

  // ✅ Handle "View Order" button click
  const handleViewOrder = async (order) => {
    console.log(`[DEBUG] Fetching items for txnID: ${order.id}`);

    try {
      const response = await axios.get(`http://localhost:8080/api/orders/${order.id}/items`);
      console.log("[DEBUG] Fetched Order Items:", response.data);

      setSelectedOrder(order);
      setOrderItems(response.data);
      setIsModalOpen(true);
    } catch (error) {
      console.error("[ERROR] Failed to fetch order items:", error);
    }
  };

  // ✅ Enhanced search filtering logic
  const filteredOrders =
    orders.length > 0
      ? orders.filter(
          (order) =>
            order.id.toString().includes(searchQuery.trim()) ||
            (order.txnType?.txnType || "Unknown").toLowerCase().includes(searchQuery.toLowerCase().trim()) ||
            (order.txnStatus?.statusName || "Unknown").toLowerCase().includes(searchQuery.toLowerCase().trim()) ||
            new Date(order.createdDate).toLocaleDateString().includes(searchQuery.trim())
        )
      : [];

  return (
    <Box>
      <Typography variant="h6">Order History</Typography>

      {error && <Typography color="error">{error}</Typography>}
      {loading && <Typography>Loading orders...</Typography>}

      {orders.length === 0 && !loading ? (
        <Typography variant="body1" align="center" sx={{ marginTop: 2 }}>
          No orders found for this site.
        </Typography>
      ) : (
        <>
          {/* ✅ Search Bar */}
          <TextField
            label="Search Order History"
            placeholder="Search by ID, Type, Status, or Date..."
            variant="outlined"
            fullWidth
            margin="normal"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          <TableContainer component={Paper} sx={{ maxHeight: "400px", overflowY: "auto" }}>
            <Table stickyHeader sx={{ tableLayout: "fixed" }}>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Order ID</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Type</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Status</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Created Date</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Ship Date</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Actions</strong>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredOrders.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      No matching orders found.
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredOrders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell>{order.id}</TableCell>
                      <TableCell>{order.txnType?.txnType || "Unknown"}</TableCell>
                      <TableCell>{order.txnStatus?.statusName || "Unknown"}</TableCell>
                      <TableCell>{new Date(order.createdDate).toLocaleString()}</TableCell>
                      <TableCell>{order.shipDate ? new Date(order.shipDate).toLocaleDateString() : "N/A"}</TableCell>
                      <TableCell>
                        <Button variant="contained" size="small" onClick={() => handleViewOrder(order)}>
                          View Order
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      {/* ✅ Order Details Modal */}
      <OrderDetailsModal open={isModalOpen} onClose={() => setIsModalOpen(false)} order={selectedOrder} items={orderItems} />
    </Box>
  );
};

export default OrderHistory;
