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
} from "@mui/material";

const InventoryList = ({
  availableItems,
  onAddItem,
  isEmergencyOrder,
  maxEmergencyItems,
  orderItems,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25); // Default: 25 rows per page
  const [loading, setLoading] = useState(true); // ✅ Loading state

  // Simulate a loading delay (replace this with actual API fetching logic)
  useEffect(() => {
    setLoading(true);
    const timer = setTimeout(() => {
      setLoading(false);
    }, 500); // Simulates a delay before rendering items
    return () => clearTimeout(timer);
  }, [availableItems]); // Re-run when availableItems changes

  // ✅ Filter items based on search input (by SKU or name)
  const filteredItems = useMemo(() => {
    return availableItems.filter(
      (inventory) =>
        inventory.item.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
        inventory.item.name.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [availableItems, searchQuery]);

  // ✅ Get only the rows for the current page
  const paginatedItems = filteredItems.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  // ✅ Handle pagination changes
  const handleChangePage = (event, newPage) => setPage(newPage);
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0); // Reset to first page when changing page size
  };

  return (
    <Box>
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
      <Box></Box>
      {/* ✅ Search Bar */}
      <TextField
        label="Search Site Inventory"
        variant="outlined"
        fullWidth
        margin="normal"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />
      {/* ✅ Inventory Table */}
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
              // ✅ Ensure table width remains the same by keeping row structure
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
                        {isDisabled ? "Limit Reached" : "Add Item +"}
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
        rowsPerPageOptions={[10, 25, 50, 100]} // Allows user to select page size
        component="div"
        count={filteredItems.length} // Total rows
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default InventoryList;
