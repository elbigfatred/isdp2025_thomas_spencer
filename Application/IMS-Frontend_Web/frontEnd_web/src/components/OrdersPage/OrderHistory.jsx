/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import {
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  TableContainer,
  Button,
  Box,
  Typography,
  TextField,
  Switch,
  FormControlLabel,
  Tooltip,
  IconButton,
} from "@mui/material";
import OrderDetailsModal from "./OrderDetailsModal"; //  Import modal component
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; //  Help Icon
import { set } from "date-fns";

const OrderHistory = ({ user, siteId, refreshTrigger, fetchAllOrders }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderItems, setOrderItems] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [showActiveOnly, setShowActiveOnly] = useState(true); // ✅ Active filter toggle

  useEffect(() => {
    if (!siteId && !fetchAllOrders) return; // ✅ Prevent fetching if no site is selected

    setLoading(true);
    const endpoint = fetchAllOrders
      ? "http://localhost:8080/api/orders/all"
      : `http://localhost:8080/api/orders/site/${siteId}`;

    axios
      .get(endpoint)
      .then((response) => {
        console.log("[DEBUG] Fetched Orders JSON:", response.data);
        // ✅ Filter orders based on txnType
        if (!response.data) {
          //setError("No orders found.");
          setOrders([]);
          return;
        }
        const filteredOrders = response.data.filter((order) =>
          ["Store Order", "Emergency Order", "Back Order"].includes(
            order.txnType.txnType
          )
        );

        setOrders(filteredOrders);
      })
      .catch(() => setError(" Failed to load orders."))
      .finally(() => setLoading(false));
  }, [siteId, refreshTrigger, fetchAllOrders]);

  // ✅ Handle "View Order" button click
  const handleViewOrder = async (order) => {
    console.log(`[DEBUG] Fetching items for txnID: ${order.id}`);

    try {
      const response = await axios.get(
        `http://localhost:8080/api/orders/${order.id}/items`
      );
      console.log("[DEBUG] Fetched Order Items:", response.data);

      setSelectedOrder(order);
      setOrderItems(response.data);
      setIsModalOpen(true);
    } catch (error) {
      console.error("[ERROR] Failed to fetch order items:", error);
    }
  };

  // ✅ Helper function for formatting dates
  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  // ✅ Enhanced search filtering logic
  const filteredOrders =
    orders.length > 0
      ? orders.filter(
          (order) =>
            (showActiveOnly
              ? !["CANCELLED", "REJECTED", "COMPLETE"].includes(
                  order.txnStatus?.statusName
                )
              : true) &&
            (order.id.toString().includes(searchQuery.trim()) ||
              (order.txnType?.txnType || "Unknown")
                .toLowerCase()
                .includes(searchQuery.toLowerCase().trim()) ||
              (order.txnStatus?.statusName || "Unknown")
                .toLowerCase()
                .includes(searchQuery.toLowerCase().trim()) ||
              formatDate(order.createdDate).includes(searchQuery.trim()))
        )
      : [];

  return (
    <Box>
      {/* ✅ Order History Section with Help Icon */}
      <Box display="flex" alignItems="center" gap={0}>
        <Typography variant="h6">Order History</Typography>
        <Tooltip
          title="This section shows a list of past orders for the selected site. Use the search bar to filter orders."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {error && <Typography color="error">{error}</Typography>}
      {loading && <Typography>Loading orders...</Typography>}

      {orders.length === 0 && !loading ? (
        <Typography variant="body1" align="center" sx={{ marginTop: 2 }}>
          No orders found for this site.
        </Typography>
      ) : (
        <>
          {/* ✅ Active Orders Toggle */}
          <FormControlLabel
            control={
              <Switch
                checked={showActiveOnly}
                onChange={() => setShowActiveOnly((prev) => !prev)}
                color="primary"
              />
            }
            label="Show Active Orders Only"
            sx={{ marginBottom: 2 }}
          />

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

          <TableContainer
            component={Paper}
            sx={{ maxHeight: "400px", overflowY: "auto" }}
          >
            <Table stickyHeader sx={{ tableLayout: "fixed" }}>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Order ID</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Site</strong>
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
                      <TableCell>
                        {order.siteIDTo?.siteName || "Unknown"}
                      </TableCell>
                      <TableCell>
                        {order.txnType?.txnType || "Unknown"}
                      </TableCell>
                      <TableCell>
                        {order.txnStatus?.statusName || "Unknown"}
                      </TableCell>
                      <TableCell>{formatDate(order.createdDate)}</TableCell>
                      <TableCell>{formatDate(order.shipDate)}</TableCell>
                      <TableCell>
                        <Button
                          variant="contained"
                          size="small"
                          onClick={() => handleViewOrder(order)}
                        >
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
      <OrderDetailsModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        order={selectedOrder}
        items={orderItems}
      />
    </Box>
  );
};

export default OrderHistory;
