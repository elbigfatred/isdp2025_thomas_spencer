# =============================================================================
# INVENTORY REPORT GENERATION
#
# Expected Request Format:
# {
#     "reportType": "inventory",         # required by app.py routing logic
#     "format": "pdf" | "csv",           # optional; default is "pdf"
#     "siteId": 4,                       # optional; filters inventory by site
#     "sortBy": "item" | "quantity",     # optional; default is "item"
#     "sortOrder": "asc" | "desc"        # optional; default is "asc"
# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from datetime import datetime
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from reportlab.lib.units import inch
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


def generate_inventory_report(data):
    site_id = data.get("siteId")
    format = data.get("format", "pdf").lower()
    sort_by = data.get("sortBy", "item").lower()
    sort_order = data.get("sortOrder", "asc").lower()

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"inventory_report_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT 
            i.itemID, s.siteName, it.name AS itemName, it.sku, i.quantity, 
            i.reorderThreshold, i.optimumThreshold
        FROM inventory i
        JOIN site s ON i.siteID = s.siteID
        JOIN item it ON i.itemID = it.itemID
    """

    params = []
    if site_id:
        query += " WHERE i.siteID = %s"
        params.append(site_id)

    # Sorting logic
    if sort_by == "quantity":
        query += " ORDER BY i.quantity"
    elif sort_by == "site":
        query += " ORDER BY s.siteName"
    else:
        query += " ORDER BY it.name"

    query += " DESC" if sort_order == "desc" else " ASC"

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["Item ID", "Site", "Item Name", "SKU",
               "Quantity", "Reorder Threshold", "Optimum Threshold"]

    MAX_ITEM_NAME_LEN = 27
    df = pd.DataFrame(results, columns=columns)

    if df.empty:
        if format == "csv":
            df.to_csv(file_path, index=False)
        else:
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            styles = getSampleStyleSheet()
            elements = [Paragraph("Inventory Report", styles['Heading1'])]

            if site_id:
                elements.append(
                    Paragraph(f"Filters: Site ID = {site_id}", styles['Normal']))
                elements.append(Spacer(1, 12))

            elements.append(Spacer(1, 24))
            elements.append(
                Paragraph("No matching records found.", styles['Normal']))
            doc.build(elements)

        return {"file_path": file_path}

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # PDF Export
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()

    logo_path = "static/bullseye1.png"
    logo = Image(logo_path, width=50, height=50)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'

    elements = [logo]  # Logo goes first
    elements.append(Paragraph("Inventory Report", styles['Heading1']))

    if site_id:
        elements.append(
            Paragraph(f"Filters: Site ID = {site_id}", styles['Normal']))
        elements.append(Spacer(1, 12))

    sort_label = {
        "item": "Item Name",
        "quantity": "Quantity",
        "site": "Site"
    }.get(sort_by, "Item Name")

    elements.append(
        Paragraph(f"Sorted by: {sort_label} ({sort_order.upper()})", styles['Normal']))
    elements.append(Spacer(1, 12))

    if "Item Name" in df.columns:
        df["Item Name"] = df["Item Name"].apply(
            lambda x: x if len(
                x) <= MAX_ITEM_NAME_LEN else x[:MAX_ITEM_NAME_LEN - 3] + "..."
        )

    # condense Site name as well
    df["Site"] = df["Site"].apply(
        lambda x: x if len(
            x) <= MAX_ITEM_NAME_LEN else x[:MAX_ITEM_NAME_LEN - 3] + "..."
    )

    table_data = [df.columns.tolist()] + df.values.tolist()

    # Table setup with fixed column widths
    col_widths = [1.2 * inch,   # Site
                  2.5 * inch,   # Item Name
                  1.2 * inch,   # SKU
                  0.9 * inch,   # Quantity
                  1.2 * inch,   # Reorder Threshold
                  1.2 * inch]   # Optimum Threshold
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

    elements.append(table)
    doc.build(elements)

    return {"file_path": file_path}
