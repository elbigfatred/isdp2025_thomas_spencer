# 7. Users - list all, sort by role, site

# =============================================================================
# USERS REPORT GENERATION
#
# Expected Request Format:
# {
#     "reportType": "users",         # required by app.py routing logic
#     "format": "pdf" | "csv",       # optional; default is "pdf"
#     "role": "Store Manager",       # optional; filters by main_role
#     "siteId": 4,                   # optional; filters by siteID
#     "sortBy": "role" | "site",     # optional; default is "role"
#     "sortOrder": "asc" | "desc"    # optional; default is "asc"
# }
#
# Notes:
# - If no filters are provided, all active users are included.
# - PDF output includes a styled table and filter/sort summary (if applicable).
# - If no matching results are found, a message is shown in the PDF.
# - CSV always includes headers, even if no data is returned.
# =============================================================================

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


def generate_users_report(data):
    role = data.get("role")
    site_id = data.get("siteId")
    format = data.get("format", "pdf").lower()  # default to PDF
    sort_by = data.get("sortBy", "role").lower()         # Default: role
    sort_order = data.get("sortOrder", "asc").lower()    # Default: ascending

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
    order_clause = " ORDER BY e.main_role, s.siteName"  # default

    site_name = None
    if site_id:
        cursor.execute(
            "SELECT siteName FROM site WHERE siteID = %s", (site_id,))
        site_row = cursor.fetchone()
        if site_row:
            site_name = site_row[0]

    if sort_by == "role":
        order_clause = " ORDER BY e.main_role"
    elif sort_by == "site":
        order_clause = " ORDER BY s.siteName"

    if sort_order == "desc":
        order_clause += " DESC"
    else:
        order_clause += " ASC"

    query += order_clause

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
                    filter_parts.append(f"Role: {role}")
                if site_id:
                    filter_parts.append(f"Site: {site_name}")
                filter_text = ", ".join(filter_parts)
                elements.append(Paragraph(filter_text, styles['Normal']))
                elements.append(Spacer(1, 12))

            elements.append(Spacer(1, 24))
            elements.append(Paragraph(
                "No matching records found for the selected filters.",
                styles['Normal']
            ))

            doc.build(elements)

        return {"file_path": file_path}

    ### EXPORT CSV if requested ###
    if format == "csv":
        df.to_csv(file_path, index=False)
        return {"file_path": file_path}

    ### EXPORT PDF (default) ####

    doc = SimpleDocTemplate(file_path, pagesize=letter)

    elements = []

    styles = getSampleStyleSheet()

    logo_path = "static/bullseye1.png"
    # size in points (72 pt = 1 inch)
    logo = Image(logo_path, width=50, height=50)
    logo.vAlign = 'TOP'
    logo.hAlign = 'RIGHT'  # align top-right
    elements.append(logo)

    # Turn DataFrame into list-of-lists (header + rows)
    elements.append(Paragraph("Users Report", styles['Heading1']))
    filter_parts = []
    if role:
        filter_parts.append(f"Role: {role}")
    if site_id:
        filter_parts.append(f"Site: {site_name}")

    if filter_parts:
        filter_text = ", ".join(filter_parts)
        elements.append(Paragraph(filter_text, styles['Normal']))
        elements.append(Spacer(1, 12))

    # Sorting summary
    sort_text = f"Sorted by: {sort_by.title()} ({sort_order.upper()})"
    elements.append(Paragraph(sort_text, styles['Normal']))
    elements.append(Spacer(1, 12))

    # Table setup
    data_for_table = [df.columns.tolist()] + df.values.tolist()
    table = Table(data_for_table, repeatRows=1)

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
    # table.hAlign = 'LEFT'

    elements.append(table)
    doc.build(elements)

    return {"file_path": file_path}
