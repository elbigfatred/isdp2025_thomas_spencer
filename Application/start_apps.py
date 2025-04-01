import subprocess
import time
import os


# Function to start the backend using Maven command
def start_backend():
    print("Starting Spring Backend...")
    
    # Run the 'mvn spring-boot:run' command to start the backend
    # Adjust the directory to where your pom.xml is located
    process = subprocess.Popen(["java", "-jar", "target/your-backend-app.jar"], cwd="IMS-Backend/backend")    
    time.sleep(5)  # Allow the backend some time to start

    # Wait for the backend to complete, if desired
    process.communicate()  # This blocks until the backend stops

# Function to start the React frontend
def start_react_frontend():
    print("Starting React Frontend...")
    subprocess.Popen(["npm", "start"], cwd="IMS-Frontend_Web")  # Assuming the frontend is in IMS-Frontend_Web
    time.sleep(5)

# Function to start the Swing frontend
def start_swing_frontend():
    print("Starting Swing Frontend...")
    subprocess.Popen(["mvn", "clean", "install"], cwd="IMS-Frontend_Desktop")  # Assuming you use Maven for Swing
    time.sleep(5)

# Function to start the report generator
def start_report_generator():
    print("Starting Report Generator...")
    subprocess.Popen(["python", "app.py"], cwd="IMS-Report_Generator")  # Assuming your report generator is in this folder
    time.sleep(5)

# Main function to orchestrate the startup of all services
def main():
    # Start the Spring backend
    start_backend()
    
    # Start the React frontend
    start_react_frontend()
    
    # Start the Swing frontend
    start_swing_frontend()
    
    # Start the Report Generator
    start_report_generator()

if __name__ == "__main__":
    main()