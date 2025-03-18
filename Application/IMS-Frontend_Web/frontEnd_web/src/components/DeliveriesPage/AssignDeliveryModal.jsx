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
  estimatedTimeCost,
  estimatedDistanceCost,
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
              <strong>Vehicle:</strong>{" "}
              {vehicle?.vehicleType ||
                "Order is too large to fit on any available vehicle"}
            </Typography>
            {vehicle ? (
              <Typography>
                <Typography>
                  <strong>Estimated Distance Cost:</strong> $
                  {estimatedDistanceCost.toFixed(2)}
                </Typography>
                <Typography>
                  <strong>Estimated Time Cost (2HR Minumum):</strong> $
                  {estimatedTimeCost.toFixed(2)}
                </Typography>
                <strong>Estimated Total Cost:</strong> $
                {estimatedCost.toFixed(2)}
              </Typography>
            ) : null}
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button
          onClick={onConfirm}
          color="primary"
          disabled={loading || !vehicle}
        >
          Confirm
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AssignDeliveryModal;
