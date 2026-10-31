import React, { useState } from 'react';
import Navbar from './components/Navbar';
import RoleSelector from './pages/RoleSelector';
import CustomerDashboard from './pages/CustomerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import ApiActivityMonitor from './components/ApiActivityMonitor';
import ServiceEventMonitor from './components/ServiceEventMonitor';

export default function App() {
  const [activeRole, setActiveRole] = useState(null); // null, 'CUSTOMER', 'ADMIN'
  const [showMonitors, setShowMonitors] = useState(false);
  const [activeMonitorTab, setActiveMonitorTab] = useState('API'); // 'API' or 'EVENTS'

  return (
    <div className="app-container">
      <Navbar
        activeRole={activeRole}
        onSwitchRole={setActiveRole}
        openMonitors={showMonitors}
        onToggleMonitors={() => setShowMonitors(!showMonitors)}
      />

      <main className="main-content">
        {!activeRole && <RoleSelector onSelectRole={setActiveRole} />}
        {activeRole === 'CUSTOMER' && <CustomerDashboard />}
        {activeRole === 'ADMIN' && <AdminDashboard />}
      </main>

      {/* Educational Monitors */}
      {showMonitors && (
        <aside className="monitors-drawer">
          <div className="drawer-header">
            <div className="flex-row items-center gap-2">
              <span className="font-semibold text-sm">EDUCATIONAL MONITORS</span>
              <div className="drawer-tab-group">
                <button
                  className={`drawer-tab-btn ${activeMonitorTab === 'API' ? 'active' : ''}`}
                  onClick={() => setActiveMonitorTab('API')}
                >
                  REST API Activity
                </button>
                <button
                  className={`drawer-tab-btn ${activeMonitorTab === 'EVENTS' ? 'active' : ''}`}
                  onClick={() => setActiveMonitorTab('EVENTS')}
                >
                  RabbitMQ Events
                </button>
              </div>
            </div>
            <button className="btn btn-icon btn-xs" onClick={() => setShowMonitors(false)}>
              ✕
            </button>
          </div>

          <div className="drawer-body">
            {activeMonitorTab === 'API' ? <ApiActivityMonitor /> : <ServiceEventMonitor />}
          </div>
        </aside>
      )}
    </div>
  );
}
