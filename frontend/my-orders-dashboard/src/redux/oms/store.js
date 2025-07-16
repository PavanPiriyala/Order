import { configureStore } from '@reduxjs/toolkit';
import orderReducer from './orderSlice';
import shipmentReducer from './shipmentDetailsSlice';

import orderDetailsReducer from './orderDetailsSlice';

import orderActionsReducer from './orderActionsSlice';

export const store = configureStore({
  reducer: {
    orders: orderReducer,
    shipment: shipmentReducer,
    orderDetails: orderDetailsReducer,
    orderActions: orderActionsReducer,
  },
});
