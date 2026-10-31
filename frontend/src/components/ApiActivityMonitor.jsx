import React, { useState, useEffect } from 'react';
import { getApiLogs, subscribeApiLogs, clearApiLogs } from '../services/api';
import { Trash2, ChevronDown, ChevronRight, Activity, Clock, CheckCircle2, AlertCircle, Copy, Check } from 'lucide-react';

export default function ApiActivityMonitor() {
  const [logs, setLogs] = useState(getApiLogs());
  const [expandedId, setExpandedId] = useState(null);
  const [methodFilter, setMethodFilter] = useState('ALL');
  const [copiedId, setCopiedId] = useState(null);

  useEffect(() => {
    const unsubscribe = subscribeApiLogs((newLogs) => {
      setLogs(newLogs);
    });
    return unsubscribe;
  }, []);

  const toggleExpand = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  const copyPayload = (id, data) => {
    navigator.clipboard.writeText(JSON.stringify(data, null, 2));
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getMethodBadgeClass = (method) => {
    switch (method) {
      case 'GET': return 'badge-method-get';
      case 'POST': return 'badge-method-post';
      case 'PUT': return 'badge-method-put';
      case 'DELETE': return 'badge-method-delete';
      default: return 'badge-neutral';
    }
  };

  const filteredLogs = logs.filter((log) => {
    if (methodFilter === 'ALL') return true;
    return log.method === methodFilter;
  });

  return (
    <div className="monitor-container">
      <div className="monitor-toolbar">
        <div className="flex-row items-center gap-2">
          <span className="font-semibold font-sm text-primary">API ACTIVITY MONITOR</span>
          <span className="badge badge-neutral font-xs">{filteredLogs.length} / {logs.length} logged</span>
        </div>

        <div className="flex-row items-center gap-2">
          <div className="filter-pill-group">
            {['ALL', 'GET', 'POST', 'PUT', 'DELETE'].map((m) => (
              <button
                key={m}
                className={`filter-pill-btn ${methodFilter === m ? 'active' : ''}`}
                onClick={() => setMethodFilter(m)}
              >
                {m}
              </button>
            ))}
          </div>

          <button className="btn btn-outline btn-xs" onClick={clearApiLogs} title="Clear API Logs">
            <Trash2 size={13} /> Clear Logs
          </button>
        </div>
      </div>

      {filteredLogs.length === 0 ? (
        <div className="monitor-empty">
          <Activity size={28} className="empty-icon text-muted" />
          <p>No Axios HTTP requests matching filter. Click buttons in Customer or Admin dashboards to see actual REST traffic.</p>
        </div>
      ) : (
        <div className="monitor-log-list">
          {filteredLogs.map((log) => {
            const isExpanded = expandedId === log.id;
            const isError = log.status >= 400 || log.isError;

            return (
              <div key={log.id} className={`monitor-log-item ${isError ? 'log-item-error' : ''}`}>
                <div className="log-summary" onClick={() => toggleExpand(log.id)}>
                  <div className="log-summary-left">
                    {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                    <span className={`badge-method ${getMethodBadgeClass(log.method)}`}>
                      {log.method}
                    </span>
                    <span className="log-url" title={log.fullUrl}>{log.url}</span>
                    <span className="log-service-tag">{log.service}</span>
                  </div>

                  <div className="log-summary-right">
                    <span className={`badge-status ${isError ? 'status-err' : 'status-ok'}`}>
                      {log.status}
                    </span>
                    <span className="log-duration">{log.duration}ms</span>
                    <span className="log-time">{log.timestamp}</span>
                  </div>
                </div>

                {isExpanded && (
                  <div className="log-details">
                    <div className="log-meta-bar">
                      <span><strong>Target URL:</strong> <code>{log.fullUrl}</code></span>
                      <span><strong>Round-Trip:</strong> {log.duration}ms</span>
                      <span><strong>HTTP Status:</strong> {log.status}</span>
                    </div>

                    <div className="log-payload-grid">
                      <div className="log-payload-box">
                        <div className="payload-header">
                          <span className="payload-label">Request Payload</span>
                          {log.requestPayload && (
                            <button
                              className="btn btn-icon btn-xs text-muted"
                              onClick={(e) => { e.stopPropagation(); copyPayload(`req-${log.id}`, log.requestPayload); }}
                              title="Copy JSON"
                            >
                              {copiedId === `req-${log.id}` ? <Check size={12} className="text-success" /> : <Copy size={12} />}
                            </button>
                          )}
                        </div>
                        <pre className="payload-code">
                          {log.requestPayload
                            ? JSON.stringify(log.requestPayload, null, 2)
                            : '(No Request Body)'}
                        </pre>
                      </div>

                      <div className="log-payload-box">
                        <div className="payload-header">
                          <span className="payload-label">Response Payload</span>
                          {log.responsePayload && (
                            <button
                              className="btn btn-icon btn-xs text-muted"
                              onClick={(e) => { e.stopPropagation(); copyPayload(`res-${log.id}`, log.responsePayload); }}
                              title="Copy JSON"
                            >
                              {copiedId === `res-${log.id}` ? <Check size={12} className="text-success" /> : <Copy size={12} />}
                            </button>
                          )}
                        </div>
                        <pre className="payload-code">
                          {log.responsePayload
                            ? JSON.stringify(log.responsePayload, null, 2)
                            : '(Empty Response)'}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
