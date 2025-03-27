import { Box, Typography } from "@mui/material";

const ReportViewer = ({ reportUrl }) => {
  if (!reportUrl) {
    return (
      <Typography variant="body1">
        No report generated yet. Please select options and click "Generate".
      </Typography>
    );
  }

  return (
    <Box sx={{ mt: 2, height: "600px" }}>
      <iframe
        title="Report Viewer"
        src={reportUrl}
        width="100%"
        height="100%"
        style={{ border: "1px solid #ccc" }}
      />
    </Box>
  );
};

export default ReportViewer;
