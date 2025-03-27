import { useState, useEffect } from "react";
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

const OrdersReport = () => {
  const [format, setFormat] = useState("pdf");
  const [selectedSiteId, setSelectedSiteId] = useState("");
  const [txnType, setTxnType] = useState("");
  const [startDate, setStartDate] = useState("2025-01-01");
  const [endDate, setEndDate] = useState("2025-12-31");
  const [reportUrl, setReportUrl] = useState(null);
  const [sites, setSites] = useState([]);
  const [txnTypes, setTxnTypes] = useState([]);

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

    const fetchTxnTypes = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/txns/txnTypes");
        const data = await res.json();
        setTxnTypes(data);
      } catch (err) {
        console.error("[ERROR] Failed to load transaction types:", err);
      }
    };

    fetchSites();
    fetchTxnTypes();
  }, []);

  const handleGenerate = async () => {
    const payload = {
      reportType: "orders",
      format,
      ...(selectedSiteId && { siteId: Number(selectedSiteId) }),
      ...(txnType && { txnType }),
      ...(startDate &&
        endDate && {
          dateRange: {
            startDate,
            endDate,
          },
        }),
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
      alert("Failed to generate report. See console for details.");
    }
  };

  const handleDownload = () => {
    if (!reportUrl) {
      alert("Please generate the report first.");
      return;
    }

    const link = document.createElement("a");
    link.href = reportUrl;
    const fileExtension = format === "csv" ? "csv" : "pdf";
    link.download = `orders_report.${fileExtension}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Orders Report Options
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
          <Select
            value={selectedSiteId}
            onChange={(e) => setSelectedSiteId(e.target.value)}
          >
            <MenuItem value="">All</MenuItem>
            {sites.map((site) => (
              <MenuItem key={site.id} value={site.id}>
                {site.siteName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Transaction Type</InputLabel>
          <Select value={txnType} onChange={(e) => setTxnType(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            {txnTypes.map((type) => (
              <MenuItem key={type.txnType} value={type.txnType}>
                {type.txnType}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField
          label="Start Date"
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
        />

        <TextField
          label="End Date"
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
        />
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

export default OrdersReport;
