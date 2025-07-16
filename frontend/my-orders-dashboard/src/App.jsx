import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import OmsApp from './pages/oms/OmsApp';

function App() {
  return (
    <BrowserRouter>
    <Routes>
      <Route path="/oms/*" element={<OmsApp />} />    
    </Routes>
    </BrowserRouter>
  );
}
export default App;