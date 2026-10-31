import React from 'react';
import { ShoppingBag, ShieldCheck, ArrowLeftRight, Activity } from 'lucide-react';

export default function Navbar({ activeRole, onSwitchRole, openMonitors, onToggleMonitors }) {
  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand" onClick={() => onSwitchRole(null)}>
          <div className="brand-logo">
            <ShoppingBag className="icon-logo" />
          </div>
          <div>
            <span className="brand-title">SmartStock</span>
            <span className="brand-subtitle">SOA Order & Inventory System</span>
          </div>
        </div>

        <div className="navbar-actions">
          {activeRole && (
            <div className="role-badge">
              {activeRole === 'CUSTOMER' ? (
                <span className="badge badge-customer">
                  <ShoppingBag size={14} /> Customer Panel
                </span>
              ) : (
                <span className="badge badge-admin">
                  <ShieldCheck size={14} /> Admin Portal
                </span>
              )}
            </div>
          )}

          <button
            className="btn btn-secondary btn-sm"
            onClick={onToggleMonitors}
            title="Toggle API & Event Monitors"
          >
            <Activity size={15} />
            <span>Monitors</span>
          </button>

          {activeRole && (
            <button className="btn btn-outline btn-sm" onClick={() => onSwitchRole(null)}>
              <ArrowLeftRight size={14} />
              <span>Switch Role</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
