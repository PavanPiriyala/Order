import React from 'react';
import { formatDate } from '../../utils/oms/helpers';

function OrderCard({ order, onDetails, onShipment, onActions }) {
  return (
    <div className="order-card">
      <div><strong>{order.orderId}</strong> — {formatDate(order.orderDate)}</div>
      <div>{order.userName} | ₹{order.finalAmount} | {order.items} items</div>
      <div>{order.orderStatus}</div>
      <button onClick={() => onDetails(order.orderId)}>Details</button>
      <button onClick={() => onShipment(order.orderId)}>Shipment</button>
      <button onClick={() => onActions(order)}>More</button>
    </div>
  );
}

export default OrderCard;