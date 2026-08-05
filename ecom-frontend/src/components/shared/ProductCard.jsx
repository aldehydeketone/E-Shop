import { useState } from "react";
import { FaShoppingCart } from "react-icons/fa";
import ProductViewModal from "./ProductViewModal";
import truncateText from "../../utils/truncateText";
import { useDispatch } from "react-redux";
import { addToCart } from "../../store/actions";
import toast from "react-hot-toast";

const ProductCard = ({
        productId,
        productName,
        image,
        description,
        quantity,
        price,
        discount,
        specialPrice,
        about = false,
}) => {
    const [openProductViewModal, setOpenProductViewModal] = useState(false);
    const btnLoader = false;
    const [selectedViewProduct, setSelectedViewProduct] = useState("");
    const isAvailable = quantity && Number(quantity) > 0;
    const dispatch = useDispatch();

    const handleProductView = (product) => {
        if (!about) {
            setSelectedViewProduct(product);
            setOpenProductViewModal(true);
        }
    };

    const addToCartHandler = (cartItems) => {
        dispatch(addToCart(cartItems, 1, toast));
    };

    return (
        <article className="group flex h-full flex-col overflow-hidden rounded-xl border border-slate-700 bg-slate-900 shadow-lg shadow-black/20 transition duration-300 hover:-translate-y-1 hover:border-slate-600 hover:shadow-xl">
            <div onClick={() => {
                handleProductView({
                    id: productId,
                    productName,
                    image,
                    description,
                    quantity,
                    price,
                    discount,
                    specialPrice,
                })
            }} 
                    className="relative w-full aspect-square overflow-hidden border-b border-slate-700 bg-white flex items-center justify-center p-6 sm:p-8 cursor-pointer group">
                <img 
                className="w-full h-full object-contain mix-blend-darken transition-transform duration-500 group-hover:scale-110"
                src={image}
                alt={productName}>
                </img>
            </div>
            <div className="flex flex-1 flex-col p-5">
                <h2 onClick={() => {
                handleProductView({
                    id: productId,
                    productName,
                    image,
                    description,
                    quantity,
                    price,
                    discount,
                    specialPrice,
                })
            }}
                    className="cursor-pointer text-lg font-semibold leading-snug text-slate-100">
                    {truncateText(productName, 50)}
                </h2>
                
                <div className="mt-2 min-h-12">
                    <p className="text-sm leading-6 text-slate-300">
                        {truncateText(description, 80)}
                    </p>
                </div>

            { !about && (
                <div className="mt-auto flex items-center justify-between gap-4 border-t border-slate-700 pt-4">
                {specialPrice ? (
                    <div className="flex min-w-0 flex-col">
                        <span className="text-sm text-slate-400 line-through">
                            ${Number(price).toFixed(2)}
                        </span>
                        <span className="text-xl font-bold text-white">
                            ${Number(specialPrice).toFixed(2)}
                        </span>
                    </div>
                ) : (
                    <span className="text-xl font-bold text-white">
                        {"  "}
                        ${Number(price).toFixed(2)}
                    </span>
                )}

                <button
                    disabled={!isAvailable || btnLoader}
                    onClick={() => addToCartHandler({
                        image,
                        productName,
                        description,
                        specialPrice,
                        price,
                        productId,
                        quantity,
                    })}
                    className={`${isAvailable ? "bg-blue-500 hover:bg-blue-600" : "bg-slate-700 text-slate-300"}
                        inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-lg px-4 text-sm font-semibold text-white transition-colors duration-300 disabled:cursor-not-allowed`}>
                    <FaShoppingCart className="mr-2"/>
                    {isAvailable ? "Add to Cart" : "Stock Out"}
                </button>
                </div>
            )}
                
            </div>
            <ProductViewModal 
                open={openProductViewModal}
                setOpen={setOpenProductViewModal}
                product={selectedViewProduct}
                isAvailable={isAvailable}
            />
        </article>
    )
}

export default ProductCard;
