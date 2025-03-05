import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  Button,
  CircularProgress,
} from "@mui/material";

const AssignDeliveryModal = ({
  open,
  onClose,
  onConfirm,
  orders,
  vehicle,
  estimatedCost,
  loading,
}) => {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Confirm Delivery Assignment</DialogTitle>
      <DialogContent>
        {loading ? (
          <CircularProgress />
        ) : (
          <>
            <Typography>
              <strong>Order ID/s:</strong>{" "}
              {orders.map((o) => o.txn.id).join(", ")}
            </Typography>
            <Typography>
              <strong>Vehicle:</strong> {vehicle?.vehicleType}
            </Typography>
            <Typography>
              <strong>Estimated Distance Cost:</strong> $
              {estimatedCost.toFixed(2)}
            </Typography>
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button onClick={onConfirm} color="primary" disabled={loading}>
          Confirm
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AssignDeliveryModal;
