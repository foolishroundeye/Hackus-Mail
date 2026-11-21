#!/usr/bin/env python3
import http.server
import json
import os
import socket
import sys
import time
import urllib.parse
from collections import Counter

PORT = 8080
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
APK_PATH = os.path.abspath(os.path.join(BASE_DIR, "..", "..", "HackusMailMobile.apk"))

class RequestHandler(http.server.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        # Serve files from web-simulator directory
        clean_path = path.split('?', 1)[0].split('#', 1)[0]
        if clean_path == "/" or clean_path == "":
            return os.path.join(BASE_DIR, "index.html")
        return os.path.join(BASE_DIR, clean_path.lstrip("/"))

    def do_HEAD(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/download-apk":
            if os.path.exists(APK_PATH):
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="HackusMailMobile.apk"')
                self.send_header("Content-Length", str(os.path.getsize(APK_PATH)))
                self.end_headers()
                return
            else:
                self.send_error(404, "APK not found")
                return
        super().do_HEAD()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/download-apk":
            if os.path.exists(APK_PATH):
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="HackusMailMobile.apk"')
                self.send_header("Content-Length", str(os.path.getsize(APK_PATH)))
                self.end_headers()
                with open(APK_PATH, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
                return
            else:
                self.send_error(404, "APK not found")
                return

        if parsed.path == "/api/status":
            self.send_json({
                "app": "Hackus Mail Mobile (Reforged Suite)",
                "version": "2.0.0",
                "apkAvailable": os.path.exists(APK_PATH),
                "apkSize": os.path.getsize(APK_PATH) if os.path.exists(APK_PATH) else 0
            })
            return

        super().do_GET()

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(length) if length > 0 else b"{}"

        try:
            data = json.loads(post_data.decode("utf-8"))
        except Exception:
            data = {}

        if parsed.path == "/api/tools/normalize":
            text = data.get("text", "")
            lines = text.splitlines()
            out = []
            for line in lines:
                clean = line.strip().strip('"').strip("'")
                clean = clean.replace("\t", ":").replace(";", ":").replace("|", ":")
                parts = clean.split(":")
                if len(parts) >= 2:
                    email = parts[0].strip().lower()
                    pwd = ":".join(parts[1:]).strip()
                    if "@" in email and "." in email.split("@")[-1]:
                        out.append(f"{email}:{pwd}")
            self.send_json({
                "output": "\n".join(out),
                "originalCount": len(lines),
                "processedCount": len(out),
                "details": f"Normalized {len(out)} valid combos (removed {len(lines) - len(out)} invalid/empty lines)"
            })
            return

        if parsed.path == "/api/tools/distinct":
            text = data.get("text", "")
            lines = [l.strip() for l in text.splitlines() if l.strip()]
            seen = set()
            out = []
            for l in lines:
                if l not in seen:
                    seen.add(l)
                    out.append(l)
            self.send_json({
                "output": "\n".join(out),
                "originalCount": len(lines),
                "processedCount": len(out),
                "details": f"Removed {len(lines) - len(out)} duplicates ({len(out)} unique lines remaining)"
            })
            return

        if parsed.path == "/api/tools/sort-domains":
            text = data.get("text", "")
            lines = [l.strip() for l in text.splitlines() if l.strip()]
            domains = {}
            for l in lines:
                email = l.split(":")[0] if ":" in l else l
                domain = email.split("@")[-1].lower() if "@" in email else "other"
                domains.setdefault(domain, []).append(l)

            sorted_domains = sorted(domains.items(), key=lambda x: len(x[1]), reverse=True)
            out = []
            stats = []
            total = max(len(lines), 1)
            for dom, dom_lines in sorted_domains:
                count = len(dom_lines)
                pct = round((count / total) * 100, 1)
                stats.append({"domain": dom, "count": count, "percentage": pct})
                out.extend(dom_lines)

            self.send_json({
                "output": "\n".join(out),
                "originalCount": len(lines),
                "processedCount": len(out),
                "details": f"Identified {len(sorted_domains)} unique domains across {len(lines)} accounts",
                "stats": stats
            })
            return

        if parsed.path == "/api/test-proxy":
            host = data.get("host", "127.0.0.1")
            port = int(data.get("port", 1080))
            start = time.time()
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.settimeout(2.5)
                s.connect((host, port))
                s.close()
                latency = int((time.time() - start) * 1000)
                self.send_json({"alive": True, "latencyMs": latency})
            except Exception:
                self.send_json({"alive": False, "latencyMs": -1})
            return

        self.send_error(404, "Endpoint not found")

    def send_json(self, payload):
        resp = json.dumps(payload).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(resp)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(resp)

def run():
    server = http.server.ThreadingHTTPServer(("0.0.0.0", PORT), RequestHandler)
    print(f"Hackus Mail Mobile Simulator & APK Server running on http://0.0.0.0:{PORT}")
    server.serve_forever()

if __name__ == "__main__":
    run()
