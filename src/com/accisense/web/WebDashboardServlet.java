package com.accisense.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET / -> AcciSense Web Console: live telemetry HUD served by the servlet layer.
 * [RUBRIC: Servlet Implementation - dynamic web view]
 */
public class WebDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().print(PAGE);
    }

    private static final String PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>AcciSense Web Console</title>
<style>
:root{
  --bg:#070d1f; --bg2:#0b132b; --surface:#101a36; --surface2:#16224a; --line:#22305a;
  --ink:#e6edf7; --ink-dim:#93a4c3; --ink-faint:#5d6f92;
  --cyan:#22d3ee; --blue:#3b82f6; --magenta:#e879f9; --amber:#fbbf24;
  --green:#34d399; --orange:#fb923c; --red:#f87171;
  --low:#34d399; --medium:#fbbf24; --high:#fb923c; --critical:#f87171;
  --header-h:64px; --radius:14px;
  --grad-brand:linear-gradient(100deg,var(--cyan),var(--blue) 55%,var(--magenta));
}
*{box-sizing:border-box;margin:0;padding:0}
html{scroll-behavior:smooth}
body{
  background:var(--bg); color:var(--ink);
  font-family:"Segoe UI",Inter,Roboto,Arial,sans-serif;
  min-height:100vh; overflow-x:hidden;
}
body::before{
  content:""; position:fixed; inset:0; z-index:-2; pointer-events:none;
  background:
    repeating-linear-gradient(0deg,transparent 0 47px,rgba(34,211,238,.045) 47px 48px),
    repeating-linear-gradient(90deg,transparent 0 47px,rgba(59,130,246,.04) 47px 48px),
    radial-gradient(1100px 600px at 85% -10%,rgba(59,130,246,.16),transparent 60%),
    radial-gradient(900px 520px at -10% 110%,rgba(232,121,249,.10),transparent 60%),
    linear-gradient(180deg,var(--bg2),var(--bg) 40%);
}
body::after{
  content:""; position:fixed; inset:-40%; z-index:-1; pointer-events:none; opacity:.5;
  background:radial-gradient(600px 380px at 30% 30%,rgba(34,211,238,.07),transparent 65%),
             radial-gradient(700px 420px at 70% 65%,rgba(232,121,249,.06),transparent 65%);
  animation:drift 26s ease-in-out infinite alternate;
}
@keyframes drift{from{transform:translate3d(-3%,-2%,0) rotate(-1deg)}to{transform:translate3d(3%,2%,0) rotate(1deg)}}
::selection{background:rgba(34,211,238,.35)}
*{scrollbar-width:thin;scrollbar-color:var(--blue) var(--bg2)}
::-webkit-scrollbar{width:10px;height:10px}
::-webkit-scrollbar-track{background:var(--bg2)}
::-webkit-scrollbar-thumb{background:linear-gradient(180deg,var(--cyan),var(--blue));border-radius:8px;border:2px solid var(--bg2)}
::-webkit-scrollbar-thumb:hover{background:linear-gradient(180deg,var(--magenta),var(--blue))}
a{color:var(--cyan)}
button{font:inherit;cursor:pointer}
:focus-visible{outline:2px solid var(--cyan);outline-offset:2px;border-radius:6px}

header{
  position:fixed; top:0; left:0; right:0; height:var(--header-h); z-index:50;
  display:flex; align-items:center; gap:18px; padding:0 22px;
  background:rgba(7,13,31,.82); backdrop-filter:blur(14px);
  border-bottom:1px solid var(--line);
}
.brand{display:flex;align-items:baseline;gap:10px;white-space:nowrap}
.brand b{font-size:22px;letter-spacing:.5px;background:var(--grad-brand);-webkit-background-clip:text;background-clip:text;color:transparent}
.brand span{font-size:10px;letter-spacing:2.5px;color:var(--ink-dim)}
nav{display:flex;gap:4px;margin-left:8px}
nav a{
  color:var(--ink-dim); text-decoration:none; font-size:13px; padding:8px 12px; border-radius:9px;
  transition:color .2s,background .2s;
}
nav a:hover{color:var(--ink);background:rgba(34,211,238,.08)}
nav a.active{color:var(--cyan);background:rgba(34,211,238,.12);box-shadow:inset 0 -2px 0 var(--cyan)}
.head-right{margin-left:auto;display:flex;align-items:center;gap:10px}
.pill{
  display:inline-flex;align-items:center;gap:7px;font-size:11px;letter-spacing:1px;
  padding:6px 12px;border-radius:999px;border:1px solid var(--line);background:var(--surface);
}
.dot{width:8px;height:8px;border-radius:50%;background:var(--green);box-shadow:0 0 10px var(--green);animation:pulse 1.6s infinite}
.pill.paused .dot{background:var(--amber);box-shadow:0 0 10px var(--amber);animation:none}
.pill.offline .dot{background:var(--red);box-shadow:0 0 10px var(--red);animation:none}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.35}}
.icon-btn{
  display:inline-flex;align-items:center;gap:7px;padding:7px 12px;border-radius:10px;
  border:1px solid var(--line);background:var(--surface);color:var(--ink);font-size:12px;
  transition:border-color .2s,transform .15s,background .2s;
}
.icon-btn:hover{border-color:var(--cyan);transform:translateY(-1px)}
.icon-btn[aria-pressed="true"]{border-color:var(--cyan);background:rgba(34,211,238,.12);color:var(--cyan)}
#clock{font-variant-numeric:tabular-nums;font-size:12px;color:var(--ink-dim)}

