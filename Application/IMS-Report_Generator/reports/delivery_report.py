# =============================================================================
# DELIVERY REPORT
#
# Expected Request:
# {
#     "reportType": "delivery_report",
#     "deliveryDate": "2025-03-26",   # required: delivery date (YYYY-MM-DD)
#     "format": "pdf" | "csv"         # optional: default is "pdf"
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


def generate_delivery_report(data):
    delivery_date = data.get("deliveryDate")
    format = data.get("format", "pdf").lower()
    formatted_delivery_date = datetime.strptime(
        delivery_date, "%Y-%m-%d").strftime("%A; %Y-%m-%d")

    if not delivery_date:
        raise ValueError("Missing 'deliveryDate' in request.")

    filename = f"delivery_report_{delivery_date}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT
            d.deliveryID,
            d.deliveryDate,
            s.siteName,
            s.address,
            s.city,
            s.provinceID,
            s.distanceFromWH,
            d.vehicleType,
            v.costPerKm,
            SUM(i.weight * ti.quantity) AS totalWeight
        FROM delivery d
        JOIN txn t ON t.deliveryID = d.deliveryID
        JOIN site s ON t.siteIDTo = s.siteID
        JOIN txnitems ti ON ti.txnID = t.txnID
        JOIN item i ON ti.itemID = i.itemID
        JOIN vehicle v ON d.vehicleType = v.vehicleType
        WHERE d.deliveryDate = %s
        GROUP BY d.deliveryID, t.txnID
        ORDER BY d.deliveryID;
    """

    cursor.execute(query, (delivery_date,))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = [
        "Delivery ID", "Delivery Date", "Store", "Address", "City", "Province",
        "Distance (km)", "Vehicle", "Cost/km", "Total Weight (kg)"
    ]
    df = pd.DataFrame(results, columns=columns)

    # If the DataFrame is empty, handle it accordingly
    if df.empty:
        # If format is CSV, only include the headers
        if format == "csv":
            # Write only the column headers to CSV
            df.head(0).to_csv(file_path, index=False)
            return {"file_path": file_path}

        # If format is PDF, create the report with a message indicating no data found
        doc = SimpleDocTemplate(file_path, pagesize=letter)
        styles = getSampleStyleSheet()
        elements = [
            Paragraph("Delivery Report", styles["Heading1"]),
            Spacer(1, 12),
            Paragraph(f"{formatted_delivery_date}", styles["Normal"]),
            Spacer(1, 24),
            Paragraph("No deliveries found for the selected date.",
                      styles["Normal"])
        ]
        doc.build(elements)
        return {"file_path": file_path}

    # If there are results, calculate totals and create the report
    df["Distance (km)"] = df["Distance (km)"].astype(float)
    df["Cost/km"] = df["Cost/km"].astype(float)
    df["Subtotal"] = df["Distance (km)"] * df["Cost/km"]

    total_km = df["Distance (km)"].sum()
    total_cost = df["Subtotal"].sum()

    # If the format is CSV, write data including totals
    if format == "csv":
        df["Subtotal"] = df["Subtotal"].round(2)  # rounding for readability
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    # For PDF generation, create the full report with grouped data
    doc = SimpleDocTemplate(file_path, pagesize=letter)
    styles = getSampleStyleSheet()
    elements = []

    grouped = df.groupby("Delivery ID")

    for delivery_id, group in grouped:
        logo_path = "static/bullseye1.png"
        logo = Image(logo_path, width=125, height=125)
        logo.vAlign = 'TOP'
        logo.hAlign = 'RIGHT'
        elements.append(logo)

        # elements.append(
        #     Paragraph(f"Delivery ID: {delivery_id}", styles["Heading1"]))
        elements.append(Paragraph("Delivery Report", styles["Heading1"]))
        # elements.append(
        #     Paragraph(f"Date: {group.iloc[0]['Delivery Date']}", styles["Normal"]))
        elements.append(
            Paragraph(f"{formatted_delivery_date}", styles["Normal"]))
        elements.append(
            Paragraph(f"Vehicle Type: {group.iloc[0]['Vehicle']}", styles["Normal"]))
        elements.append(Spacer(1, 12))

        table_data = [["Store", "Address", "City", "Province", "Distance (km)", "Cost/km", "Weight (kg)", "Distance Cost"]] + [
            [
                row["Store"],
                row["Address"],
                row["City"],
                row["Province"],
                f"{row['Distance (km)']:.2f}",
                f"${row['Cost/km']:.2f}",
                f"{row['Total Weight (kg)']:.2f}",
                f"${row['Subtotal']:.2f}"
            ]
            for idx, row in group.iterrows()
        ]

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
        elements.append(Spacer(1, 12))

    # Summary page
    # elements.append(Paragraph("Delivery Summary", styles["Heading1"]))
    elements.append(
        Paragraph(f"Total Travel Distance from Warehouse: {total_km:.2f} km", styles["Normal"]))
    # total count of orders
    elements.append(
        Paragraph(f"Total Deliveries: {len(df['Address'].unique())}", styles["Normal"]))

    doc.build(elements)

    return {"file_path": file_path}
