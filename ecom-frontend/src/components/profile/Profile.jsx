import { useEffect, useState } from "react";
import { FaRegUserCircle, FaShoppingBag } from "react-icons/fa";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import api from "../../api/api";

const Profile = () => {
  const { user: storedUser } = useSelector((state) => state.auth);
  const [user, setUser] = useState(storedUser);

  useEffect(() => {
    api.get("/auth/user")
      .then(({ data }) => setUser((currentUser) => ({
        ...currentUser,
        ...data,
        email: data.email || currentUser?.email,
        jwtToken: currentUser?.jwtToken,
      })))
      .catch(() => setUser(storedUser));
  }, [storedUser]);

  const roles = user?.roles?.map((role) => role.replace("ROLE_", "")).join(", ") || "Customer";

  return (
    <main className="min-h-[calc(100vh-70px)] bg-slate-50 px-4 py-10 sm:px-8">
      <section className="mx-auto max-w-2xl rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <div className="mb-8 flex items-center gap-4">
          <FaRegUserCircle className="text-5xl text-blue-600" />
          <div>
            <p className="text-sm font-medium text-slate-500">My Profile</p>
            <h1 className="text-2xl font-bold text-slate-900">{user?.username || "Account"}</h1>
          </div>
        </div>

        <dl className="divide-y divide-slate-200 rounded-xl border border-slate-200">
          <div className="grid gap-1 px-4 py-4 sm:grid-cols-3">
            <dt className="font-medium text-slate-500">Username</dt>
            <dd className="font-semibold text-slate-800 sm:col-span-2">{user?.username || "—"}</dd>
          </div>
          <div className="grid gap-1 px-4 py-4 sm:grid-cols-3">
            <dt className="font-medium text-slate-500">Email</dt>
            <dd className="break-all font-semibold text-slate-800 sm:col-span-2">{user?.email || "—"}</dd>
          </div>
          <div className="grid gap-1 px-4 py-4 sm:grid-cols-3">
            <dt className="font-medium text-slate-500">Account type</dt>
            <dd className="font-semibold text-slate-800 sm:col-span-2">{roles}</dd>
          </div>
        </dl>

        <Link to="/profile/orders" className="mt-6 inline-flex items-center gap-2 rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-700">
          <FaShoppingBag /> View my orders
        </Link>
      </section>
    </main>
  );
};

export default Profile;
