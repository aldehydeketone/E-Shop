        package com.ecommerce.project.service;

        import com.ecommerce.project.exceptions.APIException;
        import com.ecommerce.project.exceptions.ResourceNotFoundException;
        import com.ecommerce.project.model.Cart;
        import com.ecommerce.project.model.Category;
        import com.ecommerce.project.model.Product;
        import com.ecommerce.project.model.User;
        import com.ecommerce.project.payload.CartDTO;
        import com.ecommerce.project.payload.ProductDTO;
        import com.ecommerce.project.payload.ProductResponse;
        import com.ecommerce.project.repositories.CartRepository;
        import com.ecommerce.project.repositories.CategoryRepository;
        import com.ecommerce.project.repositories.ProductRepository;
        import com.ecommerce.project.util.AuthUtil;
        import org.modelmapper.ModelMapper;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.data.domain.Page;
        import org.springframework.data.domain.PageRequest;
        import org.springframework.data.domain.Pageable;
        import org.springframework.data.domain.Sort;
        import org.springframework.data.jpa.domain.Specification;
        import org.springframework.stereotype.Service;
        import org.springframework.web.multipart.MultipartFile;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.io.IOException;
        import java.util.List;
        import java.util.stream.Collectors;
        import org.springframework.transaction.annotation.Transactional;

        @Service
        public class ProductServiceImpl implements ProductService {
            private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
            @Autowired
            private CartRepository cartRepository;

            @Autowired
            private CartService cartService;

            @Autowired
            private ProductRepository productRepository;

            @Autowired
            private CategoryRepository categoryRepository;

            @Autowired
            private ModelMapper modelMapper;

            @Autowired
            private FileService fileService;

            @Autowired
            AuthUtil authUtil;

            @Value("${project.image}")
            private String path;

            @Value("${image.base.url}")
            private String imageBaseUrl;

            // Git commit info — baked into git.properties by git-commit-id-maven-plugin at Docker build time
            @Value("${git.commit.id.abbrev:UNKNOWN-abbrev}")
            private String gitCommitShort;

            @Value("${git.commit.id:UNKNOWN-full}")
            private String gitCommitFull;

            @Value("${git.branch:UNKNOWN-branch}")
            private String gitBranch;

            @Value("${git.commit.message.short:UNKNOWN-msg}")
            private String gitCommitMessage;

            @Override
            @Transactional
            public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
                try {
                    log.info("ADD_PRODUCT STEP 1 - Method entered. categoryId={}, productName={}",
                            categoryId, productDTO.getProductName());
                    log.info("ADD_PRODUCT STEP 1 - RUNNING COMMIT: branch={} commit={} (full={}) msg=[{}]",
                            gitBranch, gitCommitShort, gitCommitFull, gitCommitMessage);

                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Category", "categoryId", categoryId));
                    log.info("ADD_PRODUCT STEP 2 - Category loaded. categoryName={}", category.getCategoryName());

                    boolean exists = productRepository.existsByCategoryAndProductName(category, productDTO.getProductName());
                    log.info("ADD_PRODUCT STEP 3 - Duplicate check complete. exists={}", exists);
                    if (exists) {
                        throw new APIException("Product already exist!!");
                    }

                    Product product = modelMapper.map(productDTO, Product.class);
                    log.info("ADD_PRODUCT STEP 4 - DTO mapped to Product. productId(pre-save)={}", product.getProductId());

                    product.setImage("default.png");
                    log.info("ADD_PRODUCT STEP 5 - Image set to default.png");

                    product.setCategory(category);
                    log.info("ADD_PRODUCT STEP 6 - Category assigned to product");

                    User loggedInUser = authUtil.loggedInUser();
                    product.setUser(loggedInUser);
                    log.info("ADD_PRODUCT STEP 7 - User assigned. userId={}, username={}",
                            loggedInUser != null ? loggedInUser.getUserId() : "NULL",
                            loggedInUser != null ? loggedInUser.getUserName() : "NULL");

                    double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
                    product.setSpecialPrice(specialPrice);
                    log.info("ADD_PRODUCT STEP 8 - Special price calculated. price={}, discount={}, specialPrice={}",
                            product.getPrice(), product.getDiscount(), specialPrice);

                    log.info("ADD_PRODUCT STEP 9a - BEFORE productRepository.save()");
                    Product savedProduct;
                    try {
                        savedProduct = productRepository.save(product);
                        log.info("ADD_PRODUCT STEP 9b - AFTER productRepository.save(). savedProductId={}", savedProduct.getProductId());
                    } catch (Exception saveEx) {
                        Throwable rootCause = saveEx;
                        while (rootCause.getCause() != null) rootCause = rootCause.getCause();
                        log.error("ADD_PRODUCT STEP 9 FAILED - productRepository.save() threw exception");
                        log.error("  Exception class  : {}", saveEx.getClass().getName());
                        log.error("  Exception message: {}", saveEx.getMessage());
                        log.error("  Root cause class : {}", rootCause.getClass().getName());
                        log.error("  Root cause msg   : {}", rootCause.getMessage());
                        log.error("  Full stack trace :", saveEx);
                        throw saveEx;
                    }

                    log.info("ADD_PRODUCT STEP 10 - BEFORE toProductDTO(). savedProductId={}", savedProduct.getProductId());
                    ProductDTO response;
                    try {
                        response = toProductDTO(savedProduct);
                        log.info("ADD_PRODUCT STEP 11 - toProductDTO() complete. Returning response. productId={}", response.getProductId());
                    } catch (Exception dtoEx) {
                        Throwable rootCause = dtoEx;
                        while (rootCause.getCause() != null) rootCause = rootCause.getCause();
                        log.error("ADD_PRODUCT STEP 10 FAILED - toProductDTO() threw exception");
                        log.error("  Exception class  : {}", dtoEx.getClass().getName());
                        log.error("  Exception message: {}", dtoEx.getMessage());
                        log.error("  Root cause class : {}", rootCause.getClass().getName());
                        log.error("  Root cause msg   : {}", rootCause.getMessage());
                        log.error("  Full stack trace :", dtoEx);
                        throw dtoEx;
                    }

                    return response;

                } catch (Exception e) {
                    Throwable rootCause = e;
                    while (rootCause.getCause() != null) rootCause = rootCause.getCause();
                    log.error("ADD_PRODUCT OUTER CATCH - Unhandled exception escaping addProduct()");
                    log.error("  Exception class  : {}", e.getClass().getName());
                    log.error("  Exception message: {}", e.getMessage());
                    log.error("  Root cause class : {}", rootCause.getClass().getName());
                    log.error("  Root cause msg   : {}", rootCause.getMessage());
                    log.error("  Full stack trace :", e);
                    throw e;
                }
            }

            @Override
            public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category) {
                Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

                Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
                Specification<Product> spec = (root, query, cb) -> cb.conjunction();
                if (keyword != null && !keyword.isEmpty()) {
                    spec = spec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
                }

                if (category != null && !category.isEmpty()) {
                    spec = spec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.like(root.get("category").get("categoryName"), category));
                }

                Page<Product> pageProducts = productRepository.findAll(spec, pageDetails);

                List<Product> products = pageProducts.getContent();

                List<ProductDTO> productDTOS = products.stream()
                        .map(this::toProductDTO)
                        .toList();

                ProductResponse productResponse = new ProductResponse();
                productResponse.setContent(productDTOS);
                productResponse.setPageNumber(pageProducts.getNumber());
                productResponse.setPageSize(pageProducts.getSize());
                productResponse.setTotalElements(pageProducts.getTotalElements());
                productResponse.setTotalPages(pageProducts.getTotalPages());
                productResponse.setLastPage(pageProducts.isLast());
                return productResponse;
            }


            @Override
            public ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
                Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

                Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
                Page<Product> pageProducts = productRepository.findAll(pageDetails);

                List<Product> products = pageProducts.getContent();

                List<ProductDTO> productDTOS = products.stream()
                        .map(this::toProductDTO)
                        .toList();

                ProductResponse productResponse = new ProductResponse();
                productResponse.setContent(productDTOS);
                productResponse.setPageNumber(pageProducts.getNumber());
                productResponse.setPageSize(pageProducts.getSize());
                productResponse.setTotalElements(pageProducts.getTotalElements());
                productResponse.setTotalPages(pageProducts.getTotalPages());
                productResponse.setLastPage(pageProducts.isLast());
                return productResponse;
            }

            @Override
            public ProductResponse getAllProductsForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
                Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

                Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

                User user = authUtil.loggedInUser();
                Page<Product> pageProducts = productRepository.findByUser(user, pageDetails);

                List<Product> products = pageProducts.getContent();

                List<ProductDTO> productDTOS = products.stream()
                        .map(this::toProductDTO)
                        .toList();

                ProductResponse productResponse = new ProductResponse();
                productResponse.setContent(productDTOS);
                productResponse.setPageNumber(pageProducts.getNumber());
                productResponse.setPageSize(pageProducts.getSize());
                productResponse.setTotalElements(pageProducts.getTotalElements());
                productResponse.setTotalPages(pageProducts.getTotalPages());
                productResponse.setLastPage(pageProducts.isLast());
                return productResponse;
            }

            private String constructImageUrl(String imageName) {
                if (imageName == null) {
                    return "";
                }
                if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
                    return imageName;
                }
                return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
            }

            @Override
            public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Category", "categoryId", categoryId));

                Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

                Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
                Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

                List<Product> products = pageProducts.getContent();

                if(products.isEmpty()){
                    throw new APIException(category.getCategoryName() + " category does not have any products");
                }

                List<ProductDTO> productDTOS = products.stream()
                        .map(this::toProductDTO)
                        .toList();

                ProductResponse productResponse = new ProductResponse();
                productResponse.setContent(productDTOS);
                productResponse.setPageNumber(pageProducts.getNumber());
                productResponse.setPageSize(pageProducts.getSize());
                productResponse.setTotalElements(pageProducts.getTotalElements());
                productResponse.setTotalPages(pageProducts.getTotalPages());
                productResponse.setLastPage(pageProducts.isLast());
                return productResponse;
            }

            @Override
            public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
                Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

                Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
                Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

                List<Product> products = pageProducts.getContent();
                List<ProductDTO> productDTOS = products.stream()
                        .map(this::toProductDTO)
                        .toList();

                if(products.isEmpty()){
                    throw new APIException("Products not found with keyword: " + keyword);
                }

                ProductResponse productResponse = new ProductResponse();
                productResponse.setContent(productDTOS);
                productResponse.setPageNumber(pageProducts.getNumber());
                productResponse.setPageSize(pageProducts.getSize());
                productResponse.setTotalElements(pageProducts.getTotalElements());
                productResponse.setTotalPages(pageProducts.getTotalPages());
                productResponse.setLastPage(pageProducts.isLast());
                return productResponse;
            }

            @Override
            @Transactional
            public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
                Product productFromDb = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

                Product product = modelMapper.map(productDTO, Product.class);

                productFromDb.setProductName(product.getProductName());
                productFromDb.setDescription(product.getDescription());
                productFromDb.setQuantity(product.getQuantity());
                productFromDb.setDiscount(product.getDiscount());
                productFromDb.setPrice(product.getPrice());
                productFromDb.setSpecialPrice(product.getSpecialPrice());

                Product savedProduct = productRepository.save(productFromDb);

                List<Cart> carts = cartRepository.findCartsByProductId(productId);

                List<CartDTO> cartDTOs = carts.stream().map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(p -> toProductDTO(p.getProduct())).collect(Collectors.toList());

                    cartDTO.setProducts(products);

                    return cartDTO;

                }).collect(Collectors.toList());

                cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

                return toProductDTO(savedProduct);
            }

            @Override
            @Transactional
            public ProductDTO deleteProduct(Long productId) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

                if (product.getImage() != null && product.getImage().contains("cloudinary.com")) {
                    try {
                        fileService.deleteImage(product.getImage());
                    } catch (IOException e) {
                        log.error("Failed to delete image from Cloudinary for product id: {}", productId, e);
                    }
                }

                // DELETE
                List<Cart> carts = cartRepository.findCartsByProductId(productId);
                carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

                productRepository.delete(product);
                return toProductDTO(product);
            }

            @Override
            @Transactional
            public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
                Product productFromDb = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

                if (productFromDb.getImage() != null && productFromDb.getImage().contains("cloudinary.com")) {
                    fileService.deleteImage(productFromDb.getImage());
                }

                log.info("[IMAGE UPLOAD SERVICE] Before calling fileService.uploadImage for productId: {}", productId);
                String secureUrl = fileService.uploadImage(path, image);
                log.info("[IMAGE UPLOAD SERVICE] After calling fileService.uploadImage. Returned secureUrl: {}", secureUrl);
                productFromDb.setImage(secureUrl);

                Product updatedProduct = productRepository.save(productFromDb);
                return toProductDTO(updatedProduct);
            }

            private ProductDTO toProductDTO(Product product) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(product.getProductId());
                productDTO.setProductName(product.getProductName());
                productDTO.setImage(constructImageUrl(product.getImage()));
                productDTO.setDescription(product.getDescription());
                productDTO.setQuantity(product.getQuantity());
                productDTO.setPrice(product.getPrice());
                productDTO.setDiscount(product.getDiscount());
                productDTO.setSpecialPrice(product.getSpecialPrice());
                return productDTO;
            }


        }
