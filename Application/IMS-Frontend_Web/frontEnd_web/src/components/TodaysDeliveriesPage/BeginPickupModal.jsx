import { useState, useEffect } from "react";
import {
  Modal,
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  Checkbox,
  Button,
  CircularProgress,
  TextField,
} from "@mui/material";
import { styled } from "@mui/system";

const SignatureInput = styled(TextField)({
  "& input": {
    fontFamily: "'Pacifico', cursive", // ✅ Script-like font
    fontSize: "1.5rem",
    textAlign: "center",
  },
});

const BeginPickupModal = ({ open, onClose, txnIds, user, onConfirmPickup }) => {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState([]);
  const [checkedItems, setCheckedItems] = useState({});
  const [driverSignature, setDriverSignature] = useState(""); // ✅ Track driver's input

  useEffect(() => {
    if (!open || txnIds.length === 0) return;

    setLoading(true);

    Promise.all(
      txnIds.map((txnId) =>
        fetch(`http://localhost:8080/api/orders/${txnId}/items`).then((res) =>
          res.json()
        )
      )
    )
      .then((data) => {
        const consolidatedItems = {};
        data.flat().forEach((item) => {
          const key = item.itemID.sku;
          if (!consolidatedItems[key]) {
            consolidatedItems[key] = {
              ...item,
              totalQuantity: item.quantity,
            };
          } else {
            consolidatedItems[key].totalQuantity += item.quantity;
          }
        });

        setItems(Object.values(consolidatedItems));
        setCheckedItems({});
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching order items:", err);
        setLoading(false);
      });
  }, [open, txnIds]);

  const handleCheck = (sku) => {
    setCheckedItems((prev) => ({
      ...prev,
      [sku]: !prev[sku],
    }));
  };

  const handleConfirmPickup = async () => {
    if (
      !items.every((item) => checkedItems[item.itemID.sku]) ||
      !driverSignature.trim()
    )
      return;

    try {
      setLoading(true);

      const decrementPayload = {
        siteID: 3,
        items: items.map((item) => ({
          itemID: item.itemID.id,
          quantity: item.totalQuantity,
        })),
      };

      const incrementPayload = {
        siteID: 9999,
        items: items.map((item) => ({
          itemID: item.itemID.id,
          quantity: item.totalQuantity,
        })),
      };

      await fetch("http://localhost:8080/api/inventory/decrement", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(decrementPayload),
      });

      await fetch("http://localhost:8080/api/inventory/increment", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(incrementPayload),
      });

      await Promise.all(
        txnIds.map((txnId) =>
          fetch(
            `http://localhost:8080/api/orders/${txnId}/update-status?status=IN TRANSIT&empUsername=${user.username}`,
            { method: "PUT" }
          )
        )
      );

      alert(
        `Pickup confirmed successfully!\nDriver Signature: ${driverSignature}`
      );
      onConfirmPickup();
      onClose();
    } catch (err) {
      console.error("[ERROR] Confirming pickup:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 500,
          bgcolor: "background.paper",
          boxShadow: 24,
          p: 4,
          borderRadius: 2,
        }}
      >
        <Typography variant="h6">Begin Pickup</Typography>

        {loading ? (
          <CircularProgress sx={{ marginTop: 2 }} />
        ) : (
          <Paper sx={{ maxHeight: 300, overflowY: "auto", marginTop: 2 }}>
            <List>
              {items.map((item) => (
                <ListItem key={item.itemID.sku} divider>
                  <Checkbox
                    checked={!!checkedItems[item.itemID.sku]}
                    onChange={() => handleCheck(item.itemID.sku)}
                  />
                  <ListItemText
                    primary={item.itemID.name}
                    secondary={`SKU: ${item.itemID.sku} | Quantity: ${item.totalQuantity}`}
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        {/* ✅ NEW: Fancy Driver Signature Input */}
        <Typography sx={{ marginTop: 2 }}>Driver Signature:</Typography>
        <SignatureInput
          variant="standard"
          fullWidth
          value={driverSignature}
          onChange={(e) => setDriverSignature(e.target.value)}
          placeholder="Sign here..."
        />

        <Box sx={{ display: "flex", justifyContent: "flex-end", marginTop: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleConfirmPickup}
            disabled={
              !items.every((item) => checkedItems[item.itemID.sku]) ||
              !driverSignature.trim()
            }
            sx={{ marginLeft: 2 }}
          >
            Confirm Pickup
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default BeginPickupModal;
