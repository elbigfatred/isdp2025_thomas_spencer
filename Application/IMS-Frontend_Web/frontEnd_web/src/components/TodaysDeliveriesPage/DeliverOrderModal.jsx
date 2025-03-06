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

const DeliverOrderModal = ({ open, onClose, order, user, onRefresh }) => {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState([]);
  const [checkedItems, setCheckedItems] = useState({});
  const [signature, setSignature] = useState("");

  useEffect(() => {
    if (!open || !order) return;

    setLoading(true);

    fetch(`http://localhost:8080/api/orders/${order.id}/items`)
      .then((res) => res.json())
      .then((data) => {
        setItems(data);
        setCheckedItems({});
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching order items:", err);
        setLoading(false);
      });
  }, [open, order]);

  const handleCheck = (sku) => {
    setCheckedItems((prev) => ({
      ...prev,
      [sku]: !prev[sku],
    }));
  };

  const handleConfirmDelivery = async () => {
    if (!items.every((item) => checkedItems[item.itemID.sku]) || !signature)
      return;

    try {
      setLoading(true);

      const updateStatusUrl = `http://localhost:8080/api/orders/${order.id}/update-status?status=DELIVERED&empUsername=${user.username}`;
      const statusResponse = await fetch(updateStatusUrl, { method: "PUT" });

      if (!statusResponse.ok) {
        throw new Error(`Order status update failed for TXN ${order.id}`);
      }

      alert("Order delivered successfully!");

      onRefresh();
      onClose();
    } catch (err) {
      console.error("[ERROR] Confirming delivery:", err);
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
        <Typography variant="h6">Deliver Order</Typography>

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
                    secondary={`SKU: ${item.itemID.sku} | Quantity: ${item.quantity}`}
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        )}

        {/* ✅ Signature Input */}
        <Typography sx={{ marginTop: 2 }}>Store Manager Signature:</Typography>
        <SignatureInput
          fullWidth
          label="Store Manager Signature"
          value={signature}
          onChange={(e) => setSignature(e.target.value)}
          sx={{ marginTop: 2 }}
          placeholder="Sign here..."
        />

        <Box sx={{ display: "flex", justifyContent: "flex-end", marginTop: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleConfirmDelivery}
            disabled={
              !items.every((item) => checkedItems[item.itemID.sku]) ||
              !signature
            }
            sx={{ marginLeft: 2 }}
          >
            Confirm Delivered
          </Button>
        </Box>
      </Box>
    </Modal>
  );
};

export default DeliverOrderModal;
