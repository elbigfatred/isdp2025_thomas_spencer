import os
import subprocess
import tkinter as tk
from tkinter import ttk

backend_path = "IMS-Backend/backend/src/main/java/tom/ims/backend/BackendApplication.java"
backend_project_dir = "IMS-Backend/backend"
react_frontend_dir = "IMS-Frontend_Web/frontEnd_web"
report_generator_dir = "IMS-Report_Generator"
desktop_project_dir = "IMS-Frontend_Desktop/frontEnd_desktop"
desktop_main_class = "org.example.Main"

# Process tracking
processes = {
    "Backend": None,
    "Frontend": None,
    "Report Generator": None,
    "Desktop GUI": None
}


def run_spring_boot_with_maven(project_dir, quiet=True):
    print("Launching Spring Boot app in background...")
    mvn_command = "./mvnw" if os.path.exists(
        os.path.join(project_dir, "mvnw")) else "mvn"
    try:
        process = subprocess.Popen(
            [mvn_command, "spring-boot:run"],
            cwd=project_dir,
            stdout=subprocess.DEVNULL if quiet else subprocess.PIPE,
            stderr=subprocess.DEVNULL if quiet else subprocess.STDOUT,
            text=True
        )
        return process
    except Exception as e:
        print(f"Spring Boot failed: {e}")
        return None


def run_react_vite_app(react_project_dir, quiet=True):
    print("Launching React (Vite) app in background...")
    try:
        process = subprocess.Popen(
            ["npm", "run", "dev"],
            cwd=react_project_dir,
            stdout=subprocess.DEVNULL if quiet else subprocess.PIPE,
            stderr=subprocess.DEVNULL if quiet else subprocess.STDOUT,
            text=True
        )
        return process
    except Exception as e:
        print(f"React failed: {e}")
        return None


def run_flask_app(project_dir, entry_point="app.py", port=5000, quiet=True):
    print("Launching Flask app in background...")
    env = os.environ.copy()
    env["FLASK_APP"] = entry_point
    env["FLASK_ENV"] = "development"
    try:
        process = subprocess.Popen(
            ["flask", "run", "--port", str(port)],
            cwd=project_dir,
            env=env,
            stdout=subprocess.DEVNULL if quiet else subprocess.PIPE,
            stderr=subprocess.DEVNULL if quiet else subprocess.STDOUT,
            text=True
        )
        return process
    except Exception as e:
        print(f"Flask failed: {e}")
        return None


def run_swing_app_with_maven(project_dir, main_class="org.example.Main", quiet=True):
    print("Launching Swing app via Maven...")
    mvn_command = "./mvnw" if os.path.exists(
        os.path.join(project_dir, "mvnw")) else "mvn"
    try:
        process = subprocess.Popen(
            [mvn_command, "exec:java", f"-Dexec.mainClass={main_class}"],
            cwd=project_dir,
            stdout=subprocess.DEVNULL if quiet else subprocess.PIPE,
            stderr=subprocess.DEVNULL if quiet else subprocess.STDOUT,
            text=True
        )
        return process
    except Exception as e:
        print(f"Swing failed: {e}")
        return None

# === GUI Logic ===


def run_process(label, fn):
    if processes[label] is not None:
        return
    proc = fn()
    if proc:
        processes[label] = proc
        update_status(label, "Running")
    else:
        update_status(label, "Failed")


def stop_process(label):
    proc = processes[label]
    if proc:
        proc.terminate()
        proc.wait()
        processes[label] = None
    update_status(label, "Stopped")


def update_status(label, status):
    status_labels[label].config(
        text=status, foreground="green" if status == "Running" else "red")


def stop_all():
    for label in processes:
        stop_process(label)


# === GUI Setup ===
root = tk.Tk()
root.title("Master App Launcher")
root.geometry("420x270")
root.resizable(False, False)

frame = ttk.Frame(root, padding=10)
frame.pack(fill="both", expand=True)

status_labels = {}


def create_row(name, launch_fn):
    row = ttk.Frame(frame)
    row.pack(fill="x", pady=5)

    label = ttk.Label(row, text=name, width=20)
    label.pack(side="left")

    status = ttk.Label(row, text="Stopped", width=12, foreground="red")
    status.pack(side="left", padx=5)
    status_labels[name] = status

    launch_btn = ttk.Button(
        row, text="Launch", command=lambda: run_process(name, launch_fn))
    launch_btn.pack(side="left", padx=5)

    stop_btn = ttk.Button(row, text="Stop", command=lambda: stop_process(name))
    stop_btn.pack(side="left")


# Row definitions
create_row("Backend", lambda: run_spring_boot_with_maven(backend_project_dir))
create_row("Frontend", lambda: run_react_vite_app(react_frontend_dir))
create_row("Report Generator", lambda: run_flask_app(report_generator_dir))
create_row("Desktop GUI", lambda: run_swing_app_with_maven(
    desktop_project_dir, main_class=desktop_main_class))

ttk.Separator(frame).pack(fill="x", pady=10)
ttk.Button(frame, text="Stop All", command=stop_all).pack()

root.mainloop()
