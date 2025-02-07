/* eslint-disable react/prop-types */
import { useState } from "react";
import { TableContainer, Table, TableHead, TableRow, TableCell, TableBody, Paper, Typography, TextField, Box } from "@mui/material";

const OrderItemTable = ({ items }) => {
  const [searchQuery, setSearchQuery] = useState("");

  // ✅ Filter items based on search input
  const filteredItems = items.filter((item) => item.itemID?.sku.toLowerCase().includes(searchQuery.toLowerCase()) || item.itemID?.name.toLowerCase().includes(searchQuery.toLowerCase()));

  return (
    <Box>
      {/* ✅ Search Input */}
      <TextField label="Search Current Order" variant="outlined" fullWidth margin="normal" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />

      {/* ✅ Order Items Table */}
      <TableContainer component={Paper} sx={{ marginTop: 1 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>
                <strong>Item ID</strong>
              </TableCell>
              <TableCell>
                <strong>SKU</strong>
              </TableCell>
              <TableCell>
                <strong>Item Name</strong>
              </TableCell>
              <TableCell>
                <strong>Quantity</strong>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredItems.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} align="center">
                  <Typography variant="body2" color="textSecondary">
                    No matching items found.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredItems.map((item, index) => (
                <TableRow key={index}>
                  <TableCell>{item.itemID?.id || "Unknown"}</TableCell>
                  <TableCell>{item.itemID?.sku || "N/A"}</TableCell>
                  <TableCell>{item.itemID?.name || "N/A"}</TableCell>
                  <TableCell>{item.quantity}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default OrderItemTable;
