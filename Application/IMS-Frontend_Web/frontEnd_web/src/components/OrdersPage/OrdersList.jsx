/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, Button, Select, MenuItem, Typography } from "@mui/material";

const OrdersList = ({ user, onSelectOrder }) => {
  const [sites, setSites] = useState([]);
  const [selectedSite, setSelectedSite] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  // Fetch sites if user is a Warehouse Manager
  useEffect(() => {
    if (user.roles.some((role) => role.posn.permissionLevel === "Warehouse Manager")) {
      axios
        .get("http://localhost:8080/api/sites")
        .then((response) => setSites(response.data))
        .catch(() => console.error("Failed to load sites"));
    } else {
      setSelectedSite(user.site);
    }
  }, [user]);

  // Fetch orders when a site is selected
  useEffect(() => {
    if (!selectedSite) return;

    setLoading(true);
    axios
      .get(`http://localhost:8080/api/orders/active/${selectedSite.id}`)
      .then((response) => setOrders(response.data || []))
      .catch(() => console.error("Failed to load orders"))
      .finally(() => setLoading(false));
  }, [selectedSite]);

  return (
    <Paper sx={{ padding: 2, marginBottom: 4 }}>
      <Typography variant="h6">View Existing Orders</Typography>

      {user.roles.some((role) => role.posn.permissionLevel === "Warehouse Manager") && (
        <Select value={selectedSite?.id || ""} onChange={(e) => setSelectedSite(sites.find((site) => site.id === e.target.value))} fullWidth sx={{ marginBottom: 2 }}>
          {sites.map((site) => (
            <MenuItem key={site.id} value={site.id}>
              {site.siteName}
            </MenuItem>
          ))}
        </Select>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>
                <strong>Order ID</strong>
              </TableCell>
              <TableCell>
                <strong>Status</strong>
              </TableCell>
              <TableCell>
                <strong>Created Date</strong>
              </TableCell>
              <TableCell>
                <strong>Actions</strong>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={4}>Loading...</TableCell>
              </TableRow>
            ) : orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4}>No active orders found.</TableCell>
              </TableRow>
            ) : (
              orders.map((order) => (
                <TableRow key={order.txnID}>
                  <TableCell>{order.txnID}</TableCell>
                  <TableCell>{order.txnStatus.statusName}</TableCell>
                  <TableCell>{new Date(order.createdDate).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Button variant="outlined" onClick={() => onSelectOrder(order)}>
                      Edit
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};

export default OrdersList;
