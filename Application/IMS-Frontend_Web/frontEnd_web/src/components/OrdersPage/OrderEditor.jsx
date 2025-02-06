/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import { useState, useEffect } from "react";
import axios from "axios";
import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, Button, Box, Typography, TextField } from "@mui/material";

const OrderEditor = ({ selectedSite }) => {
  const [orderItems, setOrderItems] = useState([]); // "Cart" (items in the order)
  const [availableItems, setAvailableItems] = useState([]); // Full inventory
  const [activeOrder, setActiveOrder] = useState(null); // Active order ID
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  // ✅ Check if an active order exists & load data
  useEffect(() => {
    if (!selectedSite) return;
    setLoading(true);

    axios
      .get(`http://localhost:8080/api/orders/check-active?siteId=${selectedSite.id}`)
      .then((res) => {
        if (res.data) {
          // ✅ Load the existing order if active
          axios
            .get(`http://localhost:8080/api/orders?siteId=${selectedSite.id}`)
            .then((res) => {
              setActiveOrder(res.data);
              setOrderItems(res.data.items || []);
            })
            .catch(() => setError("Failed to load active order."));
        } else {
          setActiveOrder(null);
          setOrderItems([]); // No active order
        }
      })
      .catch(() => setError("Failed to check for active order."));

    // ✅ Load all inventory items for manual selection
    axios
      .get(`http://localhost:8080/api/inventory?siteId=${selectedSite.id}`)
      .then((res) => setAvailableItems(res.data))
      .catch(() => setError("Failed to load inventory"))
      .finally(() => setLoading(false));
  }, [selectedSite]);

  // ✅ Create a New Order (ONLY calls `prepopulate` on creation)
  const handleCreateOrder = () => {
    if (!selectedSite) return;

    const orderData = {
      siteIDTo: selectedSite.id,
      siteIDFrom: 1, // Default to warehouse
      txnType: "Store Order",
      txnStatus: "NEW",
    };

    axios
      .post("http://localhost:8080/api/orders", orderData)
      .then((response) => {
        alert("Order created successfully!");
        setActiveOrder(response.data);

        // ✅ Fetch prepopulated items that need reordering
        axios
          .get(`http://localhost:8080/api/orders/prepopulate?siteId=${selectedSite.id}`)
          .then((res) => {
            setOrderItems(
              res.data.map((item) => ({
                ...item,
                quantity: item.item.caseSize, // Default to 1 case
              }))
            );
          })
          .catch(() => alert("Failed to auto-add reorder items."));
      })
      .catch(() => alert("Failed to create order."));
  };

  // ✅ Handle Quantity Change (Case Size Increments)
  const handleQuantityChange = (index, change) => {
    setOrderItems((prevItems) => {
      const updatedItems = [...prevItems];
      let newQuantity = updatedItems[index].quantity + updatedItems[index].item.caseSize * change;
      if (newQuantity < 0) newQuantity = 0;
      updatedItems[index].quantity = newQuantity;
      return updatedItems;
    });
  };

  // ✅ Handle Adding Item to Order
  const handleAddItem = (item) => {
    setOrderItems((prevItems) => {
      if (prevItems.some((i) => i.item.id === item.id)) {
        alert("Item already in order.");
        return prevItems;
      }
      return [...prevItems, { item, quantity: item.caseSize }];
    });
  };

  // ✅ Handle Removing Item from Order
  const handleRemoveItem = (index) => {
    setOrderItems((prevItems) => prevItems.filter((_, i) => i !== index));
  };

  return (
    <Paper sx={{ padding: 2 }}>
      <Typography variant="h6">{activeOrder ? "Edit Order" : "Create New Order"}</Typography>

      {error && <Typography color="error">{error}</Typography>}
      {loading && <Typography>Loading...</Typography>}

      {/** ✅ Show Create Order Button if No Active Order Exists **/}
      {!activeOrder && (
        <Button variant="contained" color="primary" onClick={handleCreateOrder} sx={{ marginBottom: 2 }}>
          Create Order
        </Button>
      )}

      {activeOrder && (
        <Box sx={{ display: "flex", gap: 2, marginTop: 3 }}>
          {/* ✅ Cart Section - Items Currently in Order */}
          <TableContainer component={Paper} sx={{ flex: 1, maxHeight: "60vh", overflow: "auto" }}>
            <Typography variant="h6" sx={{ padding: 1 }}>
              Current Order
            </Typography>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Item</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Quantity</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Actions</strong>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {orderItems.map((item, index) => (
                  <TableRow key={item.item.id}>
                    <TableCell>{item.item.name}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>
                      <Button onClick={() => handleQuantityChange(index, 1)}>+1 Case</Button>
                      <Button onClick={() => handleQuantityChange(index, -1)}>-1 Case</Button>
                      <Button color="error" onClick={() => handleRemoveItem(index)}>
                        Remove
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* ✅ Available Inventory */}
          <TableContainer component={Paper} sx={{ flex: 1, maxHeight: "60vh", overflow: "auto" }}>
            <Typography variant="h6" sx={{ padding: 1 }}>
              Available Inventory
            </Typography>
            <TextField fullWidth placeholder="Search..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} sx={{ marginBottom: 2 }} />
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Item</strong>
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
        </Box>
      )}
    </Paper>
  );
};

export default OrderEditor;
