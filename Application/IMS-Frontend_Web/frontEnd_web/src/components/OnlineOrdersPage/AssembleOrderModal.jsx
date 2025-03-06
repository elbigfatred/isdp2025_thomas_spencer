import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Typography,
  Checkbox,
} from "@mui/material";

const AssembleOrderModal = ({
  open,
  onClose,
  order,
  user,
  onAssemblyComplete,
  inventory,
}) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [assembledItems, setAssembledItems] = useState({});

  useEffect(() => {
    if (!order) return;

    setLoading(true);

    // ✅ Fetch order items (Only fetch items, since inventory is already loaded!)
    fetch(`http://localhost:8080/api/orders/${order.id}/items`)
      .then((res) => res.json())
      .then((data) => {
        setItems(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching order data:", err);
        setLoading(false);
      });
  }, [order]);

  // ✅ Handle checkbox toggling
  const handleCheckboxChange = (itemId) => {
    setAssembledItems((prev) => ({
      ...prev,
      [itemId]: !prev[itemId],
    }));
  };

  // ✅ Check if all items are marked as assembled
  const allItemsAssembled =
    items.length > 0 &&
    Object.values(assembledItems).every((checked) => checked);

  // ✅ Function to complete order assembly
  const handleCompleteAssembly = () => {
    const siteID = order.siteIDTo.id;
    const inventoryUpdateRequest = {
      siteID: siteID,
      items: items.map((item) => ({
        itemID: item.itemID.id,
        quantity: item.quantity,
      })),
    };

    console.log(
      "[DEBUG] Sending Inventory Decrement Request:",
      inventoryUpdateRequest
    );

    fetch(`http://localhost:8080/api/inventory/decrement`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(inventoryUpdateRequest),
    })
      .then((res) => {
        console.log("[DEBUG] Inventory Decrement Response Status:", res.status);
        return res.text(); // Read response as text to handle unexpected formats
      })
      .then((text) => {
        try {
          const data = JSON.parse(text); // Try to parse JSON
          console.log("[DEBUG] Parsed Inventory Decrement Response:", data);
        } catch (err) {
          console.warn("[WARNING] Response is not JSON:", text);
        }

        console.log(
          "[DEBUG] Inventory decremented successfully. Proceeding to update status..."
        );

        // ✅ Prepare order status update request
        const updateStatusURL = `http://localhost:8080/api/orders/${order.id}/update-status?status=ASSEMBLED&empUsername=${user.username}`;

        console.log(
          "[DEBUG] Sending Order Status Update Request to:",
          updateStatusURL
        );

        return fetch(updateStatusURL, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
        });
      })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to update order status");
        return res.json();
      })
      .then(() => {
        console.log("[DEBUG] Order status updated to ASSEMBLED.");
        onAssemblyComplete(order.id);
        onClose();
      })
      .catch((err) => console.error("[ERROR] Assembling order:", err));
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Assemble Order {order?.barCode}</DialogTitle>
      <DialogContent>
        {loading ? (
          <Typography>Loading order details...</Typography>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>Ordered Qty</TableCell>
                <TableCell>Available Stock</TableCell>
                <TableCell>Assembled</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((item) => {
                const stock = inventory[item.itemID.id] || 0;
                const isShort = stock < item.quantity;

                return (
                  <TableRow key={item.itemID.id}>
                    <TableCell>{item.itemID.name}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{stock}</TableCell>
                    <TableCell>
                      <Checkbox
                        checked={assembledItems[item.itemID.id]}
                        onChange={() => handleCheckboxChange(item.itemID.id)}
                        disabled={isShort} // ✅ Disable checkbox if insufficient stock
                      />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="secondary">
          Close
        </Button>
        <Button
          variant="contained"
          color="primary"
          onClick={handleCompleteAssembly}
          disabled={!allItemsAssembled} // ✅ Enable only when all are checked
        >
          Complete Assembly
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AssembleOrderModal;
