import { useState } from "react";
import {
  Box,
  Typography,
  Button,
  Paper,
  IconButton,
  Tooltip,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon

import ReviewStoreOnlineOrders from "./ReviewOnlineStoreOrders";
import SearchForOnlineOrders from "./SearchForOnlineOrders";
import OnlineOrderManagerPanel from "./OnlineOrderManagerPanel.jsx";

const OnlineOrdersDashboard = ({ user }) => {
  // ✅ Determine the default tab based on the user's role
  const getDefaultTab = () => {
    if (["Administrator", "Online Customer"].includes(user.mainrole)) {
      return "create"; // Default to "Create Order" if available
    } else if (
      ["Administrator", "Store Manager", "Store Worker"].includes(user.mainrole)
    ) {
      return "review"; // Otherwise, default to "Review Orders"
    }
    return ""; // Fallback in case of an unexpected role
  };

  const [activeTab, setActiveTab] = useState(getDefaultTab());

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Online Orders Dashboard
        <Tooltip
          title="The online orders tab can be used to create new orders, and review orders."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Typography>

      <Box sx={{ display: "flex", gap: 2, marginBottom: 2 }}>
        {/* ✅ Create Order & Search Orders: Shown to "Administrator" or "Online Customer" */}
        {["Administrator", "Online Customer"].includes(user.mainrole) && (
          <>
            <Button
              variant={activeTab === "create" ? "contained" : "outlined"}
              onClick={() => setActiveTab("create")}
            >
              Create Order
            </Button>
            <Button
              variant={activeTab === "search" ? "contained" : "outlined"}
              onClick={() => setActiveTab("search")}
            >
              Search Orders
            </Button>
          </>
        )}

        {/* ✅ Review Orders: Shown to "Administrator", "Store Manager", and "Store Worker" */}
        {["Administrator", "Store Manager", "Store Worker"].includes(
          user.mainrole
        ) && (
          <Button
            variant={activeTab === "review" ? "contained" : "outlined"}
            onClick={() => setActiveTab("review")}
          >
            Review Orders
          </Button>
        )}
      </Box>

      <Paper sx={{ padding: 3, marginTop: 2 }}>
        {activeTab === "create" && <OnlineOrderManagerPanel user={user} />}
        {activeTab === "search" && <SearchForOnlineOrders />}
        {activeTab === "review" && <ReviewStoreOnlineOrders user={user} />}
      </Paper>
    </Box>
  );
};

export default OnlineOrdersDashboard;
