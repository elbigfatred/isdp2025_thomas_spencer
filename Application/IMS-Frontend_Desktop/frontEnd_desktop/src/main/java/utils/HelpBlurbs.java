package utils;

/**
 * HelpBlurbs stores static help text messages for various UI components.
 * These messages provide friendly guidance to make navigation easy!
 */
public class HelpBlurbs {

    public static final String LOGIN_HELP =
            "<html><br><br>🔑 <b>Login Help</b><br><br>"
                    + "• Enter your <b>username</b> and <b>password</b> to access the system.<br>"
                    + "• 👁️ Click the <b>eyeball icon</b> to reveal your password.<br>"
                    + "• If you forget your password, use <b>'Forgot Password?'</b> to reset it.<br>"
                    + "• 🛠️ Need help? Contact an administrator—we're happy to assist!<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String RESET_PASSWORD_HELP =
            "<html><br><br>🔒 <b>Reset Your Password</b><br><br>"
                    + "• Your <b>username</b> is already filled in—just enter a new password!<br>"
                    + "• ✅ Watch the <b>password strength checker</b> to ensure your password is strong enough.<br>"
                    + "• 🔄 Click <b>'Generate Strong Password'</b> for an automatic secure password.<br>"
                    + "• 👁️ Use the <b>eyeball icon</b> to unveil your password while typing.<br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String DASHBOARD_HELP =
            "<html><br><br>🏠 <b>Your Dashboard</b><br><br>"
                    + "• This is your central hub where you can see everything relevant to you.<br>"
                    + "• 🔄 Click <b>'Refresh'</b> to update all data instantly.<br>"
                    + "• 🚪 Click <b>'Logout'</b> when you're done working.<br>"
                    + "• ⏳ You will be <b>automatically logged out</b> after <b>20 minutes of inactivity</b>.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String ITEMS_HELP =
            "<html><br><br>📦 <b>Manage Items</b><br><br>"
                    + "• 🔍 Items can be <b>searched</b> by <b>all fields</b>.<br>"
                    + "• ✏️ You can <b>edit descriptions, add/edit notes, and upload images</b>.<br>"
                    + "• 📷 Images must be <b>JPG, PNG, or JPEG</b> format.<br>"
                    + "• 🚫 Items can also be <b>deactivated</b> if they are no longer in use.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String EMPLOYEES_HELP =
            "<html><br><br>👥 <b>Find Employees</b><br><br>"
                    + "• 🔍 You can <b>search</b> employees using <b>all fields displayed</b>.<br>"
                    + "• ⚡ <b>Only administrators</b> can add, edit, or remove employees.<br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String EDIT_PERMISSIONS_HELP =
            "<html><br><br>🔧 <b>Manage Employee Permissions</b><br><br>"
                    + "• 🔍 <b>Search for employees</b> by name or username.<br>"
                    + "• ✅ View an employee’s <b>current roles</b> at a glance.<br>"
                    + "• ➕ <b>Add or remove</b> roles to customize their permissions.<br>"
                    + "• 👤 Employee details are displayed when selecting an employee.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String ADD_EMPLOYEE_HELP =
            "<html><br><br>➕ <b>Add a New Employee</b><br><br>"
                    + "• 🆕 <b>New employees</b> are assigned a default password automatically.<br>"
                    + "• ✏️ <b>Username and email</b> are <b>auto-generated</b> based on the employee’s name.<br>"
                    + "• ✅ Assign a <b>Main Role</b> and a home site.<br>"
                    + "• 💾 Click <b>'Save'</b> to finalize the new employee.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String EDIT_EMPLOYEE_HELP =
            "<html><br><br>✏️ <b>Edit Employee Details</b><br><br>"
                    + "• 🔑 <b>Changing the password?</b> Enter a new one or <b>leave it blank</b> to keep it the same.<br>"
                    + "• ✅ Adjust the <b>Main Role</b> and modify the user's home site and names as needed.<br>"
                    + "• 💾 Click <b>'Save'</b> to apply changes.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String EDIT_ITEM_HELP =
            "<html><br><br>🖊️ <b>Edit Item Details</b><br><br>"
                    + "• ✅ You can edit an item's <b>notes</b>, <b>description</b>, and <b>images</b>.<br>"
                    + "• 📷 Images must be in <b>JPG, PNG,</b> or <b>JPEG</b> format.<br>"
                    + "• 🔄 Click <b>'Add Image</b> or <b>'Change Existing Image'</b> to upload a new image.<br>"
                    + "• 💾 Be sure to <b>save</b> your changes—they apply instantly!<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String SITES_HELP =
            "<html><br><br>📍 <b>View and Search Sites</b><br><br>"
                    + "• 🔍 You can <b>search</b> for sites using <b>all displayed fields</b>.<br>"
                    + "• 📋 Click a site to <b>view its details</b> including address, phone, and distance from the Bullseye Warehouse.<br>"
                    + "• 🚫 <b>Only administrators</b> can add or edit site information.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String ADD_EDIT_SITE_HELP =
            "<html><br><br>📍 <b>Add or Edit a Site</b><br><br>"
                    + "• 🏢 <b>Adding a new site?</b> Fill in the <b>site name, address, city, and province</b>.<br>"
                    + "• 📞 <b>Phone number</b> must include at least one digit.<br>"
                    + "• ✉️ <b>Postal code</b> should follow the <b>A2A 2A2</b> format.<br>"
                    + "• 📆 <b>Set a delivery day</b> and <b>distance</b> from the warehouse.<br>"
                    + "• ✅ Toggle <b>Active</b> status to enable or disable a site.<br>"
                    + "• 💾 Click <b>'Save'</b> to finalize changes.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String DASHBOARD_ORDER_VIEWER_HELP =
            "<html><br><br>📋 <b>Order Viewer</b><br><br>"
                    + "• 🔍 Use the <b>search bar</b> to quickly find specific orders.<br>"
                    + "• 📑 Filter orders using the <b>dropdown menus</b> to refine by status or site.<br>"
                    + "• ✅ Check the <b>'Show Only Active Orders'</b> box to view only orders that are still in progress.<br>"
                    + "• 📌 Click an order to <b>select it</b>, then press <b>'View/Modify'</b> for details and actions.<br>"
                    + "• 🔄 Click <b>'Refresh'</b> to reload the latest order data.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String VIEW_RECEIVE_ORDER_HELP =
            "<html><br><br>📦 <b>View & Receive Orders</b><br><br>"
                    + "• 👀 View order details, including <b>items, quantities, and order status</b>.<br>"
                    + "• 🔄 <b>Roles affect actions:</b> Different users can process orders at different times.<br>"
                    + "• ✅ Store Managers can <b>review and submit orders</b>.<br>"
                    + "• 🚚 Warehouse Managers can <b>receive, fulfill, and backorder items</b>.<br>"
                    + "• 📝 Use the <b>Notes</b> section to add comments before processing.<br>"
                    + "• 📋 Backorders are created automatically for unfulfilled items.<br>"
                    + "• 🚀 Click <b>'Confirm Received'</b> to process an order once it arrives.<br><br>"
                    + "<i>📧 For further assistance, reach out to <b>admin@bullseye.ca</b>.</i></html>";

