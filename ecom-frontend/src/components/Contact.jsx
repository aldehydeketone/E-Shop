import { FaEnvelope, FaMapMarkedAlt, FaPhone } from "react-icons/fa";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import api from "../api/api";
import Spinners from "./shared/Spinners";

const Contact = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm({ mode: "onTouched" });

    const submitContactMessage = async (data) => {
        setIsSubmitting(true);
        try {
            const response = await api.post("/public/contact-messages", data);
            toast.success(response.data?.message || "Your message has been sent successfully.");
            reset();
        } catch (error) {
            const responseMessage = error?.response?.data?.message;
            const validationMessages = error?.response?.data;
            toast.error(responseMessage || Object.values(validationMessages || {})[0] || "Unable to send your message. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return(
        <div
            className="flex flex-col items-center justify-center min-h-screen py-12 bg-cover bg-center"
            style={{backgroundImage: "url('')"}}>
            
            <div className="bg-white shadow-lg rounded-lg p-8 w-full max-w-lg">
                <h1 className="text-4xl font-bold text-center mb-6">Contact us</h1>
                <p className="text-gray-600 text-center mb-4">
                    We would love to hear from you! Please fill out the form below or contact us directly
                </p>

                <form className="space-y-4" onSubmit={handleSubmit(submitContactMessage)} noValidate>
                    <div>
                        <label htmlFor="contact-name" className="block text-sm font-medium text-gray-700">
                            Name
                        </label>
                        <input 
                            id="contact-name"
                            type="text"
                            autoComplete="name"
                            aria-invalid={Boolean(errors.name)}
                            className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:outline-hidden focus:ring-2 focus:ring-blue-500"
                            {...register("name", {
                                required: "Name is required",
                                maxLength: { value: 100, message: "Name must be 100 characters or fewer" },
                            })}/>
                        {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>}
                    </div>


                    <div>
                        <label htmlFor="contact-email" className="block text-sm font-medium text-gray-700">
                            Email
                        </label>
                        <input 
                            id="contact-email"
                            type="email"
                            autoComplete="email"
                            aria-invalid={Boolean(errors.email)}
                            className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:outline-hidden focus:ring-2 focus:ring-blue-500"
                            {...register("email", {
                                required: "Email is required",
                                pattern: { value: /^\S+@\S+\.\S+$/, message: "Please enter a valid email address" },
                                maxLength: { value: 254, message: "Email must be 254 characters or fewer" },
                            })}/>
                        {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
                    </div>

                    <div>
                        <label htmlFor="contact-message" className="block text-sm font-medium text-gray-700">
                            Message
                        </label>
                        <textarea 
                            id="contact-message"
                            rows={4}
                            aria-invalid={Boolean(errors.message)}
                            className="mt-1 block w-full border border-gray-300 rounded-lg p-2 focus:outline-hidden focus:ring-2 focus:ring-blue-500"
                            {...register("message", {
                                required: "Message is required",
                                maxLength: { value: 2000, message: "Message must be 2000 characters or fewer" },
                            })}/>
                        {errors.message && <p className="mt-1 text-sm text-red-600">{errors.message.message}</p>}
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 transition duration-300 disabled:cursor-not-allowed disabled:opacity-70">
                        {isSubmitting ? <span className="flex items-center justify-center gap-2"><Spinners /> Sending...</span> : "Send Message"}
                    </button>
                </form>

                <div className="mt-8 text-center">
                    <h2 className="text-lg font-semibold">Contact Information</h2>
                    <div className="flex flex-col items-center space-y-2 mt-4">
                        <div className="flex items-center">
                            <FaPhone className="text-blue-500 mr-2"/>
                            <span className="text-gray-600">+91 98765 43210</span>
                        </div>

                        <div className="flex items-center">
                            <FaEnvelope className="text-blue-500 mr-2"/>
                            <span className="text-gray-600">mihir304singh@gmail.com</span>
                        </div>

                        <div className="flex items-center">
                            <FaMapMarkedAlt className="text-blue-500 mr-2"/>
                            <span className="text-gray-600">Mumbai, India</span>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
    );
}

export default Contact;
