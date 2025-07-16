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

  const { orderStatus, total, products = [] } = data.orderDetails;

  return (
    <div className="container">
      <h1>Order Details</h1>
      <p><strong>Order ID:</strong> {orderId}</p>
      <p><strong>Status:</strong> {orderStatus}</p>
      <p><strong>Total:</strong> ₹{Number(total).toFixed(2)}</p>
      <table>
        <thead>
          <tr><th>Product</th><th>SKU</th><th>Qty</th><th>Price</th><th>Status</th></tr>
        </thead>
        <tbody>
          {products.map((item, i) => (
            <tr key={i}>
              <td>{item.productID}</td>
              <td>{item.sku}</td>
              <td>{item.quantity}</td>
              <td>₹{item.price}</td>
              <td>{item.status}</td>
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
