/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, Button, Box, Typography, Select, MenuItem, TextField } from "@mui/material";

const OrdersPage = ({ user }) => {
  const [sites, setSites] = useState([]);
  const [selectedSite, setSelectedSite] = useState(null);
  const [orderItems, setOrderItems] = useState([]); // Items being ordered
  const [availableItems, setAvailableItems] = useState([]); // All items to add
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  // Fetch sites if the user is a Warehouse Manager
  useEffect(() => {
    if (user.roles.some((role) => role.posn.permissionLevel === "Warehouse Manager")) {
      axios
        .get("http://localhost:8080/api/sites")
        .then((response) => setSites(response.data))
        .catch(() => setError("Failed to load sites"));
    } else {
      setSelectedSite(user.site);
    }
  }, [user]);

  // Fetch order items and available items when a site is selected
  useEffect(() => {
    if (!selectedSite) return;

    setLoading(true);

    axios
      .get(`http://localhost:8080/api/orders/prepopulate?siteId=${selectedSite.id}`)
      .then((response) => {
        console.log(response.data);
        const items = response.data.map((item) => ({
          ...item,
          quantity: item.optimumThreshold - (item.quantity || 0), // Ensure valid quantity
        }));
        setOrderItems(items);
      })
      .catch(() => setError("Failed to load order items"));

    axios
      .get("http://localhost:8080/api/items")
      .then((response) => {
        console.log(response.data);
        setAvailableItems(response.data);
      })
      .catch(() => setError("Failed to load available items"))
      .finally(() => setLoading(false));
  }, [selectedSite]);

  // Handle Site Selection
  const handleSiteChange = (event) => {
    const site = sites.find((s) => s.id === event.target.value);
    setSelectedSite(site);
  };

  // Handle Quantity Change
  const handleQuantityChange = (index, newQuantity) => {
    setOrderItems((prevItems) => {
      const updatedItems = [...prevItems];
      updatedItems[index].quantity = newQuantity || 0; // Ensure it's a valid number
      return updatedItems;
    });
  };

  // Handle Adding Item to Order
  const handleAddItem = (item) => {
    setOrderItems((prevItems) => {
      if (prevItems.some((i) => i.id?.itemID === item.id)) {
        console.warn("Item already in order:", item);
        alert("Item already in order.");
        return prevItems; // Prevent duplicate entries
      }
      return [
        ...prevItems,
        {
          id: { itemID: item.id, siteID: selectedSite?.id || 0 },
          quantity: item.optimumThreshold - (item.quantity || 0),
          item,
        },
      ];
    });
  };

  // Handle Removing Item from Order
  const handleRemoveItem = (index) => {
    setOrderItems((prevItems) => {
      const updatedItems = [...prevItems];
      updatedItems.splice(index, 1);
      return updatedItems;
    });
  };

  // Handle Order Submission
  const handleSubmitOrder = () => {
    if (!selectedSite) return alert("Please select a site first.");

    const orderData = {
      siteIDTo: selectedSite.id,
      siteIDFrom: 1, // Default to warehouse (id=1)
      shipDate: selectedSite.nextDeliveryDate,
      items: orderItems.map((item) => ({
        itemID: item.item.itemID,
        quantity: item.quantity,
      })),
      txnType: "Order",
      txnStatus: "NEW",
    };

    axios
      .post("http://localhost:8080/api/orders", orderData)
      .then(() => alert("Order submitted successfully!"))
      .catch(() => alert("Failed to submit order"));
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        Create Order
      </Typography>

      {/* Site Selection (Only for Warehouse Manager) */}
      {user.roles.some((role) => role.posn.permissionLevel === "Warehouse Manager") && (
        <Select value={selectedSite?.id || ""} onChange={handleSiteChange} fullWidth>
          {sites.map((site) => (
            <MenuItem key={site.id} value={site.id}>
              {site.siteName}
            </MenuItem>
          ))}
        </Select>
      )}

      {error && <Typography color="error">{error}</Typography>}
      {loading && <Typography>Loading items...</Typography>}

      <Box sx={{ display: "flex", gap: 2, marginTop: 3 }}>
        {/* Available Items Table */}
        <TableContainer component={Paper} sx={{ flex: 1, maxHeight: "60vh", overflow: "auto" }}>
          <Typography variant="h6" sx={{ padding: 1 }}>
            Available Items
          </Typography>
          <TextField fullWidth placeholder="Search for an item..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} sx={{ marginBottom: 2 }} />
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>
                  <strong>SKU</strong>
                </TableCell>
                <TableCell>
                  <strong>Item Name</strong>
                </TableCell>
                <TableCell>
                  <strong>Case Size</strong>
                </TableCell>
                <TableCell>
                  <strong>Actions</strong>
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {availableItems
                .filter((item) => item.name.toLowerCase().includes(searchTerm.toLowerCase()))
                .map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.sku}</TableCell>
                    <TableCell>{item.name}</TableCell>
                    <TableCell>{item.caseSize}</TableCell>
                    <TableCell>
                      <Button onClick={() => handleAddItem(item)}>Add</Button>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Order Items Table */}
        <TableContainer component={Paper} sx={{ flex: 1, maxHeight: "60vh", overflow: "auto" }}>
          <Typography variant="h6" sx={{ padding: 1 }}>
            Items in Order
          </Typography>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>
                  <strong>SKU</strong>
                </TableCell>
                <TableCell>
                  <strong>Item Name</strong>
                </TableCell>
                <TableCell>
                  <strong>Order Quantity</strong>
                </TableCell>
                <TableCell>
                  <strong>Actions</strong>
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orderItems.map((item, index) => (
                <TableRow key={`${item.id.itemID}-${item.id.siteID}`}>
                  <TableCell>{item.item.sku}</TableCell>
                  <TableCell>{item.item.name}</TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={isNaN(item.quantity) ? 0 : item.quantity}
                      onChange={(e) => handleQuantityChange(index, Number(e.target.value))}
                      inputProps={{ min: 0, step: item.item.caseSize || 1 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Button color="error" onClick={() => handleRemoveItem(index)}>
                      Remove
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>

      <Button variant="contained" color="primary" onClick={handleSubmitOrder} sx={{ marginTop: 2 }} disabled={orderItems.length === 0}>
        Submit Order
      </Button>
    </Box>
  );
};

export default OrdersPage;
