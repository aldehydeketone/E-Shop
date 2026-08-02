const getProductImageUrl = (image) => {
  if (!image) return "";

  const apiUrl = import.meta.env.VITE_API_URL?.replace(/\/$/, "");

  return /^https?:\/\//i.test(image)
    ? image
    : apiUrl ? `${apiUrl}/images/${image}` : `/images/${image}`;
};

export default getProductImageUrl;
