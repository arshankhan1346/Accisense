package com.accisense.web;

import com.accisense.storage.DatabaseManager;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.awt.Desktop;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Comparator;

/**
 * Embedded Tomcat container hosting the servlet layer beside the Swing GUI.
 * Boots on the first free port, verifies itself over HTTP, then opens the
 * default browser automatically.
 * [RUBRIC: Servlet Implementation - container bootstrap + URL mappings]
 */
public class WebServer {

    private final DatabaseManager db;
    private final int preferredPort;
    private Tomcat tomcat;
    private File baseDir;
    private int port = -1;

    public WebServer(DatabaseManager db, int port) {
        this.db = db;
        this.preferredPort = port;
    }

    public synchronized void start() throws Exception {
        Exception last = null;
        for (int p = preferredPort; p < preferredPort + 10; p++) {
            try {
                boot(p);
                port = p;
                break;
            } catch (Exception e) {
                last = e;
                silentStop();
            }
        }
        if (port < 0) {
            throw (last != null) ? last : new IllegalStateException("No free port found");
        }

        String url = "http://localhost:" + port;
        if (selfTest(url)) {
            System.out.println("[WEB] Verified HTTP 200 from " + url + "/api/stats");
        }
        System.out.println("==================================================");
        System.out.println("[WEB] Console live at " + url);
        System.out.println("==================================================");
        openBrowser(url);
    }

    private void boot(int p) throws Exception {
        baseDir = Files.createTempDirectory("accisense-tomcat-").toFile();
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(p);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.getAbsolutePath());
        ctx.setIgnoreAnnotations(true);   // no annotation-scanning dependencies required

        Tomcat.addServlet(ctx, "console", new WebDashboardServlet());
        ctx.addServletMappingDecoded("/", "console");

        Tomcat.addServlet(ctx, "accidents", new AccidentApiServlet(db));
        ctx.addServletMappingDecoded("/api/accidents", "accidents");

        Tomcat.addServlet(ctx, "stats", new StatsApiServlet(db));
        ctx.addServletMappingDecoded("/api/stats", "stats");

        tomcat.start();
    }

    private boolean selfTest(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url + "/api/stats").openConnection();
            c.setConnectTimeout(2500);
            c.setReadTimeout(2500);
            int code = c.getResponseCode();
            c.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("[WEB] Default browser opened.");
                return;
            }
        } catch (Throwable t) {
            // fall through to manual instruction
        }
        System.out.println("[WEB] Open this URL manually: " + url);
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized void stop() {
        silentStop();
        if (baseDir != null && baseDir.exists()) {
            try {
                Files.walk(baseDir.toPath())
                     .sorted(Comparator.reverseOrder())
                     .forEach(path -> path.toFile().delete());
            } catch (Exception ignored) { }
        }
    }

    private void silentStop() {
        if (tomcat != null) {
            try { tomcat.stop(); } catch (Exception ignored) { }
            try { tomcat.destroy(); } catch (Exception ignored) { }
            tomcat = null;
        }
    }
}