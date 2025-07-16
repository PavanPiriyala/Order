import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { fetchShipmentDetails, fetchInvoiceDetails } from '../../utils/oms/api';

export const fetchShipmentThunk = createAsyncThunk(
  'shipment/fetchShipmentDetails',
  async (orderId) => {
    return await fetchShipmentDetails(orderId);
  }
);

export const downloadInvoiceThunk = createAsyncThunk(
  'shipment/downloadInvoice',
  async (invoiceId) => {
    const blob = await fetchInvoiceDetails(invoiceId);
    return { blob, invoiceId };
  }
);

const shipmentSlice = createSlice({
  name: 'shipment',
  initialState: {
    data: null,
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchShipmentThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchShipmentThunk.fulfilled, (state, action) => {
        state.data = action.payload;
        state.loading = false;
      })
      .addCase(fetchShipmentThunk.rejected, (state) => {
        state.loading = false;
        state.error = 'Failed to fetch shipment details.';
      });
  },
});

export default shipmentSlice.reducer;
