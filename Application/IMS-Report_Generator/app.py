from flask import Flask, request, send_file, jsonify
import os
from flask_cors import CORS
from flask import send_file

from reports.delivery_report import generate_delivery_report
from reports.store_order_report import generate_store_order_report
from reports.shipping_receipt_report import generate_shipping_receipt_report
from reports.inventory_report import generate_inventory_report
from reports.orders_report import generate_orders_report
from reports.emergency_orders_report import generate_emergency_orders_report
from reports.users_report import generate_users_report
from reports.backorders_report import generate_backorders_report
from reports.supplier_order_report import generate_supplier_order_report

app = Flask(__name__)
CORS(app)

# Report type mapping
REPORT_FUNCTIONS = {
    "delivery_report": generate_delivery_report,
    "store_order": generate_store_order_report,
    "shipping_receipt": generate_shipping_receipt_report,
    "inventory": generate_inventory_report,
    "orders": generate_orders_report,
    "emergency_orders": generate_emergency_orders_report,
    "users": generate_users_report,
    "backorders": generate_backorders_report,
    "supplier_order": generate_supplier_order_report
}


@app.route("/generate-report", methods=["POST"])
def generate_report():
    try:
        data = request.json
        report_type = data.get("reportType")

        if report_type not in REPORT_FUNCTIONS:
            return jsonify({"error": "Invalid report type"}), 400

        # Generate the report and get the file path
        result = REPORT_FUNCTIONS[report_type](data)
        file_path = result["file_path"]

        # Set the MIME type based on file extension
        if file_path.endswith(".pdf"):
            mimetype = "application/pdf"
        elif file_path.endswith(".csv"):
            mimetype = "text/csv"
        else:
            mimetype = "application/octet-stream"

        # Serve the file as an attachment
        return send_file(
            file_path,
            as_attachment=True,
            mimetype=mimetype,
            download_name=os.path.basename(file_path)
        )

    except Exception as e:
        print("Error generating report:", e)
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(debug=True)
