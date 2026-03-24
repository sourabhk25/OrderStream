import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8081/api';

function App() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    customerId: '',
    productId: '',
    quantity: 1,
    totalAmount: 0
  });

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_BASE_URL}/orders`);
      setOrders(response.data);
    } catch (error) {
      console.error('Error fetching orders:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API_BASE_URL}/orders`, formData);
      setFormData({
        customerId: '',
        productId: '',
        quantity: 1,
        totalAmount: 0
      });
      fetchOrders();
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Failed to create order');
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: '#FFA500',
      PAYMENT_PROCESSING: '#4169E1',
      PAYMENT_COMPLETED: '#32CD32',
      PAYMENT_FAILED: '#DC143C',
      INVENTORY_RESERVED: '#00CED1',
      COMPLETED: '#228B22',
      CANCELLED: '#8B0000'
    };
    return colors[status] || '#808080';
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>OrderStream Dashboard</h1>
        <p>Real-time Order Tracking System</p>
      </header>

      <div className="container">
        <div className="form-section">
          <h2>Create New Order</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Customer ID:</label>
              <input
                type="text"
                name="customerId"
                value={formData.customerId}
                onChange={handleChange}
                placeholder="e.g., CUST-001"
                required
              />
            </div>
            <div className="form-group">
              <label>Product ID:</label>
              <input
                type="text"
                name="productId"
                value={formData.productId}
                onChange={handleChange}
                placeholder="e.g., PROD-001"
                required
              />
            </div>
            <div className="form-group">
              <label>Quantity:</label>
              <input
                type="number"
                name="quantity"
                value={formData.quantity}
                onChange={handleChange}
                min="1"
                required
              />
            </div>
            <div className="form-group">
              <label>Total Amount ($):</label>
              <input
                type="number"
                name="totalAmount"
                value={formData.totalAmount}
                onChange={handleChange}
                step="0.01"
                min="0.01"
                required
              />
            </div>
            <button type="submit" className="btn-submit">Create Order</button>
          </form>
        </div>

        <div className="orders-section">
          <div className="section-header">
            <h2>Live Orders ({orders.length})</h2>
            <button onClick={fetchOrders} className="btn-refresh" disabled={loading}>
              {loading ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>

          {orders.length === 0 ? (
            <div className="no-orders">
              <p>No orders yet. Create your first order!</p>
            </div>
          ) : (
            <div className="orders-grid">
              {orders.map((order) => (
                <div key={order.id} className="order-card">
                  <div className="order-header">
                    <span className="order-number">{order.orderNumber}</span>
                    <span
                      className="order-status"
                      style={{ backgroundColor: getStatusColor(order.status) }}
                    >
                      {order.status.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <div className="order-details">
                    <div className="detail-row">
                      <span className="label">Customer:</span>
                      <span className="value">{order.customerId}</span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Product:</span>
                      <span className="value">{order.productId}</span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Quantity:</span>
                      <span className="value">{order.quantity}</span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Amount:</span>
                      <span className="value">${order.totalAmount}</span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Created:</span>
                      <span className="value">
                        {new Date(order.createdAt).toLocaleString()}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
