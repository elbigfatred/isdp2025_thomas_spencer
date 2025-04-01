# =============================================================================
# STORE ORDER REPORT
#
# Expected Request:
# {
#     "reportType": "store_order",
#     "txnId": 5,                  # required: the transaction ID
#     "format": "pdf" | "csv"      # optional: default is "pdf"
# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, Image
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors
from datetime import datetime

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "bullseyedb2025",
}

REPORTS_DIR = "reports/generated_reports"
os.makedirs(REPORTS_DIR, exist_ok=True)


def generate_store_order_report(data):
    txn_id = data.get("txnId")
    format = data.get("format", "pdf").lower()

    if not txn_id:
        raise ValueError("txnId is required")

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"store_order_{txn_id}_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # Query with cost/retail price and weight
    query = """
        SELECT
            t.barCode,
            s.siteName AS destination,
            i.name AS itemName,
            i.sku,
            i.weight,
            i.costPrice,
            i.retailPrice,
            ti.quantity
        FROM txn t
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN site s ON t.siteIDTo = s.siteID
        WHERE t.txnID = %s AND t.txnType = 'Store Order'
    """
    cursor.execute(query, (txn_id,))
    results = cursor.fetchall()

    columns = ["Item Name", "SKU", "Unit Weight(kg)",
               "Cost Price($)", "Retail Price($)", "Quantity"]
    df = pd.DataFrame(results, columns=[
        "Barcode", "Destination", *columns
    ])

    if df.empty:
        raise ValueError("No data found for the provided transaction ID")

    barcode = df.iloc[0]["Barcode"]
    destination = df.iloc[0]["Destination"]
    df = df[columns]

    total_items = df["Quantity"].sum()
    total_weight = (df["Unit Weight(kg)"] * df["Quantity"]).sum()
    total_cost = (df["Cost Price($)"] * df["Quantity"]).sum()
    total_retail = (df["Retail Price($)"] * df["Quantity"]).sum()

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # Generate PDF
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []

    # Add logo
    logo_path = "static/bullseye1.png"
    logo = Image(logo_path, width=50, height=50)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'
    elements.append(logo)

    elements.append(Paragraph("Store Order Report", styles["Heading1"]))
    elements.append(Paragraph(f"Transaction ID: {txn_id}", styles["Normal"]))
    elements.append(Paragraph(f"Barcode: {barcode}", styles["Normal"]))
    elements.append(Paragraph(f"Store: {destination}", styles["Normal"]))
    elements.append(Spacer(1, 12))

    # Table
    table_data = [df.columns.tolist()] + df.values.tolist()
    table = Table(table_data, repeatRows=1)
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.white),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 6),
        ('GRID', (0, 0), (-1, -1), 0.25, colors.black),
    ]))
    elements.append(table)
    elements.append(Spacer(1, 18))

    # Summary
    elements.append(Paragraph(f"Total Items: {total_items}", styles["Normal"]))
    elements.append(
        Paragraph(f"Total Weight: {total_weight:.2f} kg", styles["Normal"]))
    elements.append(
        Paragraph(f"Total Cost: ${total_cost:.2f}", styles["Normal"]))
    elements.append(
        Paragraph(f"Total Retail Value: ${total_retail:.2f}", styles["Normal"]))

    doc.build(elements)
    return {"file_path": file_path}
