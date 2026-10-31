import { orderApi } from './api';

export const orderService = {
  async createOrder(orderData) {
    const res = await orderApi.post('/api/orders', orderData);
    return res.data;
  },

  async getAllOrders() {
    const res = await orderApi.get('/api/orders');
    return res.data;
  },

  async getOrderById(id) {
    const res = await orderApi.get(`/api/orders/${id}`);
    return res.data;
  },

  async getCustomerOrders(customerName) {
    const res = await orderApi.get(`/api/orders/customer/${encodeURIComponent(customerName)}`);
    return res.data;
  },

  async cancelOrder(id) {
    const res = await orderApi.put(`/api/orders/${id}/cancel`);
    return res.data;
  },

  async getEvents() {
    const res = await orderApi.get('/api/orders/events');
    return res.data;
  },
};
