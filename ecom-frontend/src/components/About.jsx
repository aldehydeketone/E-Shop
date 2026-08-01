import ProductCard from "./shared/ProductCard";

const products = [
    {
        image: "https://images.unsplash.com/photo-1605236453806-6ff36851218e?w=800&q=80",
        productName: "Apple iPhone 16 Pro",
        description:
          "The latest Apple iPhone 16 Pro with A18 Pro chip, Titanium design, and a stunning 48MP camera system for breathtaking professional photos.",
        specialPrice: 1099,
        price: 1099,
      },
      {
        image: "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&q=80",
        productName: "Samsung Galaxy S25 Ultra",
        description:
          "Experience the brilliance of the Samsung Galaxy S25 Ultra with its vibrant AMOLED display, powerful quad-camera setup, and sleek modern design.",
        specialPrice: 1199,
        price: 1199,
      },
      {
        image: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80",
        productName: "MacBook Air M4",
        description:
          "Super thin and light MacBook Air featuring the powerful new M4 chip, all-day battery life, and a gorgeous Liquid Retina display for ultimate productivity.",
        price: 1299,
        specialPrice: 1299,
      }
];

const About = () => {
    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-slate-800 text-4xl font-bold text-center mb-12">
                About Us
            </h1>
           <div className="flex flex-col lg:flex-row justify-between items-center mb-12 gap-8">
                <div className="w-full md:w-1/2 text-center md:text-left">
                    <p className="text-lg mb-4 text-slate-700 leading-relaxed">
                        Welcome to E-Shop, your premier destination for high-end electronics. We are passionate about connecting you with the latest technology, from cutting-edge smartphones to high-performance laptops and audiophile-grade gear.
                    </p>
                    <p className="text-lg mb-4 text-slate-700 leading-relaxed">
                        Our team is dedicated to providing an exceptional shopping experience. We carefully curate our catalog to ensure you only get top-tier products, competitive pricing, and unparalleled customer support. Discover the future of tech with us today!
                    </p>
                </div>

                <div className="w-full md:w-1/2 mb-6 md:mb-0">
                    <img
                        src="https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800&q=80"
                        alt="About Us - Electronics Store"
                        className="w-full h-auto rounded-lg shadow-xl transform transition-transform duration-300 hover:scale-105" />
                </div>
           </div>


           <div className="py-7 space-y-8">
            <h1 className="text-slate-800 text-4xl font-bold text-center">
                Our Products
            </h1>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
               {products.map((product, index) => (
                <ProductCard 
                    key={index}
                    image={product.image}
                    productName={product.productName}
                    description={product.description}
                    specialPrice={product.specialPrice}
                    price={product.price}
                    about
                />
               ))
               }
                

            </div>
           </div>
        </div>
    );
}

export default About;