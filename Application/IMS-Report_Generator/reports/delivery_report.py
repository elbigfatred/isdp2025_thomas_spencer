import mysql.connector
import pandas as pd
import os
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "",
    "database": "bullseyedb2025",
}

# Directory to save reports
REPORTS_DIR = "reports/generated_reports"
os.makedirs(REPORTS_DIR, exist_ok=True)  # Ensure directory exists

def generate_delivery_report(data):
    start_date = data["dateRange"]["startDate"]
    end_date = data["dateRange"]["endDate"]

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT 
            d.deliveryID, d.deliveryDate, d.distanceCost, d.vehicleType, 
            t.txnID AS txnID, s.siteName AS destination, s.distanceFromWH AS mileage 
        FROM delivery d 
        JOIN txn t ON d.deliveryID = t.deliveryID 
        JOIN site s ON t.siteIDTo = s.siteID 
        WHERE d.deliveryDate BETWEEN %s AND %s
        ORDER BY d.deliveryDate, d.vehicleType;
    """
    cursor.execute(query, (start_date, end_date))
    results = cursor.fetchall()
    
    cursor.close()
    conn.close()

    # Convert results into DataFrame
    columns = ["Delivery ID", "Delivery Date", "Distance Cost", "Vehicle Type", "Txn ID", "Destination", "Mileage"]
    df = pd.DataFrame(results, columns=columns)

    # File path for the generated report
    file_path = os.path.join(REPORTS_DIR, f"delivery_report_{start_date}_to_{end_date}.pdf")

    # Generate PDF
    c = canvas.Canvas(file_path, pagesize=letter)
    width, height = letter

    c.drawString(100, height - 50, f"Delivery Report ({start_date} to {end_date})")
    y_position = height - 80

    headers = ["Delivery ID", "Date", "Txn ID", "Destination", "Mileage (km)", "Vehicle", "Cost ($)"]
    col_positions = [50, 120, 200, 280, 400, 480, 550]

    for i, header in enumerate(headers):
        c.drawString(col_positions[i], y_position, header)
    
    y_position -= 20

    for _, row in df.iterrows():
        row_data = [
            row["Delivery ID"],
            row["Delivery Date"].strftime("%Y-%m-%d"),
            row["Txn ID"],
            row["Destination"],
            f"{row['Mileage']} km",
            row["Vehicle Type"],
            f"${row['Distance Cost']:.2f}"
        ]
        for i, item in enumerate(row_data):
            c.drawString(col_positions[i], y_position, str(item))
        y_position -= 20

        if y_position < 50:
            c.showPage()
            y_position = height - 50

    c.save()

    # ✅ Return file path instead of sending the file
    return {"file_path": file_path}