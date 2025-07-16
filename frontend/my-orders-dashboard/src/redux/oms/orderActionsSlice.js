import { createSlice } from '@reduxjs/toolkit';

const orderActionsSlice = createSlice({
  name: 'orderActions',
  initialState: {
    orderData: {
      id: '',
      status: '',
      total: '',
      date: '',
    },
    modal: {
      visible: false,
      title: '',
      message: '',
    },
  },
  reducers: {
    setOrderData: (state, action) => {
      state.orderData = action.payload;
    },
    showModal: (state, action) => {
      state.modal = {
        visible: true,
        title: action.payload.title,
        message: action.payload.message,
      };
    },
    closeModal: (state) => {
      state.modal = {
        visible: false,
        title: '',
        message: '',
      };
    },
  },
});

export const { setOrderData, showModal, closeModal } = orderActionsSlice.actions;
export default orderActionsSlice.reducer;
