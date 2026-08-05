
const btnStyles = "flex h-10 w-10 items-center justify-center rounded-md border border-gray-300 text-lg font-medium text-gray-700 transition-colors hover:border-gray-400 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40";
const SetQuantity = ({
    quantity,
    cardCounter,
    handeQtyIncrease,
    handleQtyDecrease,
    disabled = false,
}) => {
   return (
   <div className="flex items-center">
        {cardCounter ? null : <div className="font-semibold">QUANTITY</div>}
        <div className="flex flex-row items-center gap-3 text-base">
            <button
                disabled={disabled || quantity<=1}
                className={btnStyles}
                onClick={handleQtyDecrease}>
                -
            </button>
                <div className="min-w-5 text-center font-semibold text-gray-900">{quantity}</div>
            <button
                disabled={disabled}
                className={btnStyles}
                onClick={handeQtyIncrease}>
                +
            </button>
        </div>
    </div>
   );
};

export default SetQuantity;
