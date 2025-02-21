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
}