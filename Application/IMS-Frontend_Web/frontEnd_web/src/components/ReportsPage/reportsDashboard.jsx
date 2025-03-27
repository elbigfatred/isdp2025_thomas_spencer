import React, { useState } from "react";
import { Tabs, Tab, Box, Typography, Tooltip, IconButton } from "@mui/material";
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
      <Tabs value={activeTab} onChange={handleChange}>
        <Tab label="Users Report" />
        <Tab label="Inventory Report" />
        <Tab label="Backorders Report" />
        <Tab label="Delivery Report" />
        <Tab label="Emergency Orders Report" />
        <Tab label="Orders Report" />
        <Tab label="Shipping Receipt Report" />
        <Tab label="Store Order Report" />
        <Tab label="Supplier Order Report" />
      </Tabs>
      {activeTab === 0 && <UsersReport />}
      {activeTab === 1 && <InventoryReport />}
      {activeTab === 2 && <BackordersReport />}
      {activeTab === 3 && <DeliveryReport />}
      {activeTab === 4 && <EmergencyOrdersReport />}
      {activeTab === 5 && <OrdersReport />}
      {activeTab === 6 && <ShippingReceiptReport />}
      {activeTab === 7 && <StoreOrderReport />}
      {activeTab === 8 && <SupplierOrderReport />}
    </Box>
  );
};

export default ReportsDashboard;
