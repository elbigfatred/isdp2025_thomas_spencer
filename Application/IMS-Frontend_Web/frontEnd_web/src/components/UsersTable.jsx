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
} from "@mui/material";

const UsersTable = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios
      .get("http://localhost:8080/api/employees")
      .then((response) => {
        setUsers(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching users:", error);
        setError("Failed to load users.");
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Loading users...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <TableContainer
      component={Paper}
      style={{ maxWidth: "80%", margin: "auto", marginTop: "20px" }}
    >
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>
              <strong>Username</strong>
            </TableCell>
            <TableCell>
              <strong>First Name</strong>
            </TableCell>
            <TableCell>
              <strong>Last Name</strong>
            </TableCell>
            <TableCell>
              <strong>Email</strong>
            </TableCell>
            <TableCell>
              <strong>Site</strong>
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {users.map((user) => (
            <TableRow key={user.id}>
              <TableCell>{user.username}</TableCell>
              <TableCell>{user.firstname}</TableCell>
              <TableCell>{user.lastname}</TableCell>
              <TableCell>{user.email}</TableCell>
              <TableCell>{user.site?.siteName || "N/A"}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default UsersTable;
