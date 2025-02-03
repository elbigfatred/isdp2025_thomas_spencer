import { useEffect, useState } from "react";
import axios from "axios";
import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer } from "@mui/material";

const SitesTable = () => {
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/sites")
      .then((response) => {
        setSites(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching sites:", error);
        setError("Failed to load sites.");
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading sites...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <TableContainer component={Paper} style={{ maxWidth: "80%", margin: "auto", marginTop: "20px" }}>
      <Table>
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
          {sites.map((site) => (
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
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default SitesTable;
