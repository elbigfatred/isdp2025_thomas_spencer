# =============================================================================
# EMERGENCY ORDERS REPORT GENERATION
#
# Expected Request Format:
# {
#     "reportType": "emergency_orders",     # required by app.py routing logic
#     "format": "pdf" | "csv",              # optional; default is "pdf"
#     "siteId": 4,                          # optional; filters by destination site
#     "dateRange": {                        # optional; filters by txn.createdDate
#         "startDate": "2025-01-01",
#         "endDate": "2025-12-31"
#     }
# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from datetime import datetime
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak
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


def generate_emergency_orders_report(data):
    site_id = data.get("siteId")
    format = data.get("format", "pdf").lower()
    date_range = data.get("dateRange", {})
    start_date = date_range.get("startDate", "2000-01-01")
    end_date = date_range.get("endDate", "2100-01-01")

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"emergency_orders_report_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT 
            t.txnID, t.createdDate, s.siteName,
            COUNT(ti.itemID) AS itemCount,
            SUM(i.weight * ti.quantity) AS totalWeight
        FROM txn t
        JOIN site s ON t.siteIDTo = s.siteID
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        WHERE t.txnType = 'Emergency Order'
          AND t.createdDate BETWEEN %s AND %s
    """

    params = [start_date, end_date]

    if site_id:
        query += " AND t.siteIDTo = %s"
        params.append(site_id)

    site_name = None
    if site_id:
        cursor.execute(
            "SELECT siteName FROM site WHERE siteID = %s", (site_id,))
        site_row = cursor.fetchone()
        if site_row:
            site_name = site_row[0]

    query += """
        GROUP BY t.txnID, t.createdDate, s.siteName
        ORDER BY s.siteName, t.createdDate
    """

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["ID", "Created", "Store",
               "Item Count", "Total Weight (kg)"]
    df = pd.DataFrame(results, columns=columns)

    # Handle empty result
    if df.empty:
        if format == "csv":
            df.to_csv(file_path, index=False)
        else:
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            styles = getSampleStyleSheet()
            elements = [
                Paragraph("Emergency Orders Report", styles['Heading1'])]

            # Filters
            filters = []
            if site_id:
                filters.append(f"Site: {site_name}")
            if start_date or end_date:
                filters.append(f"Date Range: {start_date} to {end_date}")
            if filters:
                elements.append(
                    Paragraph(", ".join(filters), styles['Normal']))
                elements.append(Spacer(1, 12))

            elements.append(Spacer(1, 24))
            elements.append(Paragraph(
                "No emergency orders found for the selected filters.", styles['Normal']))
            doc.build(elements)

        return {"file_path": file_path}

    # ✅ CSV Export
    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # ✅ PDF Export
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []

    # Insert logo first
    logo_path = "static/bullseye1.png"
    if os.path.exists(logo_path):
        logo = Image(logo_path, width=50, height=50)
        logo.hAlign = 'RIGHT'
        elements.append(logo)
        elements.append(Spacer(1, 6))  # Optional: space after logo

    # Then title
    elements.append(Paragraph("Emergency Orders Report", styles['Heading1']))
    filters = []
    if site_id:
        filters.append(f"Site: {site_name}")
    if start_date or end_date:
        filters.append(f"Date Range: {start_date} to {end_date}")
    if filters:
        elements.append(
            Paragraph(", ".join(filters), styles['Normal']))
        elements.append(Spacer(1, 12))

    # Group by site to give each one its own page
    grouped = df.groupby("Store")
    for store, group in grouped:
        elements.append(Paragraph(f"Store: {store}", styles['Heading2']))
        elements.append(Spacer(1, 6))

        table_data = [group.columns.tolist()] + group.values.tolist()

        table = Table(table_data, repeatRows=1)
        table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.white),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 8),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
            ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
            ('GRID', (0, 0), (-1, -1), 0.25, colors.black),
        ]))

        elements.append(table)
        elements.append(PageBreak())

    doc.build(elements)
    return {"file_path": file_path}
