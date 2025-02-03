import { useEffect, useState } from "react";
import axios from "axios";
import {
  Typography,
  Autocomplete,
  TextField,
  CircularProgress,
  Box,
} from "@mui/material";

const OrdersPage = ({ user }) => {
  const [sites, setSites] = useState([]);
  const [selectedSite, setSelectedSite] = useState(null);
  const [loading, setLoading] = useState(true);

  // Extract role names from user object
  const userRoles = user.roles.map((role) => role.posn.permissionLevel);
  const isWarehouseManager = userRoles.includes("Warehouse Manager");
  const isStoreManager = userRoles.includes("Store Manager");

  useEffect(() => {
    if (isWarehouseManager) {
      // Warehouse Managers can see all sites
      axios
        .get("http://localhost:8080/api/sites")
        .then((response) => {
          setSites(response.data);
          setLoading(false);
        })
        .catch((error) => {
          console.error("Error fetching sites:", error);
          setLoading(false);
        });
    } else if (isStoreManager) {
      // Store Managers can only see their assigned site
      setSites([user.site]);
      setLoading(false);
    } else {
      // Default: No site access
      setSites([]);
      setLoading(false);
    }
  }, [isWarehouseManager, isStoreManager, user.site]);

  return (
    <Box sx={{ maxWidth: 600, margin: "auto", padding: 3 }}>
      <Typography variant="h4" gutterBottom>
        Orders Management
      </Typography>

      {loading ? (
        <CircularProgress />
      ) : (
        <Autocomplete
          options={sites}
          getOptionLabel={(option) => option.siteName}
          value={selectedSite}
          onChange={(event, newValue) => setSelectedSite(newValue)}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Select Site"
              variant="outlined"
              fullWidth
            />
          )}
        />
      )}
    </Box>
  );
};

export default OrdersPage;
