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

const StoreOrderReport = () => {
  const [format, setFormat] = useState("pdf");
  const [txnId, setTxnId] = useState("");
  const [reportUrl, setReportUrl] = useState(null);
  const [txns, setTxns] = useState([]);
  const [selectedSiteId, setSelectedSiteId] = useState("");

  useEffect(() => {
    const fetchTxns = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/txns");
        const data = await res.json();
        // Filter txns to include only those where txnType is "Store Order"
        const storeOrderTxns = data.filter(
          (txn) => txn.txnType.txnType === "Store Order"
        );
        setTxns(storeOrderTxns);
      } catch (err) {
        console.error("[ERROR] Failed to load transactions:", err);
      }
    };
    fetchTxns();
  }, []);

  const handleGenerate = async () => {
    const payload = {
      reportType: "store_order",
      txnId,
      format,
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
    link.download = `store_order_report.${fileExtension}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Store Order Report Options
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
          <InputLabel>Transaction</InputLabel>
          <Select value={txnId} onChange={(e) => setTxnId(e.target.value)}>
            <MenuItem value="">Select Transaction</MenuItem>
            {txns.map((txn) => (
              <MenuItem key={txn.id} value={txn.id}>
                ID: {txn.id} - {txn.siteIDTo.siteName} - Ship Date:{" "}
                {txn.shipDate.split("T")[0]}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
        <Button variant="contained" onClick={handleGenerate} disabled={!txnId}>
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

export default StoreOrderReport;
