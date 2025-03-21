import { useState, useEffect } from "react";
import {
  Box,
  Typography,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Paper,
  TextField,
  Button,
  IconButton,
  Tooltip,
} from "@mui/material";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { parseISO, format, addDays } from "date-fns";
import AssignDeliveryModal from "./AssignDeliveryModal";
import OrderDetailsModal from "./OrderDetailsModal";

const DeliveriesDashboard = ({ user }) => {
  const [deliveries, setDeliveries] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [startDate, setStartDate] = useState(format(new Date(), "yyyy-MM-dd"));
  const [assignDeliveryModal, setAssignDeliveryModal] = useState(false);
  const [selectedOrders, setSelectedOrders] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [estimatedDistanceCost, setEstimatedDistanceCost] = useState(0);
  const [estimatedTimeCost, setEstimatedTimeCost] = useState(0);
  const [estimatedCost, setEstimatedCost] = useState(0);
  const [loadingAssignment, setLoadingAssignment] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderDetailsOpen, setOrderDetailsOpen] = useState(false);

  useEffect(() => {
    fetch("http://localhost:8080/api/delivery/upcoming")
      .then((res) => res.json())
      .then((data) => {
        console.log("Upcoming TXNs:", data);
        setDeliveries(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching deliveries:", err);
        setLoading(false);
      });

    fetch("http://localhost:8080/api/delivery/availableVehicles")
      .then((res) => res.json())
      .then((data) => {
        console.log("Available Vehicles:", data);
        setVehicles(data);
      })
      .catch((err) => console.error("Error fetching vehicles:", err));
  }, []);

  // Function to determine the best vehicle based on weight
  const getBestVehicle = (weight) => {
    if (weight === 0) return null;

    const sortedVehicles = [...vehicles].sort(
      (a, b) => a.maxWeight - b.maxWeight
    );
    return (
      sortedVehicles.find((vehicle) => vehicle.maxWeight >= weight) ||
      "OVERSIZED"
    );
  };

  // Handle Assign Delivery
  const handleAssignDelivery = (orders) => {
    console.log("ORDERS", orders);
    if (orders.length === 0) return;

    //  Calculate Total Weight
    const totalWeight = orders.reduce(
      (sum, order) => sum + order.totalWeight,
      0
    );

    console.log("[DEBUG] Total Weight:", totalWeight);
    console.log("[DEBUG] Vehicles:", vehicles);
    let sortedVehicles = vehicles.sort((a, b) => a.maxWeight - b.maxWeight);
    console.log(sortedVehicles);

    //  Determine Best Vehicle; smallest that will work
    const bestVehicle = sortedVehicles.find(
      (v) => v.maxWeight >= totalWeight || null
    );

    //  Calculate Distance Cost (Sum of each site's distance * cost per km)
    const totalDistanceCost = orders.reduce(
      (sum, order) =>
        sum +
        order.txn.siteIDTo.distanceFromWH *
          (bestVehicle ? bestVehicle.costPerKm : 0),
      0
    );

    const totalHourlyCost = bestVehicle ? bestVehicle.HourlyTruckCost * 2 : 0;
    const totalCost = totalDistanceCost + totalHourlyCost;

    setEstimatedDistanceCost(totalDistanceCost);
    setEstimatedTimeCost(totalHourlyCost);

    //  Set State for Modal
    setSelectedOrders(orders);
    setSelectedVehicle(bestVehicle);
    setEstimatedCost(totalDistanceCost + totalHourlyCost);
    setAssignDeliveryModal(true);
  };

  // Confirm Assignment
  const confirmAssignDelivery = () => {
    setLoadingAssignment(true);
    const txnIds = selectedOrders.map((order) => order.txn.id);

    fetch(
      "http://localhost:8080/api/delivery/assignDelivery?empUsername=" +
        user.username,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(txnIds),
      }
    )
      .then((res) =>
        res.ok ? res.json() : Promise.reject("Failed to assign delivery")
      )
      .then(() => {
        setAssignDeliveryModal(false);
        setLoadingAssignment(false);
        fetch("http://localhost:8080/api/delivery/upcoming")
          .then((res) => res.json())
          .then((data) => setDeliveries(data));
      })
      .catch((err) => {
        console.error("Error assigning delivery:", err);
        setLoadingAssignment(false);
      });
  };

  // Group deliveries
  const groupedDeliveries = (() => {
    const selectedStart = parseISO(startDate);
    const deliveriesByDay = {};

    for (let i = 0; i < 7; i++) {
      const date = addDays(selectedStart, i);
      const formattedDate = format(date, "yyyy-MM-dd");

      deliveriesByDay[formattedDate] = {
        dateLabel: format(date, "EEEE, MMMM do yyyy"),
        orders: [],
        totalItems: 0,
        totalWeight: 0,
      };
    }

    deliveries.forEach((delivery) => {
      const shipDate = delivery.txn.shipDate.split("T")[0];
      if (deliveriesByDay[shipDate]) {
        deliveriesByDay[shipDate].orders.push(delivery);
        deliveriesByDay[shipDate].totalItems += delivery.totalItems;
        deliveriesByDay[shipDate].totalWeight += parseFloat(
          delivery.totalWeight
        );
      }
    });

    return Object.values(deliveriesByDay);
  })();

  const fetchDeliveries = async () => {
    setLoading(true);
    fetch("http://localhost:8080/api/delivery/upcoming")
      .then((res) => res.json())
      .then((data) => {
        console.log("Upcoming TXNs:", data);
        setDeliveries(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching deliveries:", err);
        setLoading(false);
      });
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Deliveries Dashboard
        <Tooltip
          title="Review daily orders and assign a delivery truck to accommodate the order(s)."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Typography>

      <Button
        variant="contained"
        color="secondary"
        sx={{ marginBottom: 2 }}
        onClick={fetchDeliveries}
        disabled={loading}
      >
        {loading ? "Refreshing..." : "Refresh Orders"}
      </Button>

      {/* Start Date Selector */}
      <Box sx={{ display: "flex", gap: 2, marginBottom: 2 }}>
        <TextField
          label="Week Beginning"
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
        />
      </Box>

      {/* Transactions Needing Delivery */}
      {!loading &&
        deliveries.length > 0 &&
        (() => {
          // Group transactions by ship date
          const groupedByDate = deliveries
            .filter(
              (delivery) =>
                (delivery.txn.txnStatus.statusName === "ASSEMBLED" ||
                  delivery.txn.txnStatus.statusName === "ASSEMBLING" ||
                  delivery.txn.txnStatus.statusName === "RECEIVED") &&
                !delivery.txn.deliveryID // Exclude assigned transactions
            )
            .reduce((acc, delivery) => {
              const shipDate = delivery.txn.shipDate.split("T")[0];
              if (!acc[shipDate]) acc[shipDate] = [];
              acc[shipDate].push(delivery.txn.id);
              return acc;
            }, {});

          const unassignedDaysCount = Object.keys(groupedByDate).length;

          return unassignedDaysCount > 0 ? (
            <Box
              sx={{
                padding: 2,
                marginBottom: 2,
                borderRadius: "4px",
                backgroundColor: "#222", // Subtle background for visibility
                color: "#fff",
              }}
            >
              <Typography
                variant="h6"
                sx={{ fontWeight: "bold", marginBottom: 1 }}
              >
                {unassignedDaysCount} Day(s) with Unassigned Orders:
              </Typography>

              {Object.entries(groupedByDate).map(([date, txnIds]) => (
                <Box
                  key={date}
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "6px 12px",
                    borderBottom: "1px solid #555",
                  }}
                >
                  <Typography variant="body2">
                    {date} | Order ID(s): {txnIds.join(", ")}
                  </Typography>

                  <Button
                    size="small"
                    variant="outlined"
                    sx={{
                      color: "#fff",
                      borderColor: "#888",
                      fontSize: "0.75rem",
                    }}
                    onClick={() => setStartDate(date)}
                  >
                    Jump to Date
                  </Button>
                </Box>
              ))}
            </Box>
          ) : (
            <Typography
              sx={{
                marginTop: 1,
                fontStyle: "italic",
                color: "#bbb",
                fontSize: "0.85rem",
              }}
            ></Typography>
          );
        })()}

      {loading ? (
        <CircularProgress />
      ) : (
        groupedDeliveries.map((day, index) => {
          const bestVehicle = getBestVehicle(day.totalWeight);
          const allOrdersAssembled = day.orders.every(
            (order) =>
              order.txn.txnStatus.statusName === "ASSEMBLED" ||
              order.txn.txnStatus.statusName === "ASSEMBLING" ||
              order.txn.txnStatus.statusName === "RECEIVED"
          );
          const hasAssignedDelivery = day.orders.some(
            (order) => order.txn.deliveryID !== null
          );
          const estimatedDistanceCost = day.orders.reduce(
            (sum, order) =>
              sum +
              order.txn.siteIDTo.distanceFromWH *
                (bestVehicle ? bestVehicle.costPerKm : 0),
            0
          );

          return (
            <Accordion key={index}>
              <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                sx={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                }}
              >
                {/*  DATE Label - Always First */}
                <Typography sx={{ fontWeight: "bold", flexShrink: 0 }}>
                  {day.dateLabel}
                </Typography>

                {/*  Flexible Spacer to Align Button Properly */}
                <Box sx={{ flexGrow: 1 }} />

                {/*  Assign Delivery Button (If Needed) */}
                {!hasAssignedDelivery && day.orders.length > 0 && (
                  <Button
                    variant="contained"
                    color="primary"
                    sx={{ marginRight: 2 }}
                    onClick={(e) => {
                      e.stopPropagation(); //  Prevents the accordion from toggling
                      handleAssignDelivery(day.orders);
                    }}
                  >
                    Assign Delivery
                  </Button>
                )}

                {/*  Show Assigned Vehicle if Delivery Exists */}
                {hasAssignedDelivery && (
                  <Typography
                    sx={{ marginRight: 2, fontStyle: "italic", color: "gray" }}
                  >
                    Assigned Vehicle:{" "}
                    {day.orders[0].txn.deliveryID.vehicle.vehicleType} |
                    Estimated Total Delivery Cost: $
                    {day.orders[0].txn.deliveryID.distanceCost.toFixed(2)}
                  </Typography>
                )}

                {/*  Order Summary (Always Last) */}
                <Typography sx={{ fontWeight: "bold" }}>
                  {day.orders.length} Orders | {day.totalItems} Items |{" "}
                  {day.totalWeight} kg
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                {day.orders.length === 0 ? (
                  <Typography>No orders to be delivered.</Typography>
                ) : (
                  day.orders.map((delivery) => (
                    <Paper
                      key={delivery.txn.id}
                      sx={{ padding: 2, marginBottom: 1 }}
                    >
                      <Typography variant="h6">
                        Order #{delivery.txn.id} -{" "}
                        {delivery.txn.siteIDTo.siteName}
                      </Typography>
                      <Typography>Total SKUs: {delivery.totalItems}</Typography>
                      <Typography>
                        Total Weight: {delivery.totalWeight} kg
                      </Typography>
                      <Button
                        variant="contained"
                        color="primary"
                        onClick={(e) => {
                          e.stopPropagation(); //  Prevents the accordion from toggling
                          setSelectedOrder(delivery.txn);
                          setOrderDetailsOpen(true);
                        }}
                      >
                        Order Details
                      </Button>
                    </Paper>
                  ))
                )}
              </AccordionDetails>
            </Accordion>
          );
        })
      )}

      {/* Assign Delivery Modal */}
      <AssignDeliveryModal
        open={assignDeliveryModal}
        onClose={() => setAssignDeliveryModal(false)}
        onConfirm={confirmAssignDelivery}
        orders={selectedOrders}
        vehicle={selectedVehicle}
        estimatedCost={estimatedCost}
        estimatedTimeCost={estimatedTimeCost}
        estimatedDistanceCost={estimatedDistanceCost}
        loading={loadingAssignment}
      />
      <OrderDetailsModal
        open={orderDetailsOpen}
        onClose={() => setOrderDetailsOpen(false)}
        order={selectedOrder}
      />
    </Box>
  );
};

export default DeliveriesDashboard;
