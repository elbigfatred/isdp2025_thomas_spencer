import { useState } from "react";
import { Box, Typography, Button, Paper } from "@mui/material";
import ReviewStoreOnlineOrders from "./ReviewOnlineStoreOrders";
import SearchForOnlineOrders from "./SearchForOnlineOrders";
import OnlineOrderManagerPanel from "./OnlineOrderManagerPanel.jsx";

const OnlineOrdersDashboard = ({ user }) => {
  const [activeTab, setActiveTab] = useState("create"); // Default to "create"

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Online Orders Dashboard
      </Typography>

      {/* Tab Selection */}
      <Box sx={{ display: "flex", gap: 2, marginBottom: 2 }}>
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
        {user.roles.some((role) =>
          ["Administrator", "Store Manager", "Store Worker"].includes(
            role.posn.permissionLevel
          )
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
