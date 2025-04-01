import React, { useState } from "react";
import {
  Tabs,
  Tab,
  Box,
  Typography,
  Tooltip,
  IconButton,
  Grid,
  Divider,
} from "@mui/material";
import UsersReport from "./UsersReport";
import InventoryReport from "./InventoryReport";
import BackordersReport from "./BackordersReport";
import DeliveryReport from "./DeliveryReport";
import EmergencyOrdersReport from "./EmergencyOrdersReport";
import OrdersReport from "./OrdersReport";
import ShippingReceiptReport from "./ShippingReceiptReport";
import StoreOrderReport from "./StoreOrderReport";
import SupplierOrderReport from "./SupplierOrderReport";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon

const ReportsDashboard = () => {
  const [activeTab, setActiveTab] = useState(0);

  const handleChange = (event, newValue) => {
    console.log("Selected tab:", newValue);
    setActiveTab(newValue);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Reports Dashboard
        <Tooltip
          title="The reports tab can be used to view various reports; click any tab and select applicable options to generate a report."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Typography>
      <Typography variant="h6" gutterBottom>
        Select a report to view:
      </Typography>

      <Grid container spacing={1} direction="row" alignItems="stretch">
        <Grid item xs={12}>
          <Tabs
            value={activeTab}
            onChange={handleChange}
            variant="scrollable"
            scrollButtons="auto"
          >
            <Tab
              label={
                <Tooltip title="View delivery details for each day, including routes, mileage, and weight for each delivery.">
                  <span>Delivery Report</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Displays a list of store orders used for record keeping and tracking.">
                  <span>Store Order</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Track shipping receipts for each order, helping to monitor deliveries.">
                  <span>Shipping Receipt</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Inventory overview, sortable by store, to manage stock levels and monitor items.">
                  <span>Inventory Report</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Order report sorted by stores, summarizing all transactions and their details.">
                  <span>Orders Report</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Emergency orders list, sortable by store, helping to manage urgent needs.">
                  <span>Emergency Orders</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="User report, sortable by role and site, to manage user data and access.">
                  <span>Users Report</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Backorders list, sortable by store, to monitor products that are on backorder.">
                  <span>Backorders</span>
                </Tooltip>
              }
            />
            <Divider orientation="vertical" flexItem sx={{ marginX: "8px" }} />

            <Tab
              label={
                <Tooltip title="Supplier orders report, used to track and manage orders placed to suppliers.">
                  <span>Supplier Order</span>
                </Tooltip>
              }
            />
          </Tabs>
        </Grid>
      </Grid>

      {activeTab === 0 && <DeliveryReport />}
      {activeTab === 2 && <StoreOrderReport />}
      {activeTab === 4 && <ShippingReceiptReport />}
      {activeTab === 6 && <InventoryReport />}
      {activeTab === 8 && <OrdersReport />}
      {activeTab === 10 && <EmergencyOrdersReport />}
      {activeTab === 12 && <UsersReport />}
      {activeTab === 14 && <BackordersReport />}
      {activeTab === 16 && <SupplierOrderReport />}
    </Box>
  );
};

export default ReportsDashboard;
