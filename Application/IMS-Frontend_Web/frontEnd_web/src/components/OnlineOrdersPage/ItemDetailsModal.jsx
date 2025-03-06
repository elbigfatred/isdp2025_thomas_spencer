import { useState, useEffect } from "react";
import {
  Modal,
  Box,
  Typography,
  Button,
  CircularProgress,
} from "@mui/material";

const ItemDetailsModal = ({ open, onClose, item }) => {
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [imageSrc, setImageSrc] = useState(null);

  useEffect(() => {
    if (!item) return;

    // Fetch item details
    setLoading(true);
    fetch(`http://localhost:8080/api/items/${item.id}`)
      .then((res) => res.json())
      .then((data) => {
        console.log("Item Details:", data);
        setDetails(data);
        setLoading(false);

        // Fetch item image if available
        if (data.imageLocation) {
          setImageSrc(`http://localhost:8080/api/items/image/${item.id}`);
        } else {
          setImageSrc(null); // No image found
        }
      })
      .catch((err) => {
        console.error("Error fetching item details:", err);
        setLoading(false);
      });
  }, [item]);

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
        {loading ? (
          <CircularProgress />
        ) : details ? (
          <>
            {/* Item Image */}
            {imageSrc ? (
              <Box
                component="img"
                src={imageSrc}
                alt={details.name}
                sx={{
                  width: "100%",
                  height: "auto",
                  borderRadius: 1,
                  marginBottom: 2,
                }}
              />
            ) : (
              <Typography variant="body2" sx={{ textAlign: "center" }}>
                No image available
              </Typography>
            )}

            {/* Item Details */}
            <Typography variant="h6">{details.name}</Typography>
            <Typography variant="body1">SKU: {details.sku}</Typography>
            <Typography variant="body2">
              <strong>Category:</strong> {details.category.categoryName}
            </Typography>
            <Typography variant="body2">
              <strong>Description:</strong> {details.description || "N/A"}
            </Typography>
            <Typography variant="body2">
              <strong>Weight:</strong> {details.weight} kg
            </Typography>
            <Typography variant="body2">
              <strong>Retail Price:</strong> ${details.retailPrice.toFixed(2)}
            </Typography>

            {/* Supplier Info */}
            {details.supplier && (
              <>
                <Typography variant="subtitle1" sx={{ marginTop: 2 }}>
                  Supplier Info
                </Typography>
                <Typography variant="body2">
                  {details.supplier.name} ({details.supplier.city},{" "}
                  {details.supplier.province.provinceName})
                </Typography>
              </>
            )}

            {/* Close Button */}
            <Box sx={{ marginTop: 3, textAlign: "right" }}>
              <Button variant="contained" onClick={onClose}>
                Close
              </Button>
            </Box>
          </>
        ) : (
          <Typography>Error loading item details.</Typography>
        )}
      </Box>
    </Modal>
  );
};

export default ItemDetailsModal;
