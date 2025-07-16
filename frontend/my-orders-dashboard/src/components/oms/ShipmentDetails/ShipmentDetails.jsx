import React, { useEffect, useState } from 'react';
import { fetchInvoiceDetails, fetchShipmentDetails } from '../../../utils/oms/api';
import { getQueryParam } from '../../../utils/oms/helpers';
import "./shipmentdetails.css";

function ShipmentDetails() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const orderId = getQueryParam('orderId');

  useEffect(() => {
    if (!orderId) {
      setError("Order ID not provided.");
      return;
    }
    fetchShipmentDetails(orderId).then(setData).catch(console.error);
  }, [orderId]);

  if (!data) return <p>No shipmentdetails</p>;

  const { items } = data;

  const getDateOnly = (dateString) => dateString?.split('T')[0] || '';

const handleDownloadInvoice = async (invoiceId) => {
  try {
    const blob = await fetchInvoiceDetails(invoiceId);
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `Invoice_${invoiceId}.pdf`;
    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    alert('Download failed: ' + error.message);
  }
};


  return (
    <div className="container">
      {/* Shipment Section */}
      <div className="shipment-section">
        <div className="order-info-shipment">
          <div className="order-info-title">Shipment Details</div>
          <div className="order-details">

            <div className="order-detail">
              <span className="order-label">Status</span>
              <span className="order-value">{data.orderStatus}</span>
            </div>

            {/* <div className="order-detail">
              <span className="order-label">Tracking ID</span>
              <span className="order-value">{Shipment.shipmentTrackingId}</span>
            </div> */}

            <div className="order-detail">
              <span className="order-label">Payment Mode</span>
              <span className="order-value">{data.paymentMode}</span>
            </div>

            <div className="order-detail">
              <span className="order-label">Ordered Date</span>
              <span className="order-value">{getDateOnly(data.orderDate)}</span>
            </div>

          </div>
        </div>
      </div>

      {/* Items Section */}
      <div className="section">
        <h2 className="section-title">Ordered Items</h2>
        <table className="items-table">
          <thead>
            <tr>
              <th>      </th>
              <th>Product</th>
              <th>Tracking Id</th>
              <th>SKU</th>
              <th>Quantity</th>
              <th>Status</th>
              <th>Shipment Date</th>
              <th>Delivery Date</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, i) => (
              <tr key={i}>
                <td>
                <img
                  src={`/assets/oms/${item.sku}.jpg`}
                  alt="Product"
                  width="60"
                  height="80"
                  style={{ objectFit: 'cover', borderRadius: '6px' }}
                />
              </td>
                <td>{item.product}</td>
                <td>{item.trackingId}</td>
                <td>{item.sku}</td>
                <td>{item.quantity}</td>
                <td>{item.itemStatus}</td>
                <td>{getDateOnly(item.shipmentDate)}</td>
                <td>{getDateOnly(item.deliveredDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ✅ Download Invoice Button */}
      <div style={{ marginTop: '20px', textAlign: 'right' }}>
        <button onClick={() => handleDownloadInvoice(orderId)} >
          Download Invoice
        </button>
      </div>
    </div>
  );
}

export default ShipmentDetails;
