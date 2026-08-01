import { useEffect, useState } from "react";
import { FaBoxOpen, FaShoppingBag } from "react-icons/fa";
import { Link } from "react-router-dom";
import api from "../../api/api";
import getProductImageUrl from "../../utils/getProductImageUrl";
import { formatPrice } from "../../utils/formatPrice";

const UserOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/orders/users?pageNumber=0&pageSize=50&sortBy=orderId&sortOrder=desc")
      .then(({ data }) => setOrders(data.content || []))
      .catch((requestError) => setError(requestError?.response?.data?.message || "Could not load your orders."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <main className="min-h-[calc(100vh-70px)] bg-slate-50 px-4 py-10 text-center text-slate-600">Loading your orders…</main>;
  }

  if (error) {
    return <main className="min-h-[calc(100vh-70px)] bg-slate-50 px-4 py-10 text-center text-rose-600">{error}</main>;
  }

  return (
    <main className="min-h-[calc(100vh-70px)] bg-slate-50 px-4 py-10 sm:px-8">
      <section className="mx-auto max-w-4xl">
        <div className="mb-6 flex items-center gap-3">
          <FaShoppingBag className="text-3xl text-blue-600" />
          <div>
            <p className="text-sm font-medium text-slate-500">Purchase history</p>
            <h1 className="text-2xl font-bold text-slate-900">My Orders</h1>
          </div>
        </div>

        {orders.length === 0 ? (
          <div className="rounded-2xl border border-slate-200 bg-white px-6 py-14 text-center shadow-sm">
            <FaBoxOpen className="mx-auto mb-4 text-5xl text-slate-400" />
            <h2 className="text-xl font-bold text-slate-800">No orders placed yet</h2>
            <p className="mt-2 text-slate-500">Your completed purchases will appear here.</p>
            <Link to="/products" className="mt-6 inline-block rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700">Browse products</Link>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <article key={order.orderId} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <div className="mb-4 flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 pb-4">
                  <div>
                    <p className="font-bold text-slate-900">Order #{order.orderId}</p>
                    <p className="text-sm text-slate-500">{order.orderDate || "Order date unavailable"}</p>
                  </div>
                  <span className="rounded-full bg-blue-50 px-3 py-1 text-sm font-semibold text-blue-700">{order.orderStatus || "Processing"}</span>
                </div>
                <div className="space-y-3">
                  {(order.orderItems || []).map((item) => (
                    <div key={item.orderItemId} className="flex items-center gap-4">
                      <img src={getProductImageUrl(item.product?.image)} alt={item.product?.productName || "Product"} className="h-14 w-14 rounded-lg object-cover" />
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-semibold text-slate-800">{item.product?.productName}</p>
                        <p className="text-sm text-slate-500">Qty: {item.quantity}</p>
                      </div>
                      <p className="font-semibold text-slate-800">{formatPrice(Number(item.orderedProductPrice) * Number(item.quantity))}</p>
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex justify-end border-t border-slate-200 pt-4 text-lg font-bold text-slate-900">Total: {formatPrice(Number(order.totalAmount))}</div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
};

export default UserOrders;
