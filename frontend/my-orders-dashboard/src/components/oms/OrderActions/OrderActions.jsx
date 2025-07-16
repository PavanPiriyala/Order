import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import FeedbackModal from '../FeedbackModal/FeedbackModal';// Ensure this exists
import './styles.css';
import { toast } from 'react-toastify';
import { setOrderData } from '../../../redux/oms/orderActionsSlice'; // Adjust the import path as necessary

function formatDate(dateString) {
  const date = new Date(dateString);
  if (dateString === 'N/A' || isNaN(date.getTime())) return 'N/A';
  return date.toISOString().split('T')[0];
}

function OrderActions() {
  const [searchParams] = useSearchParams();
  const dispatch = useDispatch();
  const orderActions = useSelector((state) => state.orderActions || {});
  const { orderData } = orderActions;

  const [feedbackType, setFeedbackType] = useState(null); // 'seller' or 'product'

  useEffect(() => {
    dispatch(
      setOrderData({
        id: searchParams.get('orderId') || 'N/A',
        status: searchParams.get('status') || 'N/A',
        total: searchParams.get('total') || 'N/A',
        date: formatDate(searchParams.get('date') || 'N/A'),
      })
    );
  }, [searchParams, dispatch]);

  const [feedbackText, setFeedbackText] = useState('');

 const handleSubmitFeedback = (feedbackText) => {
  console.log(`[${feedbackType}] Feedback submitted:`, feedbackText);
  toast.success(`${feedbackType === 'seller' ? 'Seller' : 'Product'} feedback submitted successfully!`);
  setFeedbackType(null);
};


  return (
    <div className="container">
      <div className="header">
        <h1>Feedback</h1>
        <p>Submit your feedback for the seller or product</p>
      </div>

      <div className="section">
        <div className="order-info">
          <div className="order-info-title">Order Information</div>
          <div className="order-details">
            <div className="order-detail"><span className="order-label">Order ID</span><span className="order-value">{orderData.id}</span></div>
            <div className="order-detail"><span className="order-label">Status</span><span className="order-value">{orderData.status}</span></div>
            <div className="order-detail"><span className="order-label">Total</span><span className="order-value">{orderData.total}</span></div>
            <div className="order-detail"><span className="order-label">Date</span><span className="order-value">{orderData.date}</span></div>
          </div>
        </div>

        <h2 className="section-title">Leave Feedback</h2>
        <div className="actions-grid">
          <div className="action-card" onClick={() => setFeedbackType('seller')}>
            <div className="action-icon">⭐</div>
            <div className="action-title">Seller Feedback</div>
            <button className="action-btn">Submit</button>
          </div>
          <div className="action-card" onClick={() => setFeedbackType('product')}>
            <div className="action-icon">📝</div>
            <div className="action-title">Product Feedback</div>
            <button className="action-btn">Review</button>
          </div>
        </div>
      </div>

      <FeedbackModal
    isOpen={feedbackType !== null}
    onClose={() => {
      setFeedbackType(null);
      setFeedbackText('');
    }}
    onSubmit={(text) => {
      handleSubmitFeedback(text);
      setFeedbackText('');
    }}
    feedbackType={feedbackType}
    feedbackText={feedbackText}
    setFeedbackText={setFeedbackText}
  />

    </div>
  );
}

export default OrderActions;
