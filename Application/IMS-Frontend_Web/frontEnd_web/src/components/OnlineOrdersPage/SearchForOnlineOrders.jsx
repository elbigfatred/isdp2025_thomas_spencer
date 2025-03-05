import { useState } from "react";
import {
  Box,
  Typography,
  TextField,
  Button,
  CircularProgress,
  Paper,
} from "@mui/material";

const SearchForOnlineOrders = () => {
  const [searchValue, setSearchValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [order, setOrder] = useState(null);

  const handleSearch = () => {
    if (!searchValue) return;
    setLoading(true);
    fetch(`http://localhost:8080/api/online-orders/search?query=${searchValue}`)
      .then((res) => res.json())
      .then((data) => {
        setOrder(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching order:", err);
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

      {order && (
        <Paper sx={{ marginTop: 2, padding: 2 }}>
          <Typography variant="h6">Order Details</Typography>
          <Typography>Txn ID: {order.txnID}</Typography>
          <Typography>Customer: {order.customerInfo?.email}</Typography>
          <Typography>Total Items: {order.totalItems}</Typography>
          <Typography>Status: {order.status}</Typography>
        </Paper>
      )}
    </Box>
  );
};

export default SearchForOnlineOrders;
