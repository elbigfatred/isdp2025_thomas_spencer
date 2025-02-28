import { useEffect, useState } from "react";
import axios from "axios";
import {
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  TableContainer,
  TextField,
  Box,
  Typography,
  IconButton,
  Tooltip,
  CircularProgress,
} from "@mui/material";

import HelpOutlineIcon from "@mui/icons-material/HelpOutline"; // ✅ Help Icon

const SitesTable = () => {
  const [sites, setSites] = useState([]);
  const [filteredSites, setFilteredSites] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/sites")
      .then((response) => {
        setSites(response.data);
        setFilteredSites(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching sites:", error);
        setError("Failed to load sites.");
        setLoading(false);
      });
  }, []);

  // ✅ Search Filtering
  useEffect(() => {
    const filtered = sites.filter(
      (site) =>
        site.siteName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        site.city.toLowerCase().includes(searchQuery.toLowerCase()) ||
        site.province?.provinceID
          .toLowerCase()
          .includes(searchQuery.toLowerCase()) ||
        site.country.toLowerCase().includes(searchQuery.toLowerCase()) ||
        site.phone.includes(searchQuery)
    );
    setFilteredSites(filtered);
  }, [searchQuery, sites]);

  return (
    <Box sx={{ maxWidth: "80%", margin: "auto", marginTop: "20px" }}>
      {/* ✅ Title and Help Tooltip Row */}
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 1,
        }}
      >
        <Typography variant="h5" fontWeight="bold">
          Sites
        </Typography>
        <Tooltip
          title="This table displays all active and inactive sites, including address, city, and contact information.
                 Use the search bar to filter by name, city, province, or phone number."
          arrow
        >
          <IconButton>
            <HelpOutlineIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {/* ✅ Search Bar */}
      <TextField
        label="Search Sites"
        variant="outlined"
        fullWidth
        sx={{ marginBottom: 2 }}
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />

      {/* ✅ Table */}
      <TableContainer
        component={Paper}
        sx={{ maxHeight: "70vh", overflow: "auto" }}
      >
        {loading ? (
          <Box sx={{ display: "flex", justifyContent: "center", padding: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Typography color="error" align="center" sx={{ padding: 2 }}>
            {error}
          </Typography>
        ) : (
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>
                  <strong>Site Name</strong>
                </TableCell>
                <TableCell>
                  <strong>Address</strong>
                </TableCell>
                <TableCell>
                  <strong>City</strong>
                </TableCell>
                <TableCell>
                  <strong>Province</strong>
                </TableCell>
                <TableCell>
                  <strong>Country</strong>
                </TableCell>
                <TableCell>
                  <strong>Phone</strong>
                </TableCell>
                <TableCell>
                  <strong>Active</strong>
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredSites.length > 0 ? (
                filteredSites.map((site) => (
                  <TableRow key={site.id}>
                    <TableCell>{site.siteName}</TableCell>
                    <TableCell>
                      {site.address} {site.address2 ? `, ${site.address2}` : ""}
                    </TableCell>
                    <TableCell>{site.city}</TableCell>
                    <TableCell>{site.province?.provinceID || "N/A"}</TableCell>
                    <TableCell>{site.country}</TableCell>
                    <TableCell>{site.phone}</TableCell>
                    <TableCell>{site.active ? "Yes" : "No"}</TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    No sites found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </TableContainer>
    </Box>
  );
};

export default SitesTable;
