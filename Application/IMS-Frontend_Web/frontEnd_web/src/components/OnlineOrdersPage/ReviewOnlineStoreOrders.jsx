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
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Dialog,
  IconButton,
  Tooltip,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon
import AssembleOrderModal from "./AssembleOrderModal";
import OrderDetailsModal from "./OrderDetailsModal";

const ReviewStoreOnlineOrders = ({ user }) => {
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedDetailsOrder, setSelectedDetailsOrder] = useState(null);
  const [sites, setSites] = useState([]); // ✅ Store site options for admins
  const [siteInventory, setSiteInventory] = useState({});
  const [inventoryLoading, setInventoryLoading] = useState(true);
  const [siteID, setSiteID] = useState(
    user.mainrole === "Administrator" ? "" : user.site.id
  ); // ✅ Store selected site

  // ✅ Fetch sites if user is an Admin
  useEffect(() => {
    if (user.mainrole === "Administrator") {
      fetch("http://localhost:8080/api/sites")
        .then((res) => res.json())
        .then((data) => {
          const retailSites = data.filter((site) =>
            site.siteName.includes("Retail")
          );
          setSites(retailSites);
          if (retailSites.length > 0) setSiteID(retailSites[0].id); // Default to first site
        })
        .catch((err) => console.error("Error fetching sites:", err));
    }
  }, [user.mainrole]);

  useEffect(() => {
    if (!siteID) return; // Ensure siteID is set before fetching
    setInventoryLoading(true);
    fetch(`http://localhost:8080/api/inventory/site/${siteID}`)
      .then((res) => res.json())
      .then((data) => {
        // ✅ Convert inventory array into an object { itemID: quantity }
        const inventoryMap = {};
        data.forEach((inv) => {
          inventoryMap[inv.item.id] = inv.quantity;
        });

        setSiteInventory(inventoryMap);
        setInventoryLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching inventory:", err);
        setInventoryLoading(false);
      });
  }, [siteID]); // ✅ Runs whenever siteID changes

  // ✅ Fetch orders when siteID changes
  useEffect(() => {
    if (!siteID) return;
    setLoading(true);
    fetch(`http://localhost:8080/api/orders/online?siteID=${siteID}`)
      .then((res) => res.json())
      .then((data) => {
        setOrders(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching orders:", err);
        setLoading(false);
      });
  }, [siteID]);

  // ✅ Filter orders into categories
  const newOrders = orders.filter(
    (order) => order.txnStatus.statusName === "SUBMITTED"
  );
  const readyForPickup = orders.filter(
    (order) => order.txnStatus.statusName === "ASSEMBLED"
  );
  const pastOrders = orders.filter(
    (order) => order.txnStatus.statusName === "COMPLETE"
  );

  // ✅ Admin site selection dropdown
  const renderSiteSelector = () => {
    if (user.mainrole !== "Administrator") return null;
    return (
      <FormControl sx={{ marginBottom: 2, width: "300px" }}>
        <InputLabel>Select Site</InputLabel>
        <Select value={siteID} onChange={(e) => setSiteID(e.target.value)}>
          {sites.map((site) => (
            <MenuItem key={site.id} value={site.id}>
              {site.siteName}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  };

  // ✅ Render orders table dynamically
  const renderOrdersTable = (title, orderList) => (
    <Paper sx={{ marginBottom: 3 }}>
      <Typography variant="h6" sx={{ padding: 2 }}>
        {title}
      </Typography>
      {orderList.length === 0 ? (
        <Typography sx={{ padding: 2, color: "gray" }}>
          No orders in this category.
        </Typography>
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Order ID</TableCell>
              <TableCell>Customer</TableCell>
              <TableCell>Pickup Time</TableCell>
              <TableCell>Status</TableCell>
              {title === "NEW ORDERS" && <TableCell>Actions</TableCell>}
              {title === "READY FOR PICKUP" && <TableCell>Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {orderList.map((order) => {
              let customerInfo = {};
              try {
                customerInfo = order.notes ? JSON.parse(order.notes) : {};
              } catch (error) {
                console.error("Error parsing customer notes:", error);
              }

              return (
                <TableRow key={order.id}>
                  <TableCell>{order.id}</TableCell>
                  <TableCell>{customerInfo.email || "Unknown"}</TableCell>
                  <TableCell>
                    {new Date(order.shipDate).toLocaleString("en-US", {
                      weekday: "long",
                      month: "long",
                      day: "numeric",
                      hour: "numeric",
                      minute: "2-digit",
                      hour12: true,
                    })}
                  </TableCell>
                  <TableCell>
                    {order.txnStatus.statusName === "ASSEMBLED"
                      ? "READY FOR PICKUP"
                      : order.txnStatus.statusName}
                  </TableCell>

                  {/* ✅ New Orders: View/Assemble */}
                  {title === "NEW ORDERS" && (
                    <TableCell>
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={() => setSelectedOrder(order)}
                      >
                        View/Assemble
                      </Button>
                    </TableCell>
                  )}

                  {/* ✅ Ready for Pickup: View + Confirm Received */}
                  {title === "READY FOR PICKUP" && (
                    <TableCell>
                      <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => setSelectedDetailsOrder(order)}
                        sx={{ marginRight: 1 }}
                      >
                        View
                      </Button>
                      <Button
                        variant="contained"
                        color="success"
                        onClick={() => handleConfirmReceived(order.id)}
                      >
                        Confirm Received
                      </Button>
                    </TableCell>
                  )}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      )}
    </Paper>
  );

  // ✅ Handle confirming order pickup
  const handleConfirmReceived = (txnId) => {
    console.log("[DEBUG] Confirming order received:", txnId);

    fetch(
      `http://localhost:8080/api/orders/${txnId}/update-status?status=COMPLETE&empUsername=${user.username}`,
      { method: "PUT", headers: { "Content-Type": "application/json" } }
    )
      .then((res) => {
        if (!res.ok) throw new Error("Failed to update order status");
        return res.json();
      })
      .then(() => {
        console.log("[DEBUG] Order status updated to COMPLETE.");
        setOrders((prevOrders) =>
          prevOrders.map((order) =>
            order.id === txnId
              ? { ...order, txnStatus: { statusName: "COMPLETE" } }
              : order
          )
        );
      })
      .catch((err) => console.error("[ERROR] Confirming order received:", err));
  };

  const handleAssemblyComplete = (txnId) => {
    console.log("[DEBUG] Order Assembled:", txnId);

    // ✅ Update order status in UI
    setOrders((prevOrders) =>
      prevOrders.map((order) =>
        order.id === txnId
          ? { ...order, txnStatus: { statusName: "ASSEMBLED" } }
          : order
      )
    );

    // ✅ Close the modal
    setSelectedOrder(null);
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        {user.mainrole === "Administrator"
          ? "Online Orders"
          : `Online Orders for ${user.site.siteName}`}
        <Tooltip
          title="Review online orders, assemble them, and confirm receipt."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Typography>

      {/* ✅ Admins can select a site */}
      {renderSiteSelector()}

      {loading ? (
        <CircularProgress />
      ) : (
        <>
          {renderOrdersTable("NEW ORDERS", newOrders)}
          {renderOrdersTable("READY FOR PICKUP", readyForPickup)}
          {renderOrdersTable("PAST ORDERS", pastOrders)}
        </>
      )}

      {/* ✅ Modal for assembling an order */}
      {selectedOrder && (
        <AssembleOrderModal
          open={!!selectedOrder}
          onClose={() => setSelectedOrder(null)}
          order={selectedOrder}
          user={user}
          onAssemblyComplete={handleAssemblyComplete}
          inventory={siteInventory}
        />
      )}

      {/* ✅ Modal for viewing order details */}
      {selectedDetailsOrder && (
        <OrderDetailsModal
          open={!!selectedDetailsOrder}
          onClose={() => setSelectedDetailsOrder(null)}
          order={selectedDetailsOrder}
        />
      )}
    </Box>
  );
};

export default ReviewStoreOnlineOrders;
