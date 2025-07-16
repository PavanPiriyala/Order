import React from 'react';
import { Routes, Route } from "react-router-dom";
import OrderDashboard from '../../components/oms/OrderDashboard/OrderDashboard';
import OrderActions from '../../components/oms/OrderActions/OrderActions';  
import OrderDetails from '../../components/oms/OrderDetails/OrderDetails';
import ShipmentDetails from '../../components/oms/ShipmentDetails/ShipmentDetails';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function OmsApp() {
    return(
      <>
      <Routes>
        <Route path="/" element={<OrderDashboard />} />
        <Route path="/actions" element={<OrderActions />} />
        <Route path="/details" element={<OrderDetails />} />
        <Route path="/shipment" element={<ShipmentDetails />} />
      </Routes>
          <ToastContainer position="top-right" autoClose={3000} />
      </>
    )
}

export default OmsApp;