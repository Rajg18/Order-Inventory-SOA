import React, { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';
import { inventoryService } from '../services/inventoryService';
import { Layers, RefreshCw, Radio, ArrowUpRight, ArrowDownLeft, Filter } from 'lucide-react';
import { formatDateTime } from '../utils/formatters';

export default function ServiceEventMonitor() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState('ALL');

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const [orderEvents, inventoryEvents] = await Promise.allSettled([
        orderService.getEvents(),
        inventoryService.getEvents(),
      ]);

      const oEvents = orderEvents.status === 'fulfilled' ? orderEvents.value : [];
      const iEvents = inventoryEvents.status === 'fulfilled' ? inventoryEvents.value : [];

      const combined = [...oEvents, ...iEvents].sort((a, b) => {
        return new Date(b.timestamp) - new Date(a.timestamp);
      });

      setEvents(combined);
    } catch (err) {
      console.error('Failed to fetch service events:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
    const interval = setInterval(fetchEvents, 3000);
    return () => clearInterval(interval);
  }, []);

  const getEventBadge = (type) => {
    switch (type) {
      case 'OrderCreated':
        return 'badge-info';
      case 'InventoryReserved':
      case 'StockReserved':
        return 'badge-success';
      case 'InventoryRejected':
      case 'ReservationRejected':
        return 'badge-danger';
      case 'OrderCancelled':
      case 'StockReleased':
      case 'StockRestored':
        return 'badge-warning';
      default:
        return 'badge-neutral';
    }
  };

  const filteredEvents = events.filter((evt) => {
    if (typeFilter === 'ALL') return true;
    return evt.eventType === typeFilter;
  });

  return (
    <div className="monitor-container">
      <div className="monitor-toolbar">
        <div className="flex-row items-center gap-2">
          <span className="font-semibold font-sm text-secondary">SERVICE EVENT MONITOR (RabbitMQ)</span>
          <span className="badge badge-neutral font-xs">{filteredEvents.length} / {events.length} captured</span>
        </div>

        <div className="flex-row items-center gap-2">
          <div className="filter-pill-group">
            {['ALL', 'OrderCreated', 'InventoryReserved', 'InventoryRejected', 'OrderCancelled', 'StockReleased'].map((t) => (
              <button
                key={t}
                className={`filter-pill-btn ${typeFilter === t ? 'active' : ''}`}
                onClick={() => setTypeFilter(t)}
              >
                {t === 'ALL' ? 'All Events' : t}
              </button>
            ))}
          </div>

          <button className="btn btn-outline btn-xs" onClick={fetchEvents} disabled={loading}>
            <RefreshCw size={13} className={loading ? 'spinner' : ''} /> Refresh
          </button>
        </div>
      </div>

      {filteredEvents.length === 0 ? (
        <div className="monitor-empty">
          <Radio size={28} className="empty-icon text-muted" />
          <p>No backend service events matching filter. Place or cancel an order in the UI to trigger actual RabbitMQ topic exchange events.</p>
        </div>
      ) : (
        <div className="monitor-event-list">
          {filteredEvents.map((evt, idx) => (
            <div key={evt.id || idx} className="event-item">
              <div className="event-item-header">
                <div className="flex-row items-center gap-2">
                  {evt.direction === 'PUBLISHED' ? (
                    <span className="event-dir-tag dir-pub" title="Published to RabbitMQ Topic Exchange">
                      <ArrowUpRight size={12} /> PUB
                    </span>
                  ) : evt.direction === 'RECEIVED' ? (
                    <span className="event-dir-tag dir-sub" title="Consumed from RabbitMQ Queue">
                      <ArrowDownLeft size={12} /> REC
                    </span>
                  ) : (
                    <span className="event-dir-tag dir-internal" title="Internal Atomic Operation">
                      INT
                    </span>
                  )}
                  <span className={`badge ${getEventBadge(evt.eventType)}`}>
                    {evt.eventType}
                  </span>
                  {evt.orderId && (
                    <span className="font-mono font-xs text-muted">Order #{evt.orderId}</span>
                  )}
                  {evt.productId && (
                    <span className="font-mono font-xs text-muted">Product #{evt.productId}</span>
                  )}
                </div>
                <span className="event-timestamp">{formatDateTime(evt.timestamp)}</span>
              </div>

              <p className="event-details">{evt.details}</p>

              {evt.eventId && (
                <div className="event-id-tag">
                  Event UUID: <code>{evt.eventId}</code>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
