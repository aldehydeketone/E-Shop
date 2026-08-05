import { useState } from "react";
import { HiOutlineTrash } from "react-icons/hi";
import SetQuantity from "./SetQuantity";
import { useDispatch } from "react-redux";
import { decreaseCartQuantity, increaseCartQuantity, removeFromCart } from "../../store/actions";
import toast from "react-hot-toast";
import { formatPrice } from "../../utils/formatPrice";
import truncateText from "../../utils/truncateText";
import getProductImageUrl from "../../utils/getProductImageUrl";

const ItemContent = ({
    productId,
    productName,
    image,
    description,
    quantity,
    price,
    discount,
    specialPrice,
    cartId,
  }) => {
    const [isUpdatingQuantity, setIsUpdatingQuantity] = useState(false);
    const dispatch = useDispatch();

    const handleQtyIncrease = async () => {
        if (isUpdatingQuantity) return;

        setIsUpdatingQuantity(true);
        await dispatch(increaseCartQuantity(productId, toast));
        setIsUpdatingQuantity(false);
    };

    const handleQtyDecrease = async () => {
        if (quantity <= 1 || isUpdatingQuantity) return;

        setIsUpdatingQuantity(true);
        await dispatch(decreaseCartQuantity(productId, toast));
        setIsUpdatingQuantity(false);
    };

    const removeItemFromCart = (cartItems) => {
        dispatch(removeFromCart(cartItems, toast));
    };
    
    return (
        <div className="grid grid-cols-2 items-center gap-4 border-b border-slate-700 p-4 text-sm last:border-b-0 md:grid-cols-5 md:px-5 md:py-4">
            <div className="col-span-2 flex min-w-0 items-center gap-4 md:col-span-2">
                <div className="h-24 w-24 shrink-0 overflow-hidden rounded-lg bg-white p-2 sm:h-28 sm:w-28">
                    <img 
                        src={getProductImageUrl(image)}
                        alt={productName}
                        className="h-full w-full object-contain"/>
                </div>
                <div className="min-w-0">
                   <h3 className="text-base font-semibold text-slate-100 sm:text-[17px]">
                    {truncateText(productName)}
                   </h3>
                <div className="mt-3">
                    <button
                        onClick={() => removeItemFromCart({
                            image,
                            productName,
                            description,
                            specialPrice,
                            price,
                            productId,
                            quantity,
                        })}
                        className="flex items-center gap-1.5 rounded-md border border-rose-500/80 px-3 py-1.5 text-xs font-semibold text-rose-300 transition-colors duration-200 hover:bg-rose-500/10">
                        <HiOutlineTrash size={16} className="text-rose-600"/>
                        Remove
                    </button>
                </div>
                </div>
            </div>

            <div className="justify-self-center flex flex-col items-center gap-1 text-base font-semibold text-slate-100">
                <span className="md:hidden text-xs font-medium text-slate-400">Price</span>
                {formatPrice(Number(specialPrice))}
            </div>

            <div className="justify-self-center flex flex-col items-center gap-1">
                <span className="md:hidden text-xs font-medium text-slate-400">Quantity</span>
                <SetQuantity 
                    quantity={quantity}
                    cardCounter={true}
                    disabled={isUpdatingQuantity}
                    handeQtyIncrease={handleQtyIncrease}
                    handleQtyDecrease={handleQtyDecrease}/>
            </div>

            <div className="col-span-2 flex items-center gap-2 justify-self-end text-base font-bold text-white md:col-span-1 md:justify-self-center">
                <span className="md:hidden text-xs font-medium text-slate-400">Total</span>
                {formatPrice(Number(quantity) * Number(specialPrice))}
            </div>
        </div>
    )
};

export default ItemContent;
