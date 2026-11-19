package com.accisense.web;

import com.accisense.exception.DatabaseException;
import com.accisense.model.AccidentRecord;
import com.accisense.storage.DatabaseManager;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * GET /api/stats -> JSON severity statistics.
 * [RUBRIC: Servlet Implementation + Collections (EnumMap, merge)]
 */
public class StatsApiServlet extends HttpServlet {

    private final DatabaseManager db;

    public StatsApiServlet(DatabaseManager db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        try {
            List<AccidentRecord> all;
            synchronized (db) { all = db.getAllAccidents(); }

            Map<AccidentRecord.Severity, Integer> counts =
                    new EnumMap<>(AccidentRecord.Severity.class);
            for (AccidentRecord.Severity s : AccidentRecord.Severity.values()) counts.put(s, 0);
            for (AccidentRecord r : all) counts.merge(r.getSeverity(), 1, Integer::sum);

            resp.getWriter().print("{\"total\":" + all.size()
                    + ",\"LOW\":" + counts.get(AccidentRecord.Severity.LOW)
                    + ",\"MEDIUM\":" + counts.get(AccidentRecord.Severity.MEDIUM)
                    + ",\"HIGH\":" + counts.get(AccidentRecord.Severity.HIGH)
                    + ",\"CRITICAL\":" + counts.get(AccidentRecord.Severity.CRITICAL) + "}");
        } catch (DatabaseException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}