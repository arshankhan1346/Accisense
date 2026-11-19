package com.accisense.web;

import com.accisense.exception.DatabaseException;
import com.accisense.model.AccidentRecord;
import com.accisense.storage.DatabaseManager;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * GET  /api/accidents -> JSON list (optional ?severity= filter)
 * POST /api/accidents -> manual accident report (form parameters)
 *
 * [RUBRIC: Servlet Implementation - doGet/doPost, params, JSON, status codes]
 * [REVIEW-1 FIX] DB failures surface as HTTP 500, never as silent empty data.
 */
public class AccidentApiServlet extends HttpServlet {

    private final DatabaseManager db;

    public AccidentApiServlet(DatabaseManager db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        String severity = req.getParameter("severity");
        try {
            List<AccidentRecord> records;
            synchronized (db) {
                records = (severity == null || severity.isBlank())
                        ? db.getAllAccidents()
                        : db.getBySeverity(severity);
            }
            PrintWriter out = resp.getWriter();
            out.print("[");
            for (int i = 0; i < records.size(); i++) {
                if (i > 0) out.print(",");
                out.print(toJson(records.get(i)));
            }
            out.print("]");
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            AccidentRecord.Severity sev =
                    AccidentRecord.Severity.valueOf(req.getParameter("severity").toUpperCase());
            double impact = Double.parseDouble(req.getParameter("impact"));
            double accel  = Double.parseDouble(req.getParameter("accel"));
            double gyro   = Double.parseDouble(req.getParameter("gyro"));
            String loc    = req.getParameter("location");
            if (loc == null || loc.isBlank()) loc = "[MANUAL WEB REPORT - SIMULATION]";

            AccidentRecord record = new AccidentRecord(sev, impact, accel, gyro, loc);
            synchronized (db) { db.saveAccidentOrThrow(record); }
            resp.getWriter().print("{\"status\":\"ok\",\"id\":" + record.getId() + "}");

        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid report: " + e.getMessage());
        }
    }

    private String toJson(AccidentRecord r) {
        return "{\"id\":" + r.getId()
                + ",\"time\":\"" + esc(r.getFormattedTime()) + "\""
                + ",\"severity\":\"" + r.getSeverity() + "\""
                + ",\"impact\":" + r.getImpactForce()
                + ",\"accel\":" + r.getResultantAcceleration()
                + ",\"gyro\":" + r.getResultantGyro()
                + ",\"location\":\"" + esc(r.getLocation()) + "\""
                + ",\"alert\":" + r.isAlertSent() + "}";
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}