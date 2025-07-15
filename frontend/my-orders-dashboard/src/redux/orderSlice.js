import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { fetchOrders } from '../utils/api'; // make sure this path is correct

// ✅ 1. Define async thunk
export const fetchOrdersThunk = createAsyncThunk(
  'orders/fetchOrders',
  async (months) => {
    const response = await fetchOrders(months); // your API call
    return response || [];
  }
);

// ✅ 2. Initial state
const initialState = {
  months: '6',
  orders: [],
  loading: false,
  error: null,
};

// ✅ 3. Create slice
const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    setMonths: (state, action) => {
      state.months = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrdersThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchOrdersThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.orders = action.payload;
      })
      .addCase(fetchOrdersThunk.rejected, (state) => {
        state.loading = false;
        state.error = 'Failed to fetch orders';
      });
  },
});

// ✅ 4. Export
export const { setMonths } = orderSlice.actions;
export default orderSlice.reducer;
