import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  CircularProgress,
  TablePagination,
  TextField,
} from "@mui/material";

import ItemDetailsModal from "./ItemDetailsModal";

const InventoryList = ({ activeSite, addToCart }) => {
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [selectedItem, setSelectedItem] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    if (!activeSite) return;
    setLoading(true);

    fetch(`http://localhost:8080/api/inventory/site/${activeSite.id}`)
      .then((res) => res.json())
      .then((data) => {
        console.log("Fetched Inventory:", data);
        setInventory(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching inventory:", err);
        setLoading(false);
      });
  }, [activeSite]);

  // Filter inventory by search query
  const filteredInventory = inventory.filter((item) =>
    item.item.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleOpenDetails = (item) => {
    setSelectedItem(item);
    setModalOpen(true);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Available Inventory
      </Typography>

      <TextField
        fullWidth
        label="Search Inventory..."
        variant="outlined"
        size="small"
        sx={{ marginBottom: 2 }}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {inventory.length === 0 ? (
        <Box display="flex" justifyContent="center">
          <CircularProgress />
        </Box>
      ) : (
        <>
          <TableContainer
            component={Paper}
            sx={{ maxHeight: "600px", overflowY: "auto" }} // ✅ Fixed height & scrolling
          >
            <Table stickyHeader>
              {" "}
              {/* ✅ Sticky header */}
              <TableHead>
                <TableRow>
                  <TableCell>Item Name</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>Stock</TableCell>
                  <TableCell>Price</TableCell>
                  <TableCell>Details</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredInventory
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((item) => (
                    <TableRow key={item.id.itemID}>
                      <TableCell>{item.item.name}</TableCell>
                      <TableCell>{item.item.sku}</TableCell>
                      <TableCell>{item.quantity}</TableCell>
                      <TableCell>${item.item.retailPrice.toFixed(2)}</TableCell>
                      <TableCell>
                        <Button
                          variant="outlined"
                          size="small"
                          onClick={() => handleOpenDetails(item)}
                          sx={{ marginRight: 1 }}
                        >
                          View Details
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="contained"
                          size="small"
                          onClick={() => addToCart(item)}
                          disabled={item.quantity === 0}
                        >
                          Add to Cart
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* ✅ Pagination */}
          <TablePagination
            component="div"
            count={filteredInventory.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(event, newPage) => setPage(newPage)}
            onRowsPerPageChange={(event) =>
              setRowsPerPage(parseInt(event.target.value, 10))
            }
          />
        </>
      )}

      {/* Item Details Modal */}
      {selectedItem && (
        <ItemDetailsModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          item={selectedItem.item}
        />
      )}
    </Box>
  );
};

export default InventoryList;
