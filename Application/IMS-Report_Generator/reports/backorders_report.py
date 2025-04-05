# =============================================================================
# BACKORDERS REPORT GENERATION
#
# Expected Request Format:
# {
#     "reportType": "backorders",         # required by app.py routing logic
#     "format": "pdf" | "csv",            # optional; default is "pdf"
#     "siteId": 4                         # optional; filters by destination site
#     "dateRange": {                      # optional date filtering
#         "startDate": "2025-01-01",
#         "endDate": "2025-12-31"
#     },
# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from datetime import datetime
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet
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


def generate_backorders_report(data):
    site_id = data.get("siteId")
    format = data.get("format", "pdf").lower()
    date_range = data.get("dateRange", {})
    start_date = date_range.get("startDate")
    end_date = date_range.get("endDate")
    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"backorders_report_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT 
            t.txnID, t.createdDate, s.siteName, i.name AS itemName, i.sku, ti.quantity
        FROM txn t
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN site s ON t.siteIDTo = s.siteID
        WHERE t.txnType = 'Back Order'
    """

    params = []
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

    if start_date and end_date:
        query += " AND t.createdDate BETWEEN %s AND %s"
        params.extend([start_date, end_date + " 23:59:59"])

    query += " ORDER BY s.siteName, t.createdDate"

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["Txn ID", "Created Date", "Store", "Item", "SKU", "Quantity"]
    df = pd.DataFrame(results, columns=columns)

    if df.empty:
        if format == "csv":
            df.to_csv(file_path, index=False)
        else:
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            styles = getSampleStyleSheet()
            elements = [Paragraph("Backorders Report", styles['Heading1'])]

            filter_parts = []
            if site_id:
                filter_parts.append(f"Site: {site_name}")
            if start_date and end_date:
                filter_parts.append(f"Date Range: {start_date} to {end_date}")

            if filter_parts:
                elements.append(
                    Paragraph(", ".join(filter_parts), styles['Normal']))
                elements.append(Spacer(1, 12))

            elements.append(Spacer(1, 24))
            elements.append(
                Paragraph("No backorders found for the selected filters.", styles['Normal']))
            doc.build(elements)

        return {"file_path": file_path}

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # Generate PDF — one page per backorder
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []
    logo_path = "static/bullseye1.png"
    # size in points (72 pt = 1 inch)
    logo = Image(logo_path, width=50, height=50)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'  # align top-right
    elements.append(logo)

    elements.append(Paragraph("Backorders Report", styles['Heading1']))
    if site_id:
        elements.append(Paragraph(f"Site: {site_name}", styles['Normal']))
    if start_date and end_date:
        elements.append(
            Paragraph(f"Date Range: {start_date} to {end_date}", styles['Normal']))

    grouped = df.groupby("Txn ID")

    for txn_id, group in grouped:
        store_name = group.iloc[0]["Store"]
        created_date = group.iloc[0]["Created Date"]
        # logo_path = "static/bullseye1.png"
        # # size in points (72 pt = 1 inch)
        # logo = Image(logo_path, width=50, height=50)
        # logo.vAlign = 'TOP'
        # logo.hAlign = 'RIGHT'  # align top-right
        # elements.append(logo)

        elements.append(
            Paragraph(f"Backorder ID: {txn_id}", styles['Heading2']))
        elements.append(Paragraph(f"Store: {store_name}", styles['Normal']))
        elements.append(
            Paragraph(f"Created: {created_date}", styles['Normal']))
        elements.append(Spacer(1, 12))

        filter_parts = []
        if site_id:
            filter_parts.append(f"Site: {site_name}")
        # if start_date and end_date:
        #     filter_parts.append(f"Date Range: {start_date} to {end_date}")

        if filter_parts:
            elements.append(
                Paragraph(", ".join(filter_parts), styles['Normal']))
            elements.append(Spacer(1, 12))

        table_data = [["Item", "SKU", "Quantity"]] + \
            group[["Item", "SKU", "Quantity"]].values.tolist()

        table = Table(table_data, repeatRows=1)
        table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.white),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 8),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
            ('BACKGROUND', (0, 1), (-1, -1), colors.white),
            ('GRID', (0, 0), (-1, -1), 0.25, colors.black),
        ]))
        table.hAlign = 'LEFT'

        elements.append(table)
        elements.append(PageBreak())

    doc.build(elements)
    return {"file_path": file_path}
