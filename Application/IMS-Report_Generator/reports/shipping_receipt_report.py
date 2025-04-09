# =============================================================================
# SHIPPING RECEIPT REPORT
#
# Expected Request:
# {
#     "reportType": "shipping_receipt",
#     "txnId": 12,               # required: the transaction ID
#     "format": "pdf" | "csv"    # optional: default is "pdf"

# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from datetime import datetime
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


def generate_shipping_receipt_report(data):
    txn_id = data.get("txnId")
    format = data.get("format", "pdf").lower()

    if not txn_id:
        raise ValueError("txnId is required")

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"shipping_receipt_{txn_id}_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # Pull joined data
    query = """
        SELECT
            t.barCode,
            s.siteName AS destination,
            d.vehicleType,
            d.deliveryDate,
            d.distanceCost,
            i.name AS itemName,
            i.sku,
            i.weight,
            ti.quantity
        FROM txn t
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN site s ON t.siteIDTo = s.siteID
        JOIN delivery d ON t.deliveryID = d.deliveryID
        WHERE t.txnID = %s
    """

    cursor.execute(query, (txn_id,))
    results = cursor.fetchall()

    columns = ["Item Name", "SKU", "Unit Weight", "Quantity"]
    df = pd.DataFrame(results, columns=[
        "Barcode", "Destination", "Vehicle Type", "Delivery Date", "Distance Cost",
        *columns
    ])

    if df.empty:
        raise ValueError("No data found for the provided transaction ID")

    # Extract header info (same across all rows)
    first_row = df.iloc[0]
    barcode = first_row["Barcode"]
    destination = first_row["Destination"]
    vehicle = first_row["Vehicle Type"]
    delivery_date = first_row["Delivery Date"].strftime("%Y-%m-%d")
    cost = f"${first_row['Distance Cost']:.2f}"

    df = df[columns]  # Only item-specific columns for table

    total_items = df["Quantity"].sum()
    total_weight = (df["Unit Weight"] * df["Quantity"]).sum()

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # Generate PDF
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []
    logo_path = "static/bullseye1.png"
    # size in points (72 pt = 1 inch)
    logo = Image(logo_path, width=125, height=125)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'  # align top-right
    elements.append(logo)

    # Header
    elements.append(Paragraph("Shipping Receipt", styles["Heading1"]))
    elements.append(Paragraph(f"Transaction ID: {txn_id}", styles["Normal"]))
    elements.append(Paragraph(f"Barcode: {barcode}", styles["Normal"]))
    elements.append(Paragraph(f"Destination: {destination}", styles["Normal"]))
    elements.append(Paragraph(f"Vehicle: {vehicle}", styles["Normal"]))
    elements.append(
        Paragraph(f"Delivery Date: {delivery_date}", styles["Normal"]))
    elements.append(Paragraph(f"Distance Cost: {cost}", styles["Normal"]))
    elements.append(Spacer(1, 12))

    # Table
    table_data = [df.columns.tolist()] + df.values.tolist()
    table = Table(table_data, repeatRows=1)
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('GRID', (0, 0), (-1, -1), 0.25, colors.black),
    ]))
    elements.append(table)
    elements.append(Spacer(1, 18))

    # Summary
    elements.append(Paragraph(f"Total SKUs: {total_items}", styles["Normal"]))
    elements.append(
        Paragraph(f"Total Weight: {total_weight:.2f} kg", styles["Normal"]))

    doc.build(elements)
    return {"file_path": file_path}
