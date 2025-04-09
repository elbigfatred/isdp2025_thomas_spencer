# =============================================================================
# ORDERS REPORT GENERATION
#
# Expected Request Format:
# {
#     "reportType": "orders",              # required by app.py routing logic
#     "format": "pdf" | "csv",             # optional; default is "pdf"
#     "siteId": 4,                         # optional; filters by destination site
#     "txnType": "Store Order",            # optional; filters by transaction type
#     "dateRange": {
#         "startDate": "2025-01-01",       # required
#         "endDate": "2025-12-31"          # required
#     }
# }s
# =============================================================================

import mysql.connector
import os
import pandas as pd
from datetime import datetime
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from reportlab.platypus import Image

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "bullseyedb2025",
}

REPORTS_DIR = "reports/generated_reports"
os.makedirs(REPORTS_DIR, exist_ok=True)


def generate_orders_report(data):
    site_id = data.get("siteId")
    txn_type = data.get("txnType")
    date_range = data.get("dateRange", {})
    start_date = date_range.get("startDate")
    end_date = date_range.get("endDate")
    format = data.get("format", "pdf").lower()
    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"orders_report_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    if not start_date or not end_date:
        raise ValueError("Start and end dates are required.")

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT
            t.txnID, t.createdDate, s.siteName, t.txnType,
            COUNT(ti.itemID) AS itemCount,
            SUM(i.weight * ti.quantity) AS totalWeight
        FROM txn t
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN site s ON t.siteIDTo = s.siteID
        WHERE t.createdDate BETWEEN %s AND %s
    """

    params = [start_date, end_date]

    if site_id:
        query += " AND s.siteID = %s"
        params.append(site_id)

    site_name = None
    if site_id:
        cursor.execute(
            "SELECT siteName FROM site WHERE siteID = %s", (site_id,))
        site_row = cursor.fetchone()
        if site_row:
            site_name = site_row[0]

    if txn_type:
        query += " AND t.txnType = %s"
        params.append(txn_type)

    query += " GROUP BY t.txnID, t.createdDate, s.siteName, t.txnType"
    query += " ORDER BY t.createdDate DESC"

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["ID", "Created", "Site",
               "Transaction Type", "SKU Count", "Total Weight(kg)"]
    df = pd.DataFrame(results, columns=columns)

    if df.empty:
        if format == "csv":
            df.to_csv(file_path, index=False)
        else:
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            styles = getSampleStyleSheet()
            elements = [Paragraph("Orders Report", styles["Heading1"])]

            filter_parts = [f"Date Range: {start_date} to {end_date}"]
            if site_id:
                filter_parts.append(f"Site: {site_name}")
            if txn_type:
                filter_parts.append(f"Transaction Type: {txn_type}")
            elements.append(
                Paragraph(", ".join(filter_parts), styles["Normal"]))
            elements.append(Spacer(1, 24))
            elements.append(
                Paragraph("No results found for the selected filters.", styles["Normal"]))
            doc.build(elements)
        return {"file_path": file_path}

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # PDF Export
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []

    logo_path = "static/bullseye1.png"
    logo = Image(logo_path, width=125, height=125)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'
    elements.append(logo)

    elements.append(Paragraph("Orders Report", styles["Heading1"]))
    filter_parts = [f"Date Range: {start_date} to {end_date}"]
    if site_id:
        filter_parts.append(f"Site: {site_name}")
    if txn_type:
        filter_parts.append(f"Transaction Type: {txn_type}")
    elements.append(
        Paragraph(", ".join(filter_parts), styles["Normal"]))
    elements.append(Spacer(1, 12))

    # Table
    table_data = [df.columns.tolist()] + df.values.tolist()
    table = Table(table_data, repeatRows=1)
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#E53935")),  # Header red
        # Header text white
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
        # Light blue for rows
        ('BACKGROUND', (0, 1), (-1, -1), colors.HexColor("#E3F2FD")),
        ('GRID', (0, 0), (-1, -1), 0.25, colors.black),
    ]))
    elements.append(table)

    doc.build(elements)
    return {"file_path": file_path}
