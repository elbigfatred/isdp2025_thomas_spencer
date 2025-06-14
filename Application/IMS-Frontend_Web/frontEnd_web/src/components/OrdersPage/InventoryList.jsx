/* eslint-disable react/prop-types */
import { useState, useMemo, useEffect } from "react";
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
  TablePagination,
  CircularProgress,
  FormControlLabel,
  Switch,
  IconButton,
  Tooltip,
} from "@mui/material";

import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; //  Help Icon

const InventoryList = ({
  availableItems,
  onAddItem,
  isEmergencyOrder,
  maxEmergencyItems,
  orderItems,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [loading, setLoading] = useState(true);
  const [showLowStockOnly, setShowLowStockOnly] = useState(false); // ✅ Toggle for filtering low stock items

  useEffect(() => {
    setLoading(!availableItems || availableItems.length === 0); // ✅ Properly handle loading state
  }, [availableItems]);

  // ✅ Filter items based on search input AND toggle state
  const filteredItems = useMemo(() => {
    console.log(orderItems);
    return availableItems
      .filter(
        (inventory) =>
          inventory.item.sku
            .toLowerCase()
            .includes(searchQuery.toLowerCase()) ||
          inventory.item.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
      .filter((inventory) => {
        if (showLowStockOnly) {
          return inventory.quantity < inventory.optimumThreshold; // ✅ Only show low-stock items when enabled
        }
        return true;
      });
  }, [availableItems, searchQuery, showLowStockOnly]);

  // ✅ Get only the rows for the current page
  const paginatedItems = filteredItems.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box>
      {/* ✅ Search Bar & Low Stock Toggle Inline */}
      <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
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
          Available Inventory
        </Typography>

        <Tooltip
          title="This table displays all available inventory for the current site. 
             Use the search bar to filter items and toggle low stock mode to see only understocked items."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>

        <FormControlLabel
          control={
            <Switch
              checked={showLowStockOnly}
              onChange={(e) => setShowLowStockOnly(e.target.checked)}
            />
          }
          label="Under Optimum Threshold Only"
          sx={{ whiteSpace: "nowrap" }}
        />
      </Box>

      {/*  Search Bar */}
      <TextField
        label="Search Site Inventory"
        variant="outlined"
        fullWidth
        margin="normal"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {/*  Inventory Table */}
      <TableContainer
        component={Paper}
        sx={{ maxHeight: "600px", minHeight: "600px", overflowY: "auto" }}
      >
        <Table stickyHeader sx={{ tableLayout: "fixed", width: "100%" }}>
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
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ height: "200px" }}>
                  <CircularProgress />
                  <Typography
                    variant="body2"
                    color="textSecondary"
                    sx={{ marginTop: 1 }}
                  >
                    Loading inventory...
                  </Typography>
                </TableCell>
              </TableRow>
            ) : paginatedItems.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography variant="body2" color="textSecondary">
                    No matching items found.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              paginatedItems.map((inventory) => {
                const isDisabled =
                  isEmergencyOrder && orderItems.length >= maxEmergencyItems;

                return (
                  <TableRow key={inventory.id.itemID}>
                    <TableCell>{inventory.item.sku}</TableCell>
                    <TableCell>{inventory.item.name}</TableCell>
                    <TableCell>{inventory.item.caseSize}</TableCell>
                    <TableCell>{inventory.quantity}</TableCell>
                    <TableCell>{inventory.optimumThreshold}</TableCell>
                    <TableCell>
                      <Button
                        onClick={() => onAddItem(inventory.item)}
                        disabled={isDisabled}
                      >
                        {isDisabled ? "Limit Reached" : "Add Case +"}
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* ✅ Pagination Controls */}
      <TablePagination
        rowsPerPageOptions={[10, 25, 50, 100]}
        component="div"
        count={filteredItems.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={(event, newPage) => setPage(newPage)}
        onRowsPerPageChange={(event) => {
          setRowsPerPage(parseInt(event.target.value, 10));
          setPage(0);
        }}
      />
    </Box>
  );
};

export default InventoryList;
