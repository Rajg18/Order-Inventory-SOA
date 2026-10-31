import { inventoryApi } from './api';

export const inventoryService = {
  async getProducts() {
    const res = await inventoryApi.get('/api/products');
    return res.data;
  },

  async getProductById(id) {
    const res = await inventoryApi.get(`/api/products/${id}`);
    return res.data;
  },

  async createProduct(productData) {
    const res = await inventoryApi.post('/api/products', productData);
    return res.data;
  },

  async updateProduct(id, productData) {
    const res = await inventoryApi.put(`/api/products/${id}`, productData);
    return res.data;
  },

  async updateStock(id, action, quantity) {
    const res = await inventoryApi.put(`/api/products/${id}/stock`, {
      action: action.toUpperCase(),
      quantity: Number(quantity),
    });
    return res.data;
  },

  async checkAvailability(id) {
    const res = await inventoryApi.get(`/api/products/${id}/availability`);
    return res.data;
  },

  async deleteProduct(id) {
    const res = await inventoryApi.delete(`/api/products/${id}`);
    return res.data;
  },

  async getEvents() {
    const res = await inventoryApi.get('/api/products/events');
    return res.data;
  },
};
