import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchOrderDetailsThunk } from '../../redux/orderDetailsSlice';
import { getQueryParam } from '../../utils/helpers';
import './orderdetails.css';

function OrderDetails() {
  const dispatch = useDispatch();
  const orderId = getQueryParam('orderId');

  const { data, loading, error } = useSelector((state) => state.orderDetails);

  useEffect(() => {
    if (orderId) {
      dispatch(fetchOrderDetailsThunk(orderId));
    }
  }, [dispatch, orderId]);

  if (error) return <p style={{ color: 'red' }}>{error}</p>;
  if (loading || !data) return <p>Loading...</p>;

  const { orderStatus, orderTotal, orderItems = [] } = data;

  return (
    <div className="container">
      <h1>Order Details</h1>
      <p><strong>Order ID:</strong> {orderId}</p>
      <p><strong>Status:</strong> {orderStatus}</p>
      <p><strong>Total:</strong> ₹{Number(orderTotal).toFixed(2)}</p>
      <table>
        <thead>
          <tr><th>OrderItemId</th><th>Product</th><th>SKU</th><th>Qty</th><th>UnitPrice</th><th>Discount</th><th>FinalPrice</th><th>Size</th><th>Status</th><th>SellerId</th></tr>
        </thead>
        <tbody>
          {orderItems.map((item, i) => (
            <tr key={i}>
              <td>{item.orderItemId}</td>
              <td>{item.productId}</td>
              <td>{item.sku}</td>
              <td>{item.quantity}</td>
              <td>₹{item.unitPrice}</td>
              <td>₹{item.discount}</td>
              <td>₹{item.finalPrice}</td>
              <td>{item.size}</td>  
              <td>{item.status}</td>
              <td>{item.sellerId}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="printbtn">
        <button onClick={() => window.print()}>Print Order</button>
      </div>
    </div>
  );
}

export default OrderDetails;
