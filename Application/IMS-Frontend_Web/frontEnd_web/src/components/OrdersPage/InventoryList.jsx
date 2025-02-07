/* eslint-disable react/prop-types */
import { useState } from "react";
import { TableContainer, Table, TableHead, TableRow, TableCell, TableBody, Paper, Button, TextField, Box, Typography } from "@mui/material";

const InventoryList = ({ availableItems, onAddItem }) => {
  const [searchQuery, setSearchQuery] = useState("");

  // ✅ Filter items based on search input (by SKU or name)
  const filteredItems = availableItems.filter(
    (inventory) => inventory.item.sku.toLowerCase().includes(searchQuery.toLowerCase()) || inventory.item.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <Box>
      {/* ✅ Search Bar */}
      <TextField label="Search Site Inventory" variant="outlined" fullWidth margin="normal" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />

      {/* ✅ Inventory Table */}
      <TableContainer component={Paper} sx={{ maxHeight: "600px", minHeight: "600px", overflowY: "auto" }}>
        <Table stickyHeader sx={{ tableLayout: "fixed" }}>
          <TableHead>
            <TableRow>
              <TableCell>
                <strong>SKU</strong>
              </TableCell>
              <TableCell>
                <strong>Item Name</strong>
              </TableCell>
              <TableCell>
                <strong>Quantity Per Case</strong>
              </TableCell>
              <TableCell>
                <strong>Quantity in Stock</strong>
              </TableCell>
              <TableCell>
                <strong>Optimum Threshold</strong>
              </TableCell>
              <TableCell>
                <strong>Actions</strong>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredItems.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography variant="body2" color="textSecondary">
                    Loading inventory...
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredItems.map((inventory) => (
                <TableRow key={inventory.id.itemID}>
                  <TableCell>{inventory.item.sku}</TableCell>
                  <TableCell>{inventory.item.name}</TableCell>
                  <TableCell>{inventory.item.caseSize}</TableCell>
                  <TableCell>{inventory.quantity}</TableCell>
                  <TableCell>{inventory.optimumThreshold}</TableCell>
                  <TableCell>
                    <Button onClick={() => onAddItem(inventory.item)}>Add Item</Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default InventoryList;
