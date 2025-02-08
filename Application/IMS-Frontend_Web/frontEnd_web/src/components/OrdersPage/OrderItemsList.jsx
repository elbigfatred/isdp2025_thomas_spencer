/* eslint-disable react/prop-types */
import { useState } from "react";
import { TableContainer, Table, TableHead, TableRow, TableCell, TableBody, Paper, Button, TextField, Box } from "@mui/material";

const OrderItemsList = ({ orderItems, onQuantityChange, onRemoveItem }) => {
  const [searchQuery, setSearchQuery] = useState("");

  // ✅ Filter order items based on search query
  const filteredItems = (Array.isArray(orderItems) ? orderItems : []).filter((item) => {
    if (!item || !item.itemID) return false;

    const sku = item.itemID.sku ? item.itemID.sku.toLowerCase() : "";
    const name = item.itemID.name ? item.itemID.name.toLowerCase() : "";
    const search = searchQuery.toLowerCase();

    return sku.includes(search) || name.includes(search);
  });
  return (
    <Box>
      {/* ✅ Search Bar */}
      <TextField label="Search Current Order" variant="outlined" fullWidth margin="normal" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />

      {/* ✅ Order Items Table */}
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
                <strong>Quantity in Order</strong>
              </TableCell>
              <TableCell>
                <strong>Actions</strong>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredItems.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} align="center">
                  No matching items
                </TableCell>
              </TableRow>
            ) : (
              filteredItems.map((item, index) => (
                <TableRow key={`${item.txnID}-${item.itemID.id}`}>
                  <TableCell>{item.itemID.sku}</TableCell>
                  <TableCell>{item.itemID.name}</TableCell>
                  <TableCell>
                    {/* <Button onClick={() => onQuantityChange(index, item.quantity - item.itemID.caseSize)}>-</Button> */}
                    <TextField
                      type="number"
                      value={item.quantity}
                      onChange={(e) => onQuantityChange(index, Number(e.target.value))}
                      inputProps={{
                        min: item.itemID.caseSize || 1,
                        step: item.itemID.caseSize || 1,
                      }}
                    />
                    {/* <Button onClick={() => onQuantityChange(index, item.quantity + item.itemID.caseSize)}>+</Button> */}
                  </TableCell>
                  <TableCell>
                    <Button color="error" onClick={() => onRemoveItem(index)}>
                      Remove
                    </Button>
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

export default OrderItemsList;
