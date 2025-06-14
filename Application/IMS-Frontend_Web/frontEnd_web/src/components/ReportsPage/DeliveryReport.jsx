import { useState } from "react";
import {
  Box,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  TextField,
} from "@mui/material";
import ReportViewer from "./ReportViewer";

const DeliveryReport = () => {
  const [format, setFormat] = useState("pdf");
  const [deliveryDate, setDeliveryDate] = useState("2025-03-26");
  const [reportUrl, setReportUrl] = useState(null);

  const handleGenerate = async () => {
    const payload = {
      reportType: "delivery_report",
      deliveryDate,
      ...(format && { format }),
    };

    try {
      const response = await fetch("http://127.0.0.1:5000/generate-report", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Failed to generate report");

      const blob = await response.blob();
      const blobUrl = URL.createObjectURL(blob);
      setReportUrl(blobUrl);
    } catch (error) {
      console.error("[ERROR] Report generation failed:", error);
      alert("Failed to generate report.");
    }
  };

  const [year, month, day] = deliveryDate.split("-");
  const localDate = new Date(year, month - 1, day); // month is 0-based
  const formattedDeliveryDate = localDate.toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  const handleDownload = () => {
    if (!reportUrl) {
      alert("Please generate the report first.");
      return;
    }

    const link = document.createElement("a");
    link.href = reportUrl;
    const fileExtension = format === "csv" ? "csv" : "pdf";
    link.download = `delivery_report.${fileExtension}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Delivery Report Options
      </Typography>

      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2, mb: 2 }}>
        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Format</InputLabel>
          <Select value={format} onChange={(e) => setFormat(e.target.value)}>
            <MenuItem value="pdf">PDF</MenuItem>
            <MenuItem value="csv">CSV</MenuItem>
          </Select>
        </FormControl>

        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <TextField
            label="Delivery Date"
            type="date"
            value={deliveryDate}
            onChange={(e) => setDeliveryDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
          <Typography variant="body2" color="text.secondary">
            {formattedDeliveryDate}
          </Typography>
        </Box>
      </Box>

      <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
        <Button variant="contained" onClick={handleGenerate}>
          Generate Report
        </Button>
        <Button
          variant="outlined"
          onClick={handleDownload}
          disabled={!reportUrl}
        >
          Download Report
        </Button>
      </Box>

      <ReportViewer reportUrl={reportUrl} />
    </Box>
  );
};

export default DeliveryReport;
