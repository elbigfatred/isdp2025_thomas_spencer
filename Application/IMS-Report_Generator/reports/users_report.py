# 7. Users - list all, sort by role, site

import mysql.connector
import os
import pandas as pd
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
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


def generate_users_report(data):
    role = data.get("role")
    site_id = data.get("siteId")
    format = data.get("format", "pdf").lower()  # default to PDF

    today = datetime.now().strftime("%Y-%m-%d")
    filename = f"users_report_{today}.{format}"
    file_path = os.path.join(REPORTS_DIR, filename)

    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    query = """
        SELECT e.employeeID, e.username, e.firstname, e.lastname, e.email,
               s.siteName, e.main_role
        FROM employee e
        JOIN site s ON e.siteID = s.siteID
        WHERE e.active = 1
    """

    params = []
    if role:
        query += " AND e.main_role like %s"
        params.append(role)
    if site_id:
        query += " AND s.siteID = %s"
        params.append(site_id)

    query += " ORDER BY e.main_role, s.siteName"

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    conn.close()

    columns = ["ID", "Username", "First Name",
               "Last Name", "Email", "Site", "Role"]
    df = pd.DataFrame(results, columns=columns)

    if df.empty:
        if format == "csv":
            # Still create an empty CSV with headers (optional)
            df.to_csv(file_path, index=False)
        else:
            # Create a PDF that says no data was found
            doc = SimpleDocTemplate(file_path, pagesize=letter)
            elements = []

            styles = getSampleStyleSheet()
            elements.append(Paragraph("Users Report", styles['Heading1']))

            if role or site_id:
                filter_parts = []
                if role:
                    filter_parts.append(f"Role = {role}")
                if site_id:
                    filter_parts.append(f"Site ID = {site_id}")
                filter_text = "Filters: " + ", ".join(filter_parts)
                elements.append(Paragraph(filter_text, styles['Normal']))
                elements.append(Spacer(1, 12))

            elements.append(Spacer(1, 24))
            elements.append(Paragraph(
                "No matching records found for the selected filters.",
                styles['Normal']
            ))

            doc.build(elements)

        return {"file_path": file_path}

    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    doc = SimpleDocTemplate(file_path, pagesize=letter)

    elements = []

    styles = getSampleStyleSheet()

    # Turn DataFrame into list-of-lists (header + rows)
    elements.append(Paragraph("Users Report", styles['Heading1']))
    filter_parts = []
    if role:
        filter_parts.append(f"Role = {role}")
    if site_id:
        filter_parts.append(f"Site ID = {site_id}")

    if filter_parts:
        filter_text = "Filters: " + ", ".join(filter_parts)
        elements.append(Paragraph(filter_text, styles['Normal']))
        elements.append(Spacer(1, 12))  # Only add space if filters are shown
    elements.append(Spacer(1, 12))

    # Table setup
    data_for_table = [df.columns.tolist()] + df.values.tolist()
    table = Table(data_for_table, repeatRows=1)

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
