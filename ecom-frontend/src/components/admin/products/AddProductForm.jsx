import React, { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import InputField from '../../shared/InputField';
import { Button } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { dashboardProductsAction, fetchCategories, updateProductFromDashboard } from '../../../store/actions';
import toast from 'react-hot-toast';
import Spinners from '../../shared/Spinners';
import SelectTextField from '../../shared/SelectTextField';
import Skeleton from '../../shared/Skeleton';
import ErrorPage from '../../shared/ErrorPage';
import { FaCloudUploadAlt } from 'react-icons/fa';
import api from '../../../api/api';

const AddProductForm = ({ setOpen, product, update=false}) => {
const [loader, setLoader] = useState(false);
const [selectedCategory, setSelectedCategory] = useState();
const { categories } = useSelector((state) => state.products);
const { categoryLoader, errorMessage } = useSelector((state) => state.errors);
const { user } = useSelector((state) => state.auth);
const isAdmin = user && user?.roles?.includes("ROLE_ADMIN");

// Image state (only for new product creation)
const [selectedFile, setSelectedFile] = useState(null);
const [previewImage, setPreviewImage] = useState(null);
const fileInputRef = useRef();

const dispatch = useDispatch();
    const {
        register,
        handleSubmit,
        reset,
        setValue,
        formState: { errors }
    } = useForm({
        mode: "onTouched"
    });

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file && ["image/jpeg", "image/jpg", "image/png"].includes(file.type)) {
            const reader = new FileReader();
            reader.onloadend = () => setPreviewImage(reader.result);
            reader.readAsDataURL(file);
            setSelectedFile(file);
        } else if (file) {
            toast.error("Please select a valid image file (.jpeg, .jpg, .png)");
            setSelectedFile(null);
            setPreviewImage(null);
        }
    };

    const saveProductHandler = async (data) => {
        if (!update) {
            if (!selectedCategory || !selectedCategory.categoryId) {
                toast.error("Please select a category");
                return;
            }

            // ── CREATE ──────────────────────────────────────────────────────────
            const sendData = {
                ...data,
                price: parseFloat(data.price) || 0,
                quantity: parseInt(data.quantity, 10) || 0,
                discount: parseFloat(data.discount) || 0,
                specialPrice: parseFloat(data.specialPrice) || 0,
                categoryId: selectedCategory?.categoryId,
            };

            try {
                setLoader(true);
                const endpoint = isAdmin ? "/admin/categories/" : "/seller/categories/";
                const { data: created } = await api.post(
                    `${endpoint}${sendData.categoryId}/product`,
                    sendData
                );

                // Upload image immediately after product is created
                if (selectedFile && created?.productId) {
                    const formData = new FormData();
                    formData.append("image", selectedFile);
                    const imgEndpoint = isAdmin ? "/admin/products/" : "/seller/products/";
                    try {
                        await api.put(`${imgEndpoint}${created.productId}/image`, formData);
                    } catch (imgErr) {
                        // Non-fatal: product exists, image just didn't upload
                        toast.error("Product created but image upload failed. You can upload it from the product list.");
                    }
                }

                toast.success("Product created successfully");
                reset();
                setOpen(false);
                await dispatch(dashboardProductsAction());
            } catch (error) {
                console.error(error);
                toast.error(error?.response?.data?.message || "Product creation failed");
            } finally {
                setLoader(false);
            }
        } else {
            // ── UPDATE ──────────────────────────────────────────────────────────
            const sendData = {
                ...data,
                price: parseFloat(data.price) || 0,
                quantity: parseInt(data.quantity, 10) || 0,
                discount: parseFloat(data.discount) || 0,
                specialPrice: parseFloat(data.specialPrice) || 0,
                id: product.id,
            };
            dispatch(updateProductFromDashboard(sendData, toast, reset, setLoader, setOpen, isAdmin));
        }
    };


    useEffect(() => {
        if (update && product) {
            setValue("productName", product?.productName);
            setValue("price", product?.price);
            setValue("quantity", product?.quantity);
            setValue("discount", product?.discount);
            setValue("specialPrice", product?.specialPrice);
            setValue("description", product?.description);
        }
    }, [update, product]);


    useEffect(() => {
        if (!update) {
            dispatch(fetchCategories());
        }
    }, [dispatch, update]);

    useEffect(() => {
        if (!categoryLoader && categories && !selectedCategory) {
            setSelectedCategory({ categoryId: "", categoryName: "Select Category" });
        }
    }, [categories, categoryLoader, selectedCategory]);

    if (categoryLoader) return <Skeleton />
    if (errorMessage) return <ErrorPage message={errorMessage} />

  return (
    <div className='py-5'>
        <form className='space-y-4'
            onSubmit={handleSubmit(saveProductHandler)}>
            <div className='flex md:flex-row flex-col gap-4 w-full'>
                <InputField 
                    label="Product Name"
                    required
                    id="productName"
                    type="text"
                    message="This field is required*"
                    register={register}
                    placeholder="Product Name"
                    errors={errors}
                    />

                {!update && (
                    <SelectTextField
                        label="Select Categories"
                        select={selectedCategory}
                        setSelect={setSelectedCategory}
                        lists={categories}
                    />
                )}
            </div>

            <div className='flex md:flex-row flex-col gap-4 w-full'>
                <InputField 
                    label="Price"
                    required
                    id="price"
                    type="number"
                    message="This field is required*"
                    placeholder="Product Price"
                    register={register}
                    errors={errors}
                    />

                    <InputField 
                    label="Quantity"
                    required
                    id="quantity"
                    type="number"
                    message="This field is required*"
                    register={register}
                    placeholder="Product Quantity"
                    errors={errors}
                    />
            </div>
        <div className="flex md:flex-row flex-col gap-4 w-full">
          <InputField
            label="Discount"
            id="discount"
            type="number"
            message="This field is required*"
            placeholder="Product Discount"
            register={register}
            errors={errors}
          />
          <InputField
            label="Special Price"
            id="specialPrice"
            type="number"
            message="This field is required*"
            placeholder="Product Discount"
            register={register}
            errors={errors}
          />
        </div>

        <div className="flex flex-col gap-2 w-full">
            <label htmlFor='desc'
              className='font-semibold text-sm text-slate-800'>
                Description
            </label>

            <textarea
                rows={5}
                placeholder="Add product description...."
                className={`px-4 py-2 w-full border outline-hidden bg-transparent text-slate-800 rounded-md ${
                    errors["description"]?.message ? "border-red-500" : "border-slate-700" 
                }`}
                maxLength={255}
                {...register("description", {
                    required: {value: true, message:"Description is required"},
                })}
                />

                {errors["description"]?.message && (
                    <p className="text-sm font-semibold text-red-600 mt-0">
                        {errors["description"]?.message}
                    </p>
                )}
        </div>

        {/* ── Image picker (only when creating a new product) ── */}
        {!update && (
            <div className="flex flex-col gap-2 w-full">
                <label className="font-semibold text-sm text-slate-800">
                    Product Image <span className="text-slate-500 font-normal">(optional)</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer text-custom-blue border border-dashed border-custom-blue rounded-md p-3 w-full justify-center">
                    <FaCloudUploadAlt size={20} />
                    <span>{selectedFile ? selectedFile.name : "Upload Product Image"}</span>
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleImageChange}
                        className="hidden"
                        accept=".jpeg,.jpg,.png"
                    />
                </label>
                {previewImage && (
                    <div>
                        <img
                            src={previewImage}
                            alt="Image Preview"
                            className="h-32 rounded-md mb-1 object-cover"
                        />
                        <button
                            type="button"
                            onClick={() => {
                                setPreviewImage(null);
                                setSelectedFile(null);
                                if (fileInputRef.current) fileInputRef.current.value = null;
                            }}
                            className="bg-rose-600 text-white text-xs px-2 py-1 rounded-md"
                        >
                            Clear Image
                        </button>
                    </div>
                )}
            </div>
        )}

        <div className='sticky bottom-0 z-10 mt-8 flex w-full items-center justify-between border-t bg-white py-4'>
            <Button disabled={loader}
                    onClick={() => setOpen(false)}
                    variant='outlined'
                    className='text-white py-[10px] px-4 text-sm font-medium'>
                Cancel
            </Button>

            <Button
                disabled={loader}
                type='submit'
                variant='contained'
                color='primary'
                className='bg-custom-blue text-white  py-[10px] px-4 text-sm font-medium'>
                {loader ? (
                    <div className='flex gap-2 items-center'>
                        <Spinners /> Loading...
                    </div>
                ) : (
                    "Save"
                )}
            </Button>
        </div>
        </form>
    </div>
  )
}

export default AddProductForm
