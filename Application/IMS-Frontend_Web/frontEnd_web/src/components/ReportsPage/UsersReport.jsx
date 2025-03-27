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
import { useEffect } from "react";

const UsersReport = () => {
  const [format, setFormat] = useState("pdf");
  const [role, setRole] = useState("");
  const [siteId, setSiteId] = useState("");
  const [sortBy, setSortBy] = useState("role");
  const [sortOrder, setSortOrder] = useState("asc");
  const [reportUrl, setReportUrl] = useState(null);
  const [sites, setSites] = useState([]);
  const [selectedSiteId, setSelectedSiteId] = useState("");

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
      reportType: "users",
      format,
      ...(role && { role }),
      ...(selectedSiteId && { siteId: Number(selectedSiteId) }),
      ...(sortBy && { sortBy }),
      ...(sortOrder && { sortOrder }),
    };

    //console.log("[DEBUG] Final report request payload:", payload);

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

    // Optional: Set the filename based on format
    const fileExtension = format === "csv" ? "csv" : "pdf";
    link.download = `users_report.${fileExtension}`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Users Report Options
      </Typography>

      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 2, mb: 2 }}>
        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Format</InputLabel>
          <Select value={format} onChange={(e) => setFormat(e.target.value)}>
            <MenuItem value="pdf">PDF</MenuItem>
            <MenuItem value="csv">CSV</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 180 }}>
          <InputLabel>Role</InputLabel>
          <Select
            value={role}
            label="Role"
            onChange={(e) => setRole(e.target.value)}
          >
            <MenuItem value="">All</MenuItem>
            <MenuItem value="Administrator">Administrator</MenuItem>
            <MenuItem value="Store Manager">Store Manager</MenuItem>
            <MenuItem value="Store Worker">Store Worker</MenuItem>
            <MenuItem value="Warehouse Manager">Warehouse Manager</MenuItem>
            <MenuItem value="Warehouse Worker">Warehouse Worker</MenuItem>
            <MenuItem value="Delivery">Delivery</MenuItem>
            <MenuItem value="Regional Manager">Regional Manager</MenuItem>
            <MenuItem value="Financial Manager">Financial Manager</MenuItem>
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

        <FormControl sx={{ minWidth: 160 }}>
          <InputLabel>Sort By</InputLabel>
          <Select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <MenuItem value="role">Role</MenuItem>
            <MenuItem value="site">Site</MenuItem>
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

export default UsersReport;
