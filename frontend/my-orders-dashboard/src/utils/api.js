export const fetchOrders = async (months = 6) => {
  const response = await fetch(`http://localhost:8080/orders/${months}`);
  if (!response.ok) throw new Error('Failed to fetch orders');
  return await response.json();
};

export const fetchOrderDetails = async (orderId) => {
  const res = await fetch(`http://localhost:8080/orders/orderDetails/${orderId}`);
  if (!res.ok) throw new Error('Failed to fetch order details');
  return await res.json();
};

export const fetchShipmentDetails = async (orderId) => {
  const res = await fetch(`http://localhost:8080/shipment/shipmentDetails/${orderId}`);
  if (!res.ok) throw new Error('Failed to fetch shipment details');
  return await res.json();
};

export const fetchTrackingData = async (orderId) => {
  const res = await fetch(`http://localhost:8080/shipment/tracking/${orderId}`);
  if (!res.ok) throw new Error('Failed to fetch shipment details');
  return await res.json();
};

export const fetchInvoiceDetails = async (invoiceId) => {
  const res = await fetch(`http://localhost:8080/shipment/generatepdf?invoiceId=${invoiceId}`, {
    method: 'GET',
    headers: {
      Accept: 'application/pdf',
    },
  });

  if (!res.ok) throw new Error('Failed to fetch invoice PDF');
  return await res.blob();
};

