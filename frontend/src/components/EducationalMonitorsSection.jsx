import React, { useState } from 'react';
import ApiActivityMonitor from './ApiActivityMonitor';
import ServiceEventMonitor from './ServiceEventMonitor';
import { Activity, Radio, ChevronUp, ChevronDown } from 'lucide-react';

export default function EducationalMonitorsSection() {
  const [activeTab, setActiveTab] = useState('API'); // 'API' or 'EVENTS'
  const [collapsed, setCollapsed] = useState(false);

  return (
    <section className="educational-monitors-panel card">
      <div className="card-header monitor-panel-header">
        <div className="flex-row items-center gap-2">
          <div className="monitor-badge-icon">
            <Activity size={18} />
          </div>
          <div>
            <h2 className="card-title">Educational SOA Monitors</h2>
            <p className="card-subtitle">
              Live observability into Axios REST communications and RabbitMQ asynchronous messaging events.
            </p>
          </div>
        </div>

        <div className="flex-row items-center gap-2">
          <div className="btn-group-pill">
            <button
              className={`pill-btn ${activeTab === 'API' ? 'active' : ''}`}
              onClick={() => { setActiveTab('API'); setCollapsed(false); }}
            >
              <Activity size={13} className="inline mr-1" />
              API ACTIVITY MONITOR
            </button>
            <button
              className={`pill-btn ${activeTab === 'EVENTS' ? 'active' : ''}`}
              onClick={() => { setActiveTab('EVENTS'); setCollapsed(false); }}
            >
              <Radio size={13} className="inline mr-1" />
              SERVICE EVENT MONITOR
            </button>
          </div>

          <button
            className="btn btn-outline btn-xs"
            onClick={() => setCollapsed(!collapsed)}
            title={collapsed ? 'Expand Monitors' : 'Collapse Monitors'}
          >
            {collapsed ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
            <span>{collapsed ? 'Expand' : 'Collapse'}</span>
          </button>
        </div>
      </div>

      {!collapsed && (
        <div className="monitor-panel-body">
          {activeTab === 'API' ? <ApiActivityMonitor /> : <ServiceEventMonitor />}
        </div>
      )}
    </section>
  );
}
