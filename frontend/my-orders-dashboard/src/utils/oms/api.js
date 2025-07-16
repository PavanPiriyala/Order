import axios from 'axios';

const BASE_URL = "http://localhost:8006";
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwcGlyeWFsYUBuaXN1bS5jb20iLCJ1c2VySWQiOjEwMSwicm9sZXMiOlsiY3VzdG9tZXIiLCJvbXNfYWRtaW4iXSwiaWF0IjoxNzUyMTQ2Mzk3LCJleHAiOjE4NTIyMzI3OTd9.8i3FewE-ajqyBh6jp7kNGkfZbNvXvhzrV2u_XIOZOas';
// const token = getTokenFromCookie(); // Or via secure library


// 1. Fetch Orders List
export const fetchOrders = async (months = 6) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/orders/list/${months}`,{
      headers :{
        Authorization: `Bearer ${token}`,
      },
  });
    return data;
  } catch (err) {
    throw new Error('Failed to fetch orders');
  }
};

// 2. Fetch Order Details
export const fetchOrderDetails = async (orderId) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/orders/${orderId}`, {
      headers :{
        Authorization: `Bearer ${token}`,
      },});
    return data;
  } catch (err) {
    throw new Error('Failed to fetch order details');
  }
};

// 3. Fetch Shipment Details
export const fetchShipmentDetails = async (orderId) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/shipment/shipment-times/order/${orderId}`, {headers :{
        Authorization: `Bearer ${token}`,
      },});
    return data;
  } catch (err) {
    throw new Error(err?.response?.data?.message || 'Failed to fetch shipment details');
  }
};

// 5. Fetch Invoice as PDF
export const fetchInvoiceDetails = async (invoiceId) => {
  try {
    const response = await axios.get(`${BASE_URL}/shipment/generatepdf`,{
      params: { orderId: invoiceId },
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/pdf'
      },
      responseType: 'blob', // to handle PDF file
    });
    return response.data;
  } catch (err) {
    throw new Error('Failed to fetch invoice PDF');
  }
};
