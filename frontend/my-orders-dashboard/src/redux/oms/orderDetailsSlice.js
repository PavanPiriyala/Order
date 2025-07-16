import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { fetchOrderDetails } from '../../utils/oms/api';

// Thunk to fetch order details
export const fetchOrderDetailsThunk = createAsyncThunk(
  'orderDetails/fetchOrderDetails',
  async (orderId, { rejectWithValue }) => {
    try {
      const data = await fetchOrderDetails(orderId);
      return data;
    } catch (err) {
      return rejectWithValue('Failed to fetch order details.');
    }
  }
);

const orderDetailsSlice = createSlice({
  name: 'orderDetails',
  initialState: {
    data: null,
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrderDetailsThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchOrderDetailsThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.data = action.payload;
      })
      .addCase(fetchOrderDetailsThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Something went wrong.';
      });
  },
});

export default orderDetailsSlice.reducer;
