import React from 'react';
import { toast } from 'react-toastify';
import './styles.css';

const FeedbackModal = ({ isOpen, onClose, onSubmit, feedbackType, feedbackText, setFeedbackText }) => {
  if (!isOpen) return null;



    const handleSubmit = () => {
      if (feedbackText.trim() === '') return;

      onSubmit(feedbackText);

      toast.success('Thank you for your feedback! 🎉'); // <-- Toast here
      setFeedbackText('');
      onClose(); // optional: close modal after submit
    };


  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <span className="modal-close" onClick={onClose}>&times;</span>
        <h2>{feedbackType === 'seller' ? 'Seller Feedback' : 'Product Feedback'}</h2>
        <textarea
          placeholder="Type your feedback here..."
          value={feedbackText}
          onChange={(e) => setFeedbackText(e.target.value)}
        />
        <button className="submit-btn" onClick={handleSubmit}>Submit</button>
      </div>
    </div>
  );
};

export default FeedbackModal;