    public static final String SUPPLIER_TABLE_VIEW = "This table provides an overview of all registered suppliers, including their contact details and address.\nWarehouse Managers can add and edit suppliers to ensure accurate and up-to-date supplier information.";

    public static final String TXN_TABLE_VIEW = "The Transaction Table provides a complete view of all transaction records in the system.\nAdministrators can edit records as long as the transaction has not yet left the warehouse.\nOnce a transaction is in transit or is Complete/Cancelled, it becomes read-only to maintain data integrity.";

    public static final String EDIT_TXN_VIEW = "The Edit Transactions view enables administrators to update details of an order before it leaves the warehouse.\nAdmins can change the order status, modify the ship date, assign a delivery ID, etc.\nOnce a transaction is in transit, editing is disabled to maintain reliable tracking.";

    public static final String ADD_EDIT_SUPPLIER_VIEW = "The Add/Edit Supplier view enables Warehouse Managers to create and update supplier records.\nThey can modify supplier names, addresses, and set supplier status as active or inactive to maintain accurate supplier management.";

    public static final String LOSSES_RETURNS_DASHBOARD = "The Losses/Returns Dashboard provides an overview of all loss, return, and damage transactions in the system.\nWarehouse/Store Managers can track the status of these transactions, filter by various criteria, and manage inventory discrepancies efficiently.";

    public static final String ADD_LOSS_RETURN_SCREEN = "The Add Loss/Return screen allows Warehouse/Store Managers to record losses, returns, or damages.\nUsers must select the appropriate type of transaction (Loss, Return, or Damage) and indicate if the item is resellable in case of a return.\nThis helps maintain accurate inventory records and ensures proper processing of returned items.";

    public static final String SUPPLIER_ORDERS_DASHBOARD = "The Supplier Orders Dashboard provides an overview of all orders placed with suppliers.\nWarehouse Managers can view the status of orders and keep track of pending or completed orders for smooth supply chain management.";

    public static final String SUPPLIER_ORDER_CREATE_SCREEN = "The Supplier Order screen allows Warehouse Managers to create new orders to suppliers.\nUsers can select items, specify quantities, and submit the order for processing.\nThis functionality ensures that the warehouse maintains optimal stock levels and orders are placed in a timely manner.";

    public static final String VIEW_ITEM_DETAILS_SUPPLIER_ORDER = "The Item Details view within the Supplier Order screen provides detailed information about a selected item.\nWarehouse Managers can review key details such as the item name, description, supplier, case size, weight, cost price, and more.\nThis feature allows users to verify item specifications before placing or editing a supplier order.";


}