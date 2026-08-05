import { MdArrowBack, MdShoppingCart } from "react-icons/md";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import ItemContent from "./ItemContent";
import CartEmpty from "./CartEmpty";
import { formatPrice } from "../../utils/formatPrice";

const Cart = () => {
    const dispatch = useDispatch();
    const { cart } = useSelector((state) => state.carts);
    const newCart = { ...cart };

    newCart.totalPrice = cart?.reduce(
        (acc, cur) => acc + Number(cur?.specialPrice) * Number(cur?.quantity), 0
    );

    if (!cart || cart.length === 0) return <CartEmpty />;

    return (
        <div className="lg:px-14 sm:px-8 px-4 py-12">
            <div className="mb-10 flex flex-col items-center pt-2">
                <h1 className="text-4xl font-bold text-gray-900 flex items-center gap-3">
                  <MdShoppingCart size={36} className="text-gray-700" />
                    Your Cart
                </h1>
                <p className="text-lg text-gray-600 mt-2">All your selected items</p>
            </div>

            <div className="overflow-hidden rounded-xl border border-slate-700 bg-slate-900/70">
            <div className="hidden items-center gap-4 border-b border-slate-700 bg-slate-800/70 px-5 py-4 font-semibold md:grid md:grid-cols-5">
                <div className="md:col-span-2 justify-self-start text-lg text-slate-800 lg:ps-4">
                    Product
                </div>

                <div className="justify-self-center text-lg text-slate-800">
                    Price
                </div>

                <div className="justify-self-center text-lg text-slate-800">
                    Quantity
                </div>

                <div className="justify-self-center text-lg text-slate-800">
                    Total
                </div>
            </div>

            <div>
                {cart && cart.length > 0 &&
                    cart.map((item, i) => <ItemContent key={i} {...item}/>)}
            </div>
            </div>

            <div className="mt-6 flex justify-end">
                <div className="flex w-full max-w-md flex-col gap-1 rounded-xl border border-slate-700 bg-slate-900/70 p-5 text-sm">
                    <div className="flex justify-between w-full md:text-lg text-sm font-semibold">
                        <span>Subtotal</span>
                        <span>{formatPrice(newCart?.totalPrice)}</span>
                    </div>

                    <p className="text-slate-500">
                        Taxes and shipping calculated at checkout
                    </p>

                    <Link className="mt-2 flex w-full" to="/checkout">
                    <button
                        onClick={() => {}}
                        className="font-semibold w-full sm:w-[300px] py-2 px-4 rounded-xs bg-custom-blue text-white flex items-center justify-center gap-2 hover:text-gray-300 transition duration-500">
                        <MdShoppingCart size={20} />
                        Checkout
                    </button>
                    </Link>

                    <Link className="mt-3 flex items-center justify-center gap-2 text-slate-400 transition-colors hover:text-white" to="/products">
                        <MdArrowBack />
                        <span>Continue Shopping</span>
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Cart;
