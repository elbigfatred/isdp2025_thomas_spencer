/* eslint-disable react/prop-types */
import { useState, useMemo } from "react";
import {
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  Button,
  TextField,
  Box,
  Typography,
  useTheme,
} from "@mui/material";

const OrderItemsList = ({
  orderItems = [],
  onQuantityChange,
  onRemoveItem,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const theme = useTheme(); // ✅ Get theme for dark/light mode

  // ✅ Ensure orderItems is always an array
  const safeOrderItems = Array.isArray(orderItems) ? orderItems : [];

  // ✅ Memoized total calculations (NOT affected by search)
  const totalQuantity = useMemo(
    () => safeOrderItems.reduce((sum, item) => sum + (item?.quantity || 0), 0),
    [safeOrderItems]
  );

  const totalPrice = useMemo(
    () =>
      safeOrderItems.reduce(
        (sum, item) =>
          sum + (item?.itemID?.costPrice || 0) * (item?.quantity || 0),
        0
      ),
    [safeOrderItems]
  );

  const totalWeight = useMemo(
    () =>
      safeOrderItems.reduce(
        (sum, item) =>
          sum + (item?.itemID?.weight || 0) * (item?.quantity || 0),
        0
      ),
    [safeOrderItems]
  );

  // ✅ Filter order items based on search query
  const filteredItems = useMemo(() => {
    return safeOrderItems.filter((item) => {
      if (!item || !item.itemID) return false;
      const sku = item.itemID.sku?.toLowerCase() || "";
      const name = item.itemID.name?.toLowerCase() || "";
      return (
        sku.includes(searchQuery.toLowerCase()) ||
        name.includes(searchQuery.toLowerCase())
      );
    });
  }, [safeOrderItems, searchQuery]);

  return (
    <Box>
      {/* ✅ Title */}
      <Typography
        variant="h4"
        fontWeight="bold"
        color="primary"
        sx={{
          textTransform: "uppercase",
          letterSpacing: 1.2,
          display: "flex",
          alignItems: "center",
        }}
      >
        Current Cart
      </Typography>

      {/* ✅ Search Bar */}
      <TextField
        label="Search Current Order"
        variant="outlined"
        fullWidth
        margin="normal"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {/* ✅ Order Items Table */}
      <TableContainer
        component={Paper}
        sx={{
          maxHeight: "600px",
          minHeight: "600px",
          overflowY: "auto",
          backgroundColor: theme.palette.background.paper, // ✅ Adjusts to dark/light mode
        }}
      >
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
                <strong>Price</strong>
              </TableCell>
              <TableCell>
                <strong>Weight</strong>
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
                  No matching items
                </TableCell>
              </TableRow>
            ) : (
              filteredItems.map((item, index) => (
                <TableRow
                  key={`${item?.txnID || index}-${item?.itemID?.id || index}`}
                >
                  <TableCell>{item?.itemID?.sku || "N/A"}</TableCell>
                  <TableCell>{item?.itemID?.name || "Unnamed Item"}</TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={item?.quantity || 1}
                      onChange={(e) =>
                        onQuantityChange(index, Number(e.target.value))
                      }
                      inputProps={{
                        min: item?.itemID?.caseSize || 1,
                        step: item?.itemID?.caseSize || 1,
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    $
                    {(
                      (item?.itemID?.costPrice || 0) * (item?.quantity || 0)
                    ).toFixed(2)}
                  </TableCell>
                  <TableCell>
                    {(
                      (item?.itemID?.weight || 0) * (item?.quantity || 0)
                    ).toFixed(2)}{" "}
                    kg
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

      {/* ✅ Totals Section */}
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mt={2}
        p={2}
        sx={{
          backgroundColor: theme.palette.background.default, // ✅ Dark/light mode adaptive
          borderRadius: 1,
          boxShadow: 1,
        }}
      >
        <Typography variant="h6">
          Total Quantity: <strong>{totalQuantity}</strong>
        </Typography>
        <Typography variant="h6">
          Total Cost: <strong>${totalPrice.toFixed(2)}</strong>
        </Typography>
        <Typography variant="h6">
          Total Weight (kg): <strong>{totalWeight.toFixed(2)} kg</strong>
        </Typography>
      </Box>
    </Box>
  );
};

export default OrderItemsList;