.sim-tag{ margin:calc(var(--header-h) + 14px) auto 0; max-width:1240px; padding:0 22px; }
.sim-tag div{
  display:flex;align-items:center;gap:10px;font-size:11px;letter-spacing:1.6px;
  color:var(--amber);background:rgba(251,191,36,.08);border:1px solid rgba(251,191,36,.35);
  padding:8px 14px;border-radius:10px;
}
main{max-width:1240px;margin:0 auto;padding:18px 22px 60px}
section{scroll-margin-top:calc(var(--header-h) + 16px);margin-top:26px}
h2{font-size:15px;letter-spacing:2px;color:var(--ink-dim);text-transform:uppercase;margin-bottom:14px;display:flex;align-items:center;gap:10px}
h2::after{content:"";flex:1;height:1px;background:linear-gradient(90deg,var(--line),transparent)}
.reveal{opacity:0;transform:translateY(14px);animation:rise .6s forwards}
.reveal:nth-child(2){animation-delay:.08s}.reveal:nth-child(3){animation-delay:.16s}.reveal:nth-child(4){animation-delay:.24s}
@keyframes rise{to{opacity:1;transform:none}}

.stats-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:12px}
.stat{
  background:linear-gradient(160deg,var(--surface),var(--surface2));
  border:1px solid var(--line);border-radius:var(--radius);padding:16px 18px;position:relative;overflow:hidden;
  transition:transform .2s,border-color .2s,box-shadow .2s;
}
.stat:hover{transform:translateY(-3px);border-color:var(--accent,var(--cyan));box-shadow:0 10px 30px -18px var(--accent,var(--cyan))}
.stat::before{content:"";position:absolute;inset:0 auto 0 0;width:3px;background:var(--accent,var(--cyan))}
.stat small{display:block;font-size:10px;letter-spacing:2px;color:var(--ink-dim)}
.stat b{font-size:30px;font-variant-numeric:tabular-nums;color:var(--accent,var(--cyan))}
.dist{margin-top:12px;background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);padding:14px 18px}
.dist-bar{display:flex;height:14px;border-radius:8px;overflow:hidden;background:var(--bg2)}
.dist-bar span{transition:width .8s cubic-bezier(.2,.8,.2,1);position:relative}
.dist-bar span i{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-style:normal;font-size:9px;color:#04101f;font-weight:700}
.dist-legend{display:flex;gap:16px;flex-wrap:wrap;margin-top:10px;font-size:11px;color:var(--ink-dim)}
.dist-legend em{font-style:normal;display:inline-flex;align-items:center;gap:6px}
.swatch{width:9px;height:9px;border-radius:3px;display:inline-block}

.telemetry{display:grid;grid-template-columns:340px 1fr;gap:14px}
.panel{background:linear-gradient(165deg,var(--surface),var(--bg2));border:1px solid var(--line);border-radius:var(--radius);padding:18px;position:relative}
.panel h3{font-size:11px;letter-spacing:2px;color:var(--ink-dim);margin-bottom:10px}
.gauge-wrap{display:flex;flex-direction:column;align-items:center}
#needle{transition:transform .9s cubic-bezier(.3,1.4,.4,1);transform-origin:100px 100px}
.gauge-val{font-size:34px;font-variant-numeric:tabular-nums;margin-top:-46px;color:var(--ink)}
.gauge-val small{font-size:12px;color:var(--ink-dim)}
.gauge-range{display:flex;justify-content:space-between;width:100%;font-size:10px;color:var(--ink-faint);margin-top:26px}
#wave{width:100%;height:240px;display:block}
.legend{display:flex;gap:14px;align-items:center;font-size:11px;color:var(--ink-dim);margin-top:8px;flex-wrap:wrap}
.legend b{color:var(--ink);font-variant-numeric:tabular-nums}

.controls{
  display:flex;flex-wrap:wrap;gap:10px;align-items:center;
  background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);
  padding:12px 14px;margin-bottom:14px;position:sticky;top:calc(var(--header-h) + 8px);z-index:20;
  backdrop-filter:blur(10px);
}
.ctl{display:inline-flex;align-items:center;gap:8px;font-size:11px;color:var(--ink-dim)}
.ctl output{color:var(--cyan);font-variant-numeric:tabular-nums;min-width:34px}
input[type=range]{accent-color:var(--cyan);width:110px}
select,input[type=text],input[type=number]{
  background:var(--bg2);color:var(--ink);border:1px solid var(--line);border-radius:9px;
  padding:7px 10px;font-size:12px;transition:border-color .2s;
}
select:hover,input:hover{border-color:var(--blue)}
input[type=text]{min-width:170px}
.chips{display:flex;gap:6px;flex-wrap:wrap}
.chip{
  border:1px solid var(--line);background:var(--bg2);color:var(--ink-dim);
  padding:6px 12px;border-radius:999px;font-size:11px;letter-spacing:.6px;transition:all .2s;
}
.chip:hover{color:var(--ink);border-color:var(--blue)}
.chip.active{color:#04101f;font-weight:700;background:var(--c,var(--cyan));border-color:var(--c,var(--cyan));box-shadow:0 0 16px -4px var(--c,var(--cyan))}
.btn{
  border:1px solid var(--line);background:var(--surface2);color:var(--ink);
  padding:8px 14px;border-radius:10px;font-size:12px;transition:all .2s;
}
.btn:hover{border-color:var(--cyan);color:var(--cyan);transform:translateY(-1px)}
.btn.primary{background:var(--grad-brand);border:none;color:#04101f;font-weight:700}
.btn.primary:hover{filter:brightness(1.12);color:#04101f}
.btn:disabled{opacity:.5;cursor:wait;transform:none}

.table-wrap{background:var(--surface);border:1px solid var(--line);border-radius:var(--radius);overflow:auto;max-height:480px}
table{width:100%;border-collapse:collapse;font-size:13px;min-width:860px}
thead th{
  position:sticky;top:0;z-index:5;background:var(--surface2);color:var(--cyan);
  font-size:10px;letter-spacing:1.6px;text-align:left;padding:12px 14px;cursor:pointer;user-select:none;
  border-bottom:1px solid var(--line);white-space:nowrap;
}
thead th:hover{color:var(--magenta)}
thead th .arrow{color:var(--magenta)}
tbody td{padding:11px 14px;border-bottom:1px solid rgba(34,48,90,.5);font-variant-numeric:tabular-nums;white-space:nowrap}
body[data-density="compact"] tbody td{padding:6px 14px;font-size:12px}
tbody tr{cursor:pointer;transition:background .15s,box-shadow .15s;border-left:3px solid transparent}
tbody tr:hover{background:rgba(34,211,238,.06);box-shadow:inset 3px 0 0 var(--cyan)}
tbody tr.row-new{animation:flash 1.4s}
@keyframes flash{0%{background:rgba(34,211,238,.28)}100%{background:transparent}}
.sev{padding:3px 10px;border-radius:999px;font-size:10px;font-weight:700;letter-spacing:1px;color:#04101f}
.sev.LOW{background:var(--low)}.sev.MEDIUM{background:var(--medium)}.sev.HIGH{background:var(--high)}.sev.CRITICAL{background:var(--critical)}
.loc{color:var(--ink-dim);max-width:260px;overflow:hidden;text-overflow:ellipsis}
.alert-yes{color:var(--green)}.alert-no{color:var(--ink-faint)}
.skel td{padding:14px}
.skel span{display:block;height:12px;border-radius:6px;background:linear-gradient(90deg,var(--surface2) 25%,var(--line) 50%,var(--surface2) 75%);background-size:200% 100%;animation:shimmer 1.2s infinite}
@keyframes shimmer{to{background-position:-200% 0}}
.empty{padding:46px 20px;text-align:center;color:var(--ink-dim)}
.empty b{display:block;font-size:16px;color:var(--ink);margin-bottom:6px}

.offline-banner{
  display:none;align-items:center;gap:12px;margin-bottom:14px;padding:12px 16px;border-radius:12px;
  background:rgba(248,113,113,.1);border:1px solid rgba(248,113,113,.4);color:var(--red);font-size:13px;
}
.offline-banner.show{display:flex}

form.report{display:grid;grid-template-columns:repeat(6,1fr);gap:12px;align-items:end}
form.report label{display:flex;flex-direction:column;gap:6px;font-size:10px;letter-spacing:1.4px;color:var(--ink-dim)}
form.report .wide{grid-column:span 2}
form.report .actions{grid-column:1/-1;display:flex;gap:10px;align-items:center;flex-wrap:wrap}
.field-err{color:var(--red);font-size:11px;min-height:14px}

.drawer-scrim{position:fixed;inset:0;background:rgba(4,8,20,.6);backdrop-filter:blur(3px);opacity:0;pointer-events:none;transition:opacity .3s;z-index:60}
.drawer-scrim.open{opacity:1;pointer-events:auto}
.drawer{
  position:fixed;top:0;right:0;bottom:0;width:min(420px,92vw);z-index:61;
  background:linear-gradient(180deg,var(--surface),var(--bg2));border-left:1px solid var(--line);
  transform:translateX(102%);transition:transform .38s cubic-bezier(.2,.9,.25,1);
  padding:22px;overflow-y:auto;box-shadow:-30px 0 60px -30px #000;
}
.drawer.open{transform:none}
.drawer h3{font-size:18px;margin-bottom:4px}
.drawer .sub{color:var(--ink-dim);font-size:12px;margin-bottom:18px}
.kv{display:grid;grid-template-columns:1fr auto;gap:8px;font-size:13px;padding:9px 0;border-bottom:1px solid rgba(34,48,90,.5)}
.kv span{color:var(--ink-dim)}
.kv b{font-variant-numeric:tabular-nums}
.meter{margin:14px 0}
.meter small{display:flex;justify-content:space-between;font-size:10px;letter-spacing:1px;color:var(--ink-dim);margin-bottom:5px}
.meter .track{height:8px;border-radius:6px;background:var(--bg2);overflow:hidden}
.meter .fill{height:100%;border-radius:6px;transition:width .7s cubic-bezier(.2,.8,.2,1)}
.drawer .btn{margin-top:8px}
.close-x{position:absolute;top:14px;right:14px}

#toasts{position:fixed;right:18px;bottom:18px;z-index:80;display:flex;flex-direction:column;gap:10px}
.toast{
  display:flex;align-items:center;gap:10px;padding:12px 16px;border-radius:12px;font-size:13px;
  background:var(--surface2);border:1px solid var(--line);box-shadow:0 16px 40px -18px #000;
  animation:toast-in .3s cubic-bezier(.2,.9,.3,1.2);max-width:340px;
}
.toast.success{border-color:rgba(52,211,153,.5)}.toast.success i{color:var(--green)}
.toast.error{border-color:rgba(248,113,113,.5)}.toast.error i{color:var(--red)}
.toast.info i{color:var(--cyan)}
.toast i{font-style:normal;font-size:15px}
.toast.out{animation:toast-out .3s forwards}
@keyframes toast-in{from{opacity:0;transform:translateX(30px)}to{opacity:1;transform:none}}
@keyframes toast-out{to{opacity:0;transform:translateX(30px)}}

footer{border-top:1px solid var(--line);margin-top:40px;padding:22px;text-align:center;color:var(--ink-faint);font-size:12px}
footer b{color:var(--ink-dim)}
@media (max-width:980px){
  .stats-grid{grid-template-columns:repeat(2,1fr)}
  .telemetry{grid-template-columns:1fr}
  form.report{grid-template-columns:repeat(2,1fr)}
  form.report .wide{grid-column:span 2}
  nav{display:none}
  .controls{position:static}
}
</style>
</head>
<body data-density="comfortable">
<header>
  <div class="brand"><b>AcciSense</b><span>WEB CONSOLE</span></div>
  <nav id="nav">
    <a href="#overview" class="active">Overview</a>
    <a href="#telemetry">Telemetry</a>
    <a href="#records">Records</a>
    <a href="#report">Report</a>
  </nav>
  <div class="head-right">
    <span id="clock">--:--:--</span>
    <button class="icon-btn" id="soundBtn" aria-pressed="true" title="Toggle alert sound">SOUND ON</button>
    <button class="icon-btn" id="liveBtn" aria-pressed="true" title="Pause or resume live feed">LIVE</button>
    <span class="pill" id="statusPill"><span class="dot"></span><span id="statusText">LIVE</span></span>
  </div>
</header>

<div class="sim-tag"><div>⚠ SIMULATION DATA — NOT A REAL EMERGENCY SERVICE · NO REAL SMS OR GPS IS USED</div></div>

<main>
  <section id="overview" class="reveal">
    <h2>Fleet Overview</h2>
    <div class="stats-grid">
      <div class="stat" style="--accent:var(--cyan)"><small>TOTAL EVENTS</small><b id="stTotal">0</b></div>
      <div class="stat" style="--accent:var(--critical)"><small>CRITICAL</small><b id="stCritical">0</b></div>
      <div class="stat" style="--accent:var(--high)"><small>HIGH</small><b id="stHigh">0</b></div>
      <div class="stat" style="--accent:var(--medium)"><small>MEDIUM</small><b id="stMedium">0</b></div>
      <div class="stat" style="--accent:var(--low)"><small>LOW</small><b id="stLow">0</b></div>
    </div>
    <div class="dist">
      <div class="dist-bar" id="distBar"></div>
      <div class="dist-legend" id="distLegend"></div>
    </div>
  </section>

  <section id="telemetry" class="reveal">
    <h2>Live Telemetry</h2>
    <div class="telemetry">
      <div class="panel gauge-wrap">
        <h3>LATEST IMPACT FORCE</h3>
        <svg viewBox="0 0 200 150" width="260" height="195" role="img" aria-label="Impact gauge">
          <defs>
            <linearGradient id="ggrad" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#34d399"/><stop offset="50%" stop-color="#fbbf24"/><stop offset="100%" stop-color="#f87171"/>
            </linearGradient>
          </defs>
          <path d="M 32.45 139 A 78 78 0 1 1 167.55 139" fill="none" stroke="#16224a" stroke-width="14" stroke-linecap="round"/>
          <path d="M 32.45 139 A 78 78 0 1 1 167.55 139" fill="none" stroke="url(#ggrad)" stroke-width="8" stroke-linecap="round" opacity=".9"/>
          <g id="needle"><line x1="100" y1="100" x2="100" y2="36" stroke="#e6edf7" stroke-width="3" stroke-linecap="round"/><circle cx="100" cy="100" r="7" fill="#22d3ee"/></g>
        </svg>
        <div class="gauge-val"><span id="gaugeVal">0.0</span> <small>kN</small></div>
        <div class="gauge-range"><span>0</span><span id="gaugeMaxLabel">150 kN</span></div>
      </div>
      <div class="panel">
        <h3>EVENT WAVEFORM — IMPACT &amp; ACCELERATION</h3>
        <canvas id="wave"></canvas>
        <div class="legend">
          <em><span class="swatch" style="background:var(--cyan)"></span>Impact <b id="legImpact">–</b> kN</em>
          <em><span class="swatch" style="background:var(--magenta)"></span>Accel <b id="legAccel">–</b> m/s²</em>
          <em style="margin-left:auto">thresholds: 40 kN · 20 m/s²</em>
        </div>
      </div>
    </div>
  </section>

  <section id="records" class="reveal">
    <h2>Accident Records</h2>
    <div class="offline-banner" id="offlineBanner">⚠ Connection to servlet lost — showing last known data. <button class="btn" id="retryBtn">Retry now</button></div>
    <div class="controls">
      <div class="chips" id="filterChips">
        <button class="chip active" data-f="ALL" style="--c:var(--cyan)">ALL <span id="cAll"></span></button>
        <button class="chip" data-f="CRITICAL" style="--c:var(--critical)">CRITICAL</button>
        <button class="chip" data-f="HIGH" style="--c:var(--high)">HIGH</button>
        <button class="chip" data-f="MEDIUM" style="--c:var(--medium)">MEDIUM</button>
        <button class="chip" data-f="LOW" style="--c:var(--low)">LOW</button>
      </div>
      <input type="text" id="search" placeholder="Search id, time, location…  ( / )" aria-label="Search records">
      <label class="ctl">SORT
        <select id="sortSel">
          <option value="time-desc">Newest first</option>
          <option value="time-asc">Oldest first</option>
          <option value="impact-desc">Impact ↓</option>
          <option value="impact-asc">Impact ↑</option>
          <option value="severity-desc">Severity ↓</option>
          <option value="id-desc">ID ↓</option>
          <option value="custom" hidden>Custom</option>
        </select>
      </label>
      <label class="ctl">REFRESH <input type="range" id="intervalRange" min="1" max="10" step="0.5" value="2"><output id="intervalOut">2.0s</output></label>
      <label class="ctl">WINDOW <input type="range" id="windowRange" min="10" max="60" step="5" value="30"><output id="windowOut">30</output></label>
      <label class="ctl">GAUGE
        <select id="gaugeMaxSel"><option>100</option><option selected>150</option><option>250</option></select>
      </label>
      <label class="ctl">DENSITY
        <select id="densitySel"><option value="comfortable">Comfortable</option><option value="compact">Compact</option></select>
      </label>
      <button class="btn" id="exportJson">Export JSON</button>
      <button class="btn" id="exportCsv">Export CSV</button>
      <button class="btn" id="resetView">Reset view</button>
    </div>
    <div class="table-wrap">
      <table>
        <thead><tr>
          <th data-k="id">ID <span class="arrow"></span></th>
          <th data-k="time">TIME <span class="arrow"></span></th>
          <th data-k="severity">SEVERITY <span class="arrow"></span></th>
          <th data-k="impact">IMPACT kN <span class="arrow"></span></th>
          <th data-k="accel">ACCEL m/s² <span class="arrow"></span></th>
          <th data-k="gyro">GYRO °/s <span class="arrow"></span></th>
          <th data-k="location">LOCATION</th>
          <th data-k="alert">ALERT</th>
        </tr></thead>
        <tbody id="rows">
          <tr class="skel"><td colspan="8"><span></span></td></tr>
          <tr class="skel"><td colspan="8"><span></span></td></tr>
          <tr class="skel"><td colspan="8"><span></span></td></tr>
        </tbody>
      </table>
      <div class="empty" id="emptyState" hidden>
        <b>No records match this view</b>
        Adjust filters, or submit the first manual report below.
        <div style="margin-top:14px"><button class="btn primary" id="emptyGo">Create a report</button></div>
      </div>
    </div>
  </section>

  <section id="report" class="reveal">
    <h2>Manual Accident Report · doPost</h2>
    <div class="panel">
      <form class="report" id="reportForm" novalidate>
        <label>SEVERITY
          <select id="fSev"><option>LOW</option><option selected>MEDIUM</option><option>HIGH</option><option>CRITICAL</option></select>
        </label>
        <label>IMPACT (kN)<input type="number" id="fImp" step="0.1" min="0" value="85"></label>
        <label>ACCEL (m/s²)<input type="number" id="fAcc" step="0.1" min="0" value="28"></label>
        <label>GYRO (°/s)<input type="number" id="fGyr" step="0.1" min="0" value="140"></label>
        <label class="wide">LOCATION (optional)<input type="text" id="fLoc" placeholder="[MANUAL WEB REPORT - SIMULATION]"></label>
        <div class="actions">
          <button type="submit" class="btn primary" id="submitBtn">Submit report</button>
          <button type="button" class="btn" id="quickFill">Quick-fill typical crash</button>
          <span class="field-err" id="formErr"></span>
        </div>
      </form>
    </div>
  </section>
</main>

<footer>
  <b>AcciSense</b> · Servlet layer on embedded Tomcat, sharing the SQLite database with the Swing dashboard ·
  B.Tech CSE (AI &amp; ML), Galgotias University · Team: Arshan Khan, Dhruv Pandey, Kush Mishra, Kartik ·
  <a href="https://github.com/arshankhan1346/Accisense" target="_blank" rel="noopener">GitHub repository</a>
</footer>

<div class="drawer-scrim" id="scrim"></div>
<aside class="drawer" id="drawer" aria-label="Record details">
  <button class="icon-btn close-x" id="drawerClose">✕</button>
  <h3 id="dTitle">Record</h3>
  <div class="sub" id="dSub"></div>
  <div id="dBody"></div>
  <button class="btn" id="copyJson">Copy record JSON</button>
</aside>
<div id="toasts"></div>

<script>
(function(){
"use strict";
var NL = String.fromCharCode(10);
var SEV_ORDER = {LOW:0, MEDIUM:1, HIGH:2, CRITICAL:3};
var SEV_COLOR = {LOW:"#34d399", MEDIUM:"#fbbf24", HIGH:"#fb923c", CRITICAL:"#f87171"};
var state = {
  records: [], stats: null, filter: "ALL", query: "", sortKey: "time", sortDir: -1,
  live: true, intervalMs: 2000, sound: true, windowSize: 30, gaugeMax: 150,
  knownIds: {}, firstLoad: true, online: true, drawerId: null, timer: null, polling: false
};
function $(id){ return document.getElementById(id); }
function esc(s){ return String(s == null ? "" : s).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;"); }
function fmt(n){ return Number(n).toFixed(2); }
function guard(fn){ try{ fn(); }catch(e){ /* a render fault must never flip link status */ } }

/* ---------- sound: tied to visible new HIGH/CRITICAL rows ---------- */
var audioCtx = null;
document.addEventListener("pointerdown", function(){
  if(!audioCtx){ try{ audioCtx = new (window.AudioContext || window.webkitAudioContext)(); }catch(e){} }
}, {once:true});
function chime(){
  if(!state.sound || !audioCtx) return;
  try{
    var o = audioCtx.createOscillator(), g = audioCtx.createGain();
    o.type = "sine"; o.frequency.value = 880;
    g.gain.setValueAtTime(0.0001, audioCtx.currentTime);
    g.gain.exponentialRampToValueAtTime(0.12, audioCtx.currentTime + 0.02);
    g.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + 0.35);
    o.connect(g); g.connect(audioCtx.destination);
    o.start(); o.stop(audioCtx.currentTime + 0.4);
  }catch(e){}
}

/* ---------- toasts ---------- */
function toast(msg, type){
  var t = document.createElement("div");
  t.className = "toast " + (type || "info");
  t.innerHTML = "<i>" + (type === "success" ? "✔" : type === "error" ? "⚠" : "ℹ") + "</i><span>" + esc(msg) + "</span>";
  $("toasts").appendChild(t);
  setTimeout(function(){ t.classList.add("out"); setTimeout(function(){ t.remove(); }, 320); }, 3500);
}

/* ---------- data: endpoints polled independently ---------- */
function fetchJson(url){
  return fetch(url, {cache:"no-store"}).then(function(r){
    if(!r.ok) throw new Error("HTTP " + r.status);
    return r.json();
  });
}
function poll(){
  if(state.polling) return;
  state.polling = true;

  fetchJson("/api/accidents").then(function(list){
    if(!Array.isArray(list)) throw new Error("bad payload");
    var newIds = [];
    list.forEach(function(r){
      if(!state.knownIds[r.id]){ newIds.push(r); state.knownIds[r.id] = true; }
    });
    state.records = list;
    state.online = true;
    $("offlineBanner").classList.remove("show");
    setStatus();
    guard(function(){ renderTable(newIds); });
    guard(renderTelemetry);
    if(!state.firstLoad && newIds.length){
      var severe = newIds.some(function(r){ return r.severity === "HIGH" || r.severity === "CRITICAL"; });
      if(severe) chime();
      toast(newIds.length + " new event" + (newIds.length > 1 ? "s" : "") + " received", "info");
    }
    state.firstLoad = false;
    document.title = "AcciSense • " + list.length + " events";
  }).catch(function(){
    state.online = false;
    setStatus();
    $("offlineBanner").classList.add("show");
  }).finally(function(){ state.polling = false; });

  fetchJson("/api/stats").then(function(s){
    state.stats = s;
    guard(renderStats);
  }).catch(function(){ /* stats outage keeps last counts; link status follows records feed */ });
}
function restartTimer(){
  if(state.timer) clearInterval(state.timer);
  state.timer = setInterval(function(){ if(state.live) poll(); }, state.intervalMs);
}
function setStatus(){
  var pill = $("statusPill"), txt = $("statusText");
  pill.classList.remove("paused","offline");
  if(!state.online){ pill.classList.add("offline"); txt.textContent = "OFFLINE"; }
  else if(!state.live){ pill.classList.add("paused"); txt.textContent = "PAUSED"; }
  else { txt.textContent = "LIVE"; }
}

/* ---------- stats + distribution ---------- */
function renderStats(){
  var s = state.stats || {total:0,LOW:0,MEDIUM:0,HIGH:0,CRITICAL:0};
  $("stTotal").textContent = s.total; $("stCritical").textContent = s.CRITICAL;
  $("stHigh").textContent = s.HIGH; $("stMedium").textContent = s.MEDIUM; $("stLow").textContent = s.LOW;
  $("cAll").textContent = "(" + s.total + ")";
  var order = ["CRITICAL","HIGH","MEDIUM","LOW"], bar = "", leg = "";
  order.forEach(function(k){
    var pct = s.total ? (s[k] / s.total * 100) : 0;
    if(pct > 0){
      bar += '<span style="width:' + pct + '%;background:' + SEV_COLOR[k] + '"><i>' + (pct > 9 ? s[k] : "") + '</i></span>';
    }
    leg += '<em><span class="swatch" style="background:' + SEV_COLOR[k] + '"></span>' + k + ' · ' + s[k] + '</em>';
  });
  $("distBar").innerHTML = bar || '<span style="width:100%;background:var(--surface2)"></span>';
  $("distLegend").innerHTML = leg;
}

/* ---------- table ---------- */
function visibleRecords(){
  var q = state.query.toLowerCase();
  var list = state.records.filter(function(r){
    if(state.filter !== "ALL" && r.severity !== state.filter) return false;
    if(!q) return true;
    return (String(r.id) + " " + r.time + " " + r.severity + " " + r.location).toLowerCase().indexOf(q) !== -1;
  });
  var k = state.sortKey, d = state.sortDir;
  list.sort(function(a,b){
    var va = a[k], vb = b[k];
    if(k === "severity"){ va = SEV_ORDER[va]; vb = SEV_ORDER[vb]; }
    if(k === "alert"){ va = va ? 1 : 0; vb = vb ? 1 : 0; }
    if(typeof va === "string") return va.localeCompare(vb) * d;
    return (va - vb) * d;
  });
  return list;
}
function renderTable(newIds){
  var list = visibleRecords();
  var tb = $("rows");
  $("emptyState").hidden = list.length !== 0;
  if(!list.length){ tb.innerHTML = ""; return; }
  var fresh = {};
  (newIds || []).forEach(function(r){ fresh[r.id] = true; });
  tb.innerHTML = list.map(function(r){
    return '<tr data-id="' + r.id + '" class="' + (fresh[r.id] ? "row-new" : "") + '">' +
      '<td>' + r.id + '</td><td>' + esc(r.time) + '</td>' +
      '<td><span class="sev ' + r.severity + '">' + r.severity + '</span></td>' +
      '<td>' + fmt(r.impact) + '</td><td>' + fmt(r.accel) + '</td><td>' + fmt(r.gyro) + '</td>' +
      '<td class="loc" title="' + esc(r.location) + '">' + esc(r.location) + '</td>' +
      '<td class="' + (r.alert ? "alert-yes" : "alert-no") + '">' + (r.alert ? "YES" : "NO") + '</td></tr>';
  }).join("");
  document.querySelectorAll("thead th[data-k]").forEach(function(th){
    var a = th.querySelector(".arrow");
    a.textContent = th.dataset.k === state.sortKey ? (state.sortDir === 1 ? "▲" : "▼") : "";
  });
}

/* ---------- telemetry: gauge + waveform ---------- */
function renderTelemetry(){
  var latest = state.records[0];
  var impact = latest ? latest.impact : 0;
  var accel = latest ? latest.accel : 0;
  var deg = -120 + Math.max(0, Math.min(1, impact / state.gaugeMax)) * 240;
  $("needle").style.transform = "rotate(" + deg + "deg)";
  $("gaugeVal").textContent = fmt(impact);
  $("gaugeMaxLabel").textContent = state.gaugeMax + " kN";
  $("legImpact").textContent = fmt(impact);
  $("legAccel").textContent = fmt(accel);
  drawWave();
}
var wave = $("wave"), wctx = wave.getContext("2d");
function sizeWave(){
  var dpr = Math.min(2, window.devicePixelRatio || 1);
  var w = wave.clientWidth, h = wave.clientHeight;
  wave.width = Math.max(1, w * dpr); wave.height = Math.max(1, h * dpr);
  wctx.setTransform(dpr, 0, 0, dpr, 0, 0);
}
function drawWave(){
  sizeWave();
  var w = wave.clientWidth, h = wave.clientHeight;
  wctx.clearRect(0, 0, w, h);
  var padL = 40, padR = 44, padT = 14, padB = 24;
  var iw = w - padL - padR, ih = h - padT - padB;
  wctx.strokeStyle = "rgba(34,48,90,.7)"; wctx.lineWidth = 1;
  for(var g = 0; g <= 4; g++){
    var y = padT + ih * g / 4;
    wctx.beginPath(); wctx.moveTo(padL, y); wctx.lineTo(w - padR, y); wctx.stroke();
  }
  var data = state.records.slice(0, state.windowSize).reverse();
  function yImp(v){ return padT + ih * (1 - Math.min(1, v / state.gaugeMax)); }
  function yAcc(v){ return padT + ih * (1 - Math.min(1, v / 60)); }
  wctx.setLineDash([5,5]);
  wctx.strokeStyle = "rgba(34,211,238,.4)";
  wctx.beginPath(); wctx.moveTo(padL, yImp(40)); wctx.lineTo(w - padR, yImp(40)); wctx.stroke();
  wctx.strokeStyle = "rgba(232,121,249,.4)";
  wctx.beginPath(); wctx.moveTo(padL, yAcc(20)); wctx.lineTo(w - padR, yAcc(20)); wctx.stroke();
  wctx.setLineDash([]);
  wctx.fillStyle = "#5d6f92"; wctx.font = "10px Segoe UI";
  wctx.fillText("40 kN", 4, yImp(40) + 3);
  wctx.fillText("20 m/s²", w - padR + 6, yAcc(20) + 3);
  if(!data.length){
    wctx.fillStyle = "#5d6f92"; wctx.font = "12px Segoe UI";
    wctx.fillText("Awaiting telemetry…", padL + 10, padT + ih / 2);
    return;
  }
  function x(i){ return padL + (data.length === 1 ? iw / 2 : iw * i / (data.length - 1)); }
  function series(getY, color, fill){
    if(fill){
      var grad = wctx.createLinearGradient(0, padT, 0, padT + ih);
      grad.addColorStop(0, "rgba(34,211,238,.22)"); grad.addColorStop(1, "rgba(34,211,238,0)");
      wctx.beginPath(); wctx.moveTo(x(0), padT + ih);
      data.forEach(function(r,i){ wctx.lineTo(x(i), getY(r)); });
      wctx.lineTo(x(data.length - 1), padT + ih); wctx.closePath();
      wctx.fillStyle = grad; wctx.fill();
    }
    wctx.beginPath();
    data.forEach(function(r,i){ i ? wctx.lineTo(x(i), getY(r)) : wctx.moveTo(x(i), getY(r)); });
    wctx.strokeStyle = color; wctx.lineWidth = 2;
    wctx.shadowColor = color; wctx.shadowBlur = 8;
    wctx.stroke(); wctx.shadowBlur = 0;
    var lx = x(data.length - 1), ly = getY(data[data.length - 1]);
    wctx.beginPath(); wctx.arc(lx, ly, 3.5, 0, 7); wctx.fillStyle = color; wctx.fill();
  }
  series(function(r){ return yImp(r.impact); }, "#22d3ee", true);
  series(function(r){ return yAcc(r.accel); }, "#e879f9", false);
}
if(window.ResizeObserver){ new ResizeObserver(function(){ guard(drawWave); }).observe(wave); }
window.addEventListener("resize", function(){ guard(drawWave); });

/* ---------- drawer ---------- */
function openDrawer(id){
  var r = state.records.find(function(x){ return x.id === id; });
  if(!r) return;
  state.drawerId = id;
  $("dTitle").innerHTML = 'Accident #' + r.id + ' <span class="sev ' + r.severity + '">' + r.severity + '</span>';
  $("dSub").textContent = r.time;
  function meter(label, val, max, unit, color){
    var pct = Math.min(100, val / max * 100);
    return '<div class="meter"><small><span>' + label + '</span><span>' + fmt(val) + ' ' + unit + ' / ' + max + '</span></small>' +
      '<div class="track"><div class="fill" style="width:' + pct + '%;background:' + color + '"></div></div></div>';
  }
  $("dBody").innerHTML =
    '<div class="kv"><span>Impact force</span><b>' + fmt(r.impact) + ' kN</b></div>' +
    '<div class="kv"><span>Resultant accel</span><b>' + fmt(r.accel) + ' m/s²</b></div>' +
    '<div class="kv"><span>Resultant gyro</span><b>' + fmt(r.gyro) + ' °/s</b></div>' +
    '<div class="kv"><span>Alert sent</span><b>' + (r.alert ? "YES (simulated)" : "NO") + '</b></div>' +
    '<div class="kv"><span>Location</span><b style="max-width:220px;overflow:hidden;text-overflow:ellipsis">' + esc(r.location) + '</b></div>' +
    meter("IMPACT vs THRESHOLD", r.impact, 40, "kN", "#22d3ee") +
    meter("ACCEL vs THRESHOLD", r.accel, 20, "m/s²", "#e879f9") +
    meter("GYRO vs THRESHOLD", r.gyro, 120, "°/s", "#fbbf24");
  $("drawer").classList.add("open"); $("scrim").classList.add("open");
}
function closeDrawer(){ state.drawerId = null; $("drawer").classList.remove("open"); $("scrim").classList.remove("open"); }
$("rows").addEventListener("click", function(e){
  var tr = e.target.closest("tr[data-id]");
  if(tr) openDrawer(Number(tr.dataset.id));
});
$("drawerClose").addEventListener("click", closeDrawer);
$("scrim").addEventListener("click", closeDrawer);
document.addEventListener("keydown", function(e){
  if(e.key === "Escape") closeDrawer();
  if(e.key === "/" && document.activeElement.tagName !== "INPUT"){ e.preventDefault(); $("search").focus(); }
});
$("copyJson").addEventListener("click", function(){
  var r = state.records.find(function(x){ return x.id === state.drawerId; });
  if(!r) return;
  var txt = JSON.stringify(r, null, 2);
  function done(){ toast("Record JSON copied", "success"); }
  function fallback(){
    var ta = document.createElement("textarea"); ta.value = txt; document.body.appendChild(ta);
    ta.select(); try{ document.execCommand("copy"); done(); }catch(e){ toast("Copy failed", "error"); }
    ta.remove();
  }
  if(navigator.clipboard && navigator.clipboard.writeText){ navigator.clipboard.writeText(txt).then(done, fallback); }
  else fallback();
});

/* ---------- controls ---------- */
$("filterChips").addEventListener("click", function(e){
  var b = e.target.closest(".chip"); if(!b) return;
  state.filter = b.dataset.f;
  document.querySelectorAll("#filterChips .chip").forEach(function(c){ c.classList.toggle("active", c === b); });
  guard(function(){ renderTable(); });
});
$("search").addEventListener("input", function(){ state.query = this.value; guard(function(){ renderTable(); }); });
$("sortSel").addEventListener("change", function(){
  var p = this.value.split("-"); state.sortKey = p[0]; state.sortDir = p[1] === "asc" ? 1 : -1;
  guard(function(){ renderTable(); });
});
document.querySelectorAll("thead th[data-k]").forEach(function(th){
  th.addEventListener("click", function(){
    var k = th.dataset.k;
    if(state.sortKey === k) state.sortDir *= -1; else { state.sortKey = k; state.sortDir = -1; }
    var opt = k + (state.sortDir === 1 ? "-asc" : "-desc");
    var sel = $("sortSel");
    sel.value = Array.prototype.some.call(sel.options, function(o){ return o.value === opt; }) ? opt : "custom";
    guard(function(){ renderTable(); });
  });
});
$("intervalRange").addEventListener("input", function(){
  state.intervalMs = Number(this.value) * 1000;
  $("intervalOut").textContent = Number(this.value).toFixed(1) + "s";
  restartTimer();
});
$("windowRange").addEventListener("input", function(){
  state.windowSize = Number(this.value); $("windowOut").textContent = this.value; guard(drawWave);
});
$("gaugeMaxSel").addEventListener("change", function(){ state.gaugeMax = Number(this.value); guard(renderTelemetry); });
$("densitySel").addEventListener("change", function(){ document.body.dataset.density = this.value; });
$("liveBtn").addEventListener("click", function(){
  state.live = !state.live;
  this.setAttribute("aria-pressed", state.live);
  this.textContent = state.live ? "LIVE" : "PAUSED";
  setStatus(); if(state.live) poll();
});
$("soundBtn").addEventListener("click", function(){
  state.sound = !state.sound;
  this.setAttribute("aria-pressed", state.sound);
  this.textContent = state.sound ? "SOUND ON" : "MUTED";
  toast(state.sound ? "Alert sound enabled" : "All sound muted", "info");
});
$("retryBtn").addEventListener("click", poll);
$("resetView").addEventListener("click", function(){
  state.filter = "ALL"; state.query = ""; state.sortKey = "time"; state.sortDir = -1;
  state.windowSize = 30; state.gaugeMax = 150; state.intervalMs = 2000;
  $("search").value = ""; $("sortSel").value = "time-desc";
  $("windowRange").value = 30; $("windowOut").textContent = "30";
  $("gaugeMaxSel").value = "150"; $("intervalRange").value = 2; $("intervalOut").textContent = "2.0s";
  $("densitySel").value = "comfortable"; document.body.dataset.density = "comfortable";
  document.querySelectorAll("#filterChips .chip").forEach(function(c){ c.classList.toggle("active", c.dataset.f === "ALL"); });
  restartTimer(); guard(function(){ renderTable(); }); guard(renderTelemetry);
  toast("View reset", "info");
});
function download(name, mime, text){
  var blob = new Blob([text], {type:mime});
  var a = document.createElement("a");
  a.href = URL.createObjectURL(blob); a.download = name;
  document.body.appendChild(a); a.click(); a.remove();
  setTimeout(function(){ URL.revokeObjectURL(a.href); }, 800);
}
$("exportJson").addEventListener("click", function(){
  download("accisense-records.json", "application/json", JSON.stringify(visibleRecords(), null, 2));
  toast("JSON exported", "success");
});
$("exportCsv").addEventListener("click", function(){
  var rows = [["id","time","severity","impact_kN","accel_ms2","gyro_degs","location","alert_sent"]];
  visibleRecords().forEach(function(r){
    rows.push([r.id, r.time, r.severity, r.impact, r.accel, r.gyro, '"' + String(r.location).replace(/"/g,'""') + '"', r.alert ? 1 : 0]);
  });
  download("accisense-records.csv", "text/csv", rows.map(function(r){ return r.join(","); }).join(NL));
  toast("CSV exported", "success");
});
$("emptyGo").addEventListener("click", function(){ location.hash = "#report"; $("fImp").focus(); });

/* ---------- create (doPost) ---------- */
$("quickFill").addEventListener("click", function(){
  $("fSev").value = "HIGH"; $("fImp").value = 85; $("fAcc").value = 28; $("fGyr").value = 140;
  $("fLoc").value = ""; toast("Typical crash values loaded", "info");
});
$("reportForm").addEventListener("submit", function(e){
  e.preventDefault();
  var err = $("formErr"); err.textContent = "";
  var imp = Number($("fImp").value), acc = Number($("fAcc").value), gyr = Number($("fGyr").value);
  if([imp,acc,gyr].some(function(v){ return isNaN(v) || v < 0; })){
    err.textContent = "Impact, accel and gyro must be numbers ≥ 0."; return;
  }
  var btn = $("submitBtn"); btn.disabled = true; btn.textContent = "Saving…";
  var p = new URLSearchParams();
  p.set("severity", $("fSev").value); p.set("impact", imp); p.set("accel", acc); p.set("gyro", gyr);
  p.set("location", $("fLoc").value);
  fetch("/api/accidents", {method:"POST", body:p})
    .then(function(r){ if(!r.ok) throw new Error("HTTP " + r.status); return r.json(); })
    .then(function(j){
      toast("Report saved as accident #" + j.id, "success");
      $("fLoc").value = "";
      poll();
    })
    .catch(function(ex){ toast("Save failed: " + ex.message, "error"); err.textContent = "Server rejected the report."; })
    .finally(function(){ btn.disabled = false; btn.textContent = "Submit report"; });
});

/* ---------- nav + clock ---------- */
var links = document.querySelectorAll("#nav a");
if(window.IntersectionObserver){
  var io = new IntersectionObserver(function(entries){
    entries.forEach(function(en){
      if(en.isIntersecting){
        links.forEach(function(l){ l.classList.toggle("active", l.getAttribute("href") === "#" + en.target.id); });
      }
    });
  }, {rootMargin:"-40% 0px -55% 0px"});
  ["overview","telemetry","records","report"].forEach(function(id){ io.observe($(id)); });
}
setInterval(function(){ $("clock").textContent = new Date().toLocaleTimeString("en-GB"); }, 1000);
$("clock").textContent = new Date().toLocaleTimeString("en-GB");

/* ---------- boot ---------- */
restartTimer();
poll();
})();
</script>
</body>
</html>
""";
}