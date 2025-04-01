# =============================================================================
# SUPPLIER ORDER REPORT
#
# Expected Request:
# {
#     "reportType": "supplier_order",
#     "txnId": 19,                # required
#     "format": "pdf" | "csv"     # optional, default is "pdf"
# }
# =============================================================================

import mysql.connector
import os
import pandas as pd
from datetime import datetime
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak, Image
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "bullseyedb2025",
}

REPORTS_DIR = "reports/generated_reports"
os.makedirs(REPORTS_DIR, exist_ok=True)


def generate_supplier_order_report(data):
    txn_id = data.get("txnId")
    format = data.get("format", "pdf").lower()

    if not txn_id:
        raise ValueError("txnId is required")

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"supplier_order_report_{txn_id}_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT 
            s.name AS supplierName,
            i.name AS itemName,
            i.sku,
            ti.quantity,
            i.costPrice
        FROM txn t
        JOIN txnitems ti ON t.txnID = ti.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN supplier s ON i.supplierID = s.supplierID
        WHERE t.txnID = %s
        ORDER BY s.name, i.name
    """

    cursor.execute(query, (txn_id,))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["Supplier", "Item", "SKU", "Quantity", "Cost Price($)"]
    df = pd.DataFrame(results, columns=columns)

    if df.empty:
        if format == "csv":
            df.to_csv(file_path, index=False)
        else:
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            styles = getSampleStyleSheet()
            elements = [
                Paragraph("Supplier Order Report", styles['Heading1']),
                Paragraph(f"Transaction ID: {txn_id}", styles["Normal"]),
                Spacer(1, 12),
                Paragraph("No items found for this supplier order.",
                          styles["Normal"])
            ]
            doc.build(elements)

        return {"file_path": file_path}

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

 # PDF — one page per supplier
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []

    grouped = df.groupby("Supplier")

    for supplier, group in grouped:
        logo_path = "static/bullseye1.png"
        logo = Image(logo_path, width=50, height=50)
        logo.vAlign = 'TOP'
        logo.hAlign = 'RIGHT'
        elements.append(logo)

        elements.append(Paragraph(f"Supplier: {supplier}", styles['Heading1']))
        elements.append(
            Paragraph(f"Transaction ID: {txn_id}", styles["Normal"]))
        elements.append(Spacer(1, 12))

        table_data = [["Item", "SKU", "Quantity", "Cost Price($)"]]
        total_cost = 0

        for _, row in group.iterrows():
            item_name = row["Item"]
            sku = row["SKU"]
            quantity = row["Quantity"]
            cost_price = row["Cost Price($)"]
            subtotal = quantity * cost_price
            total_cost += subtotal
            table_data.append([item_name, sku, quantity, f"${cost_price:.2f}"])

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
        table.hAlign = 'LEFT'

        elements.append(table)
        elements.append(Spacer(1, 12))
        elements.append(Paragraph(
            f"<b>Subtotal for {supplier}:</b> ${total_cost:.2f}", styles["Normal"]))
        elements.append(PageBreak())

    doc.build(elements)
    return {"file_path": file_path}
