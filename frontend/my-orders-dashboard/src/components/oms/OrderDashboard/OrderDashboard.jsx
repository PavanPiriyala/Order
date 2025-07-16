import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { fetchOrdersThunk, setMonths } from "../../../redux/oms/orderSlice";
import { formatDate, setQueryParam } from "../../../utils/oms/helpers";
import { getUserFromToken } from "../../../utils/oms/auth";
import { useNavigate } from "react-router-dom";
import "./orderdashboard.css";


function OrderDashboard() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const user = getUserFromToken();
  const username = user?.name || user?.sub || "You";


  const { months, orders } = useSelector((state) => state.orders);
  const [activeTab, setActiveTab] = useState(""); // No default active tab on load

  const validMonths = ["1", "2", "3", "4", "5", "6", "12"];

  // Handle dropdown changes for months (only when orders tab is active)
  const handleChange = (e) => dispatch(setMonths(e.target.value));

  // Effect to trigger API when tab changes or months change
  useEffect(() => {
    if (activeTab === "orders") {
      setQueryParam("months", months);
      setLoading(true);
      dispatch(fetchOrdersThunk(months)).finally(() => setLoading(false));
    }

    if (activeTab === "cancelled") {
      // TODO: dispatch(fetchCancelledOrdersThunk());
    }
  }, [months, dispatch, activeTab]);

  // Click handlers for tabs
  const handleOrdersClick = () => {
    setActiveTab("orders");
    dispatch(setMonths("6")); // Default to 6 months
  };

  const handleCancelledClick = () => {
    setActiveTab("cancelled");
  };

  // Navigation actions
  const viewDetails = (orderId) => navigate(`details?orderId=${orderId}`);
  const viewShipment = (orderId) => navigate(`shipment?orderId=${orderId}`);
  const viewActions = (order) => {
    const { orderId, orderStatus, orderTotal, orderDate } = order;
    navigate(
      `actions?orderId=${orderId}&status=${orderStatus}&total=${orderTotal}&date=${orderDate}`
    );
  };

  return (
    <div className="container">
      <div className="header">
        <h1>Orders Management Dashboard</h1>
      </div>

      <div className="tabs">
        <button
          className={`tab-btn ${activeTab === "orders" ? "active" : ""}`}
          onClick={handleOrdersClick}
        >
          Orders
        </button>
        <button
          className={`tab-btn ${activeTab === "cancelled" ? "active" : ""}`}
          onClick={handleCancelledClick}
          disabled
        >
          Cancelled Orders
        </button>
      </div>

      {activeTab === "orders" && (
        <div className="filter-section">
          <div className="filter-container">
            <label className="filter-label">Filter by months:</label>
            <div className="dropdown">
              <select value={months} onChange={handleChange}>
                {validMonths.map((m) => (
                  <option key={m} value={m}>
                    Last {m} month(s)
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      )}

      <div className="orders-section">
        <div className="orders-grid">
          {loading ? (
              <div className="loading-indicator">
                <p>Loading orders...</p>
              </div>
            ) : orders.length === 0 ? (
              <div className="no-orders">
                <h3>No Orders Found</h3>
              </div>
            ) : (
            orders.map((order) => (
              <div key={order.orderId} className="order-card">
                <div className="order-header">
                  <div className="info-label">Order ID: {order.orderId}</div>
                  {/* <span className="info-value"></span> */}
                  <div className="order-date">
                    <i className="fi fi-sr-calendar"></i>{" "}
                    {formatDate(order.orderDate)}
                  </div>
                </div>
                <div className="order-body">
                  <div className="order-info">
                    <div className="info-item">
                      <span className="info-label">
                        <i className="fi fi-sr-user"></i>
                      </span>
                      <span className="info-value">{username}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Amount</span>
                      <span className="info-value">₹{order.orderTotal}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Items</span>
                      <span className="info-value">{order.items}</span>
                    </div>
                  </div>

                  <div className={`status ${order.orderStatus.toLowerCase()}`}>
                    {order.orderStatus}
                  </div>

                  <div className="order-actions">
                    <button
                      className="action-btn btn-details"
                      onClick={() => viewDetails(order.orderId)}
                    >
                      Details
                    </button>
                    <button
                      className="action-btn btn-shipment"
                      onClick={() => viewShipment(order.orderId)}
                    >
                      Shipment
                    </button>
                    <button
                      className="action-btn btn-more"
                      onClick={() => viewActions(order)}
                    >
                      Feedback
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default OrderDashboard;
