const getProductImageUrl = (image) => {
  if (!image) return "";

  return /^https?:\/\//i.test(image)
    ? image
    : `${import.meta.env.VITE_BACK_END_URL}/images/${image}`;
};

export default getProductImageUrl;
