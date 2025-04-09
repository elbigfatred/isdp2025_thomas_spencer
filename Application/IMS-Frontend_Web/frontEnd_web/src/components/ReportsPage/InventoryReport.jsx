import { useState, useEffect } from "react";
import {
  Box,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
} from "@mui/material";
import ReportViewer from "./ReportViewer";

const InventoryReport = () => {
  const [format, setFormat] = useState("pdf");
  const [siteId, setSiteId] = useState("");
  const [sortBy, setSortBy] = useState("item");
  const [sortOrder, setSortOrder] = useState("asc");
  const [reportUrl, setReportUrl] = useState(null);
  const [sites, setSites] = useState([]);

  useEffect(() => {
    const fetchSites = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/sites");
        const data = await res.json();
        setSites(data);
      } catch (err) {
        console.error("[ERROR] Failed to load sites:", err);
      }
    };
    fetchSites();
  }, []);

  const handleGenerate = async () => {
    const payload = {
      reportType: "inventory",
      format,
      ...(siteId && { siteId: Number(siteId) }),
      ...(sortBy && { sortBy }),
      ...(sortOrder && { sortOrder }),
    };

    try {
      const response = await fetch("http://127.0.0.1:5000/generate-report", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error("Failed to generate report");
      }

      const blob = await response.blob();
      const blobUrl = URL.createObjectURL(blob);
      setReportUrl(blobUrl);
    } catch (error) {
      console.error("[ERROR] Report generation failed:", error);
      alert("Failed to generate report.");
    }
  };

  const handleDownload = () => {
    if (!reportUrl) {
      alert("Please generate the report first.");
      return;
    }

    const link = document.createElement("a");
    link.href = reportUrl;
    const ext = format === "csv" ? "csv" : "pdf";
    link.download = `inventory_report.${ext}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Inventory Report Options
      </Typography>

      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2, mb: 2 }}>
        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Format</InputLabel>
          <Select value={format} onChange={(e) => setFormat(e.target.value)}>
            <MenuItem value="pdf">PDF</MenuItem>
            <MenuItem value="csv">CSV</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Site</InputLabel>
          <Select value={siteId} onChange={(e) => setSiteId(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            {sites.map((site) => (
              <MenuItem key={site.id} value={site.id}>
                {site.siteName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 160 }}>
          <InputLabel>Sort By</InputLabel>
          <Select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <MenuItem value="item">Item</MenuItem>
            <MenuItem value="quantity">Quantity</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 160 }}>
          <InputLabel>Sort Order</InputLabel>
          <Select
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value)}
          >
            <MenuItem value="asc">Ascending</MenuItem>
            <MenuItem value="desc">Descending</MenuItem>
          </Select>
        </FormControl>
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

export default InventoryReport;
